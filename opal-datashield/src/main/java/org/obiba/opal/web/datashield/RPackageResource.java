/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.obiba.opal.spi.r.RMatrix;
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.web.datashield.support.NoSuchRPackageException;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Base class for R package management.
 */
public abstract class RPackageResource {

  private static final Logger log = LoggerFactory.getLogger(RPackageResource.class);

  protected static final String VERSION = "Version";

  protected static final String AGGREGATE_METHODS = "AggregateMethods";

  protected static final String ASSIGN_METHODS = "AssignMethods";

  protected static final String OPTIONS = "Options";

  private static final String[] defaultFields = new String[] { "Title", "Description", "Author", "Maintainer",
      "Date/Publication", AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS };

  @Value("${org.obiba.opal.r.repos}")
  private String defaultRepos;

  @Autowired
  protected OpalRService opalRService;

  protected RScriptROperation getInstalledPackages() {
    return getInstalledPackages(new ArrayList<String>());
  }

  protected RScriptROperation getInstalledPackages(Iterable<String> fields) {
    Iterable<String> allFields = Iterables.concat(Arrays.asList(defaultFields), fields);
    String fieldStr = StringUtils.collectionToDelimitedString(Lists.newArrayList(allFields), ",", "'", "'");
    String cmd = "installed.packages(fields=c(" + fieldStr + "))";
    return execute(cmd);
  }

  protected RScriptROperation removePackage(String name) {
    checkAlphanumeric(name);
    String cmd = "remove.packages('" + name + "')";
    return execute(cmd);
  }

  protected RScriptROperation installDatashieldPackage(String name, String ref) {
    String cmd;
    if(Strings.isNullOrEmpty(ref)) {
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
        cmd = getInstallGitHubCommand(name, "datashield", ref);
      }
    }
    RScriptROperation rval = execute(cmd);
    restartRServer();
    return rval;
  }

  /**
   * Simple security check: the provided name (package name or Github user/organization name) must be alphanumeric.
   *
   * @param name
   */
  private void checkAlphanumeric(String name) {
    if (!name.matches("[a-zA-Z0-9]+"))
      throw new IllegalArgumentException("Not a valid name: " + name);
  }

  private void restartRServer() {
    try {
      opalRService.stop();
      opalRService.start();
    } catch(Exception ex) {
      log.error("Error while restarting R server after package install: {}", ex.getMessage(), ex);
    }
  }

  private List<String> getDefaultRepos() {
    return Lists.newArrayList(defaultRepos.split(",")).stream().map(r -> r.trim()).collect(Collectors.toList());
  }

  private String getInstallPackagesCommand(String name) {
    String repos = StringUtils.collectionToDelimitedString(getDefaultRepos(), ",", "'", "'");
    return "install.packages('" + name + "', repos=c(" + repos + "), dependencies=TRUE)";
  }

  private String getInstallDevtoolsPackageCommand() {
    return "if (!require('devtools', character.only=TRUE)) { " + getInstallPackagesCommand("devtools") + " }";
  }

  private String getInstallGitHubCommand(String name, String username, String ref) {
    return String.format("devtools::install_github('%s/%s', ref='%s', dependencies=TRUE)", username, name, ref);
  }

  protected RScriptROperation execute(String rscript) {
    log.info(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    opalRService.execute(rop);
    return rop;
  }

  protected OpalR.RPackageDto getDatashieldPackage(final String name) throws REXPMismatchException {
    RScriptROperation rop = getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);

    Iterator<OpalR.RPackageDto> iter = Iterables
        .filter(Iterables.transform(matrix.iterateRows(), new StringsToRPackageDto(matrix)),
            new DataShieldPackagePredicate() {
              @Override
              public boolean apply(@Nullable OpalR.RPackageDto input) {
                return input != null && input.getName().equals(name) && super.apply(input);
              }
            }).iterator();

    if(iter.hasNext()) {
      return iter.next();
    }
    throw new NoSuchRPackageException(name);
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
      if(input != null) {
        for(int i = 0; i < input.length; i++) {
          if(!Strings.isNullOrEmpty(input[i]) && !"NA".equals(input[i])) {
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

  protected static class DataShieldPackagePredicate implements Predicate<OpalR.RPackageDto> {
    @Override
    public boolean apply(@Nullable OpalR.RPackageDto input) {
      if(input == null) return false;
      for(Opal.EntryDto entry : input.getDescriptionList()) {
        String key = entry.getKey();
        if(AGGREGATE_METHODS.equals(key) || ASSIGN_METHODS.equals(key) || OPTIONS.equals(key)) {
          return !"NA".equals(entry.getValue());
        }
      }
      return false;
    }
  }
}
