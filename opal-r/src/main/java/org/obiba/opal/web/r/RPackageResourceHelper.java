/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.r;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.spi.r.RMatrix;
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Helper class for R package management.
 */
@Component
public class RPackageResourceHelper {

  private static final Logger log = LoggerFactory.getLogger(RPackageResourceHelper.class);

  public static final String VERSION = "Version";

  public static final String AGGREGATE_METHODS = "AggregateMethods";

  public static final String ASSIGN_METHODS = "AssignMethods";

  public static final String OPTIONS = "Options";

  private static final String[] defaultFields = new String[]{"Title", "Description", "Author", "Maintainer",
      "Date/Publication", AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS};

  @Value("${org.obiba.opal.r.repos}")
  private String defaultRepos;

  @Autowired
  protected OpalRService opalRService;

  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    ROperationWithResult rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    try {
      RStringMatrix matrix = new RStringMatrix(rexp);
      return StreamSupport.stream(matrix.iterateRows().spliterator(), false)
          .map(new RPackageResourceHelper.StringsToRPackageDto(matrix))
          .collect(Collectors.toList());
    } catch (REXPMismatchException e) {
      log.error("Error when reading installed packages", e);
      return Lists.newArrayList();
    }
  }

  private ROperationWithResult getInstalledPackages() {
    return getInstalledPackages(new ArrayList<>());
  }

  public ROperationWithResult removePackage(String name) {
    checkAlphanumeric(name);
    String cmd = "remove.packages('" + name + "')";
    return execute(cmd);
  }

  public ROperationWithResult installPackage(String name, String ref, String defaultName) {
    String cmd;
    if (Strings.isNullOrEmpty(ref)) {
      checkAlphanumeric(name);
      cmd = getInstallPackagesCommand(name);
    } else {
      execute(getInstallDevtoolsPackageCommand());
      if (name.contains("/")) {
        String[] parts = name.split("/");
        checkAlphanumeric(parts[0]);
        checkAlphanumeric(parts[1]);
        cmd = getInstallGitHubCommand(parts[1], parts[0], ref);
      } else {
        checkAlphanumeric(name);
        cmd = getInstallGitHubCommand(name, defaultName, ref);
      }
    }
    ROperationWithResult rval = execute(cmd);
    restartRServer();
    return rval;
  }

  void restartRServer() {
    try {
      opalRService.stop();
      opalRService.start();
    } catch (Exception ex) {
      log.error("Error while restarting R server after package install: {}", ex.getMessage(), ex);
    }
  }

  List<String> getDefaultRepos() {
    return Lists.newArrayList(defaultRepos.split(",")).stream().map(String::trim).collect(Collectors.toList());
  }

  public ROperationWithResult execute(String rscript) {
    RScriptROperation rop = new RScriptROperation(rscript, false);
    return execute(rop);
  }

  public ROperationWithResult execute(ROperationWithResult rop) {
    opalRService.execute(rop);
    return rop;
  }

  private ROperationWithResult getInstalledPackages(Iterable<String> fields) {
    Iterable<String> allFields = Iterables.concat(Arrays.asList(defaultFields), fields);
    String fieldStr = Joiner.on("','").join(allFields);
    String cmd = "installed.packages(fields=c('" + fieldStr + "'))";
    return execute(cmd);
  }

  /**
   * Simple security check: the provided name (package name or Github user/organization name) must be alphanumeric.
   *
   * @param name
   */
  private void checkAlphanumeric(String name) {
    if (!name.matches("[a-zA-Z0-9\\-]+"))
      throw new IllegalArgumentException("Not a valid name: " + name);
  }

  private String getInstallPackagesCommand(String name) {
    String repos = Joiner.on("','").join(getDefaultRepos());
    return "install.packages('" + name + "', repos=c('" + repos + "'), dependencies=TRUE)";
  }

  private String getInstallDevtoolsPackageCommand() {
    return "if (!require('devtools', character.only=TRUE)) { " + getInstallPackagesCommand("devtools") + " }";
  }

  private String getInstallGitHubCommand(String name, String username, String ref) {
    return String.format("devtools::install_github('%s/%s', ref='%s', dependencies=TRUE, upgrade=TRUE)", username, name, ref);
  }


  public static class StringsToRPackageDto implements Function<String[], OpalR.RPackageDto> {

    private int current = 0;

    private final RMatrix<String> matrix;

    public StringsToRPackageDto(RMatrix<String> matrix) {
      this.matrix = matrix;
    }

    @Override
    public OpalR.RPackageDto apply(@Nullable String[] input) {
      OpalR.RPackageDto.Builder builder = OpalR.RPackageDto.newBuilder();
      if (input != null) {
        for (int i = 0; i < input.length; i++) {
          if (!Strings.isNullOrEmpty(input[i]) && !"NA".equals(input[i])) {
            Opal.EntryDto.Builder entry = Opal.EntryDto.newBuilder();
            entry.setKey(matrix.getColumnName(i));
            entry.setValue(input[i]);
            builder.addDescription(entry);
          }
        }
      }
      builder.setName(matrix.getRowName(current++));
      return builder.build();
    }
  }

}
