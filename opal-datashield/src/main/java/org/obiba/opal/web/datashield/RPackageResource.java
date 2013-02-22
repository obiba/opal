/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
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
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import org.obiba.opal.r.RMatrix;
import org.obiba.opal.r.RScriptROperation;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 *
 */
public abstract class RPackageResource {

  private static final Logger log = LoggerFactory.getLogger(RPackageResource.class);

  protected static final String AGGREGATE_METHODS = "AggregateMethods";
  protected static final String ASSIGN_METHODS = "AssignMethods";
  private static final String[] defaultFields = new String[] { "Title", "Description", "Author", "Maintainer",
      "Date/Publication", AGGREGATE_METHODS, ASSIGN_METHODS};

  private static final String[] defaultRepos = new String[] { "http://cran.obiba.org", "http://cran.datashield.org",
      "http://cran.rstudio.com" };

  protected final OpalRService opalRService;

  protected RPackageResource(OpalRService opalRService) {
    this.opalRService = opalRService;
  }

  protected RScriptROperation getInstalledPackages() {
    return getInstalledPackages(new ArrayList<String>());
  }

  protected RScriptROperation getInstalledPackages(List<String> fields) {
    Iterable<String> allFields = Iterables.concat(Arrays.asList(defaultFields), fields);
    String fieldStr = StringUtils.collectionToDelimitedString(Lists.newArrayList(allFields), ",", "\"", "\"");
    String cmd = "installed.packages(fields=c(" + fieldStr + "))";
    return execute(cmd);
  }

  protected RScriptROperation removePackage(String name) {
    String cmd = "remove.packages('" + name + "')";
    return execute(cmd);
  }

  protected RScriptROperation installPackage(String name) {
    return execute(getInstallPackagesCommand(name));
  }

  protected RScriptROperation installDatashieldPackage(String name, String ref) {
    String cmd;
    if(Strings.isNullOrEmpty(ref)) {
      cmd = getInstallPackagesCommand(name);
    } else {
      execute(getInstallDevtoolsPackageCommand());
      cmd = getInstallGitHubCommand(name, "datashield", ref);
    }
    return execute(cmd);
  }

  private String getInstallPackagesCommand(String name) {
    String repos = StringUtils.collectionToDelimitedString(Lists.newArrayList(defaultRepos), ",", "'", "'");
    return "install.packages('" + name + "', repos=c(" + repos + "), dependencies=TRUE)";
  }

  private String getInstallDevtoolsPackageCommand() {
    return "if (!require('devtools', character.only=TRUE)) { " + getInstallPackagesCommand("devtools") + " }";
  }

  private String getInstallGitHubCommand(String name, String username, String ref) {
    return "devtools::install_github('" + name + "', username='" + username + "', ref='" + ref + "')";
  }

  protected RScriptROperation execute(String rscript) {
    log.info(rscript);
    RScriptROperation rop = new RScriptROperation(rscript, false);
    opalRService.execute(rop);
    return rop;
  }

  protected Response asResponse(RScriptROperation rop) {
    if(rop.hasResult() && rop.hasRawResult()) {
      return Response.ok().entity(rop.getRawResult().asBytes()).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  public class StringsToRPackageDto implements Function<String[], OpalR.RPackageDto> {

    private int current = 0;

    private final RMatrix<String> matrix;

    public StringsToRPackageDto(RMatrix<String> matrix) {
      this.matrix = matrix;
    }

    @Override
    public OpalR.RPackageDto apply(@Nullable String[] input) {
      OpalR.RPackageDto.Builder builder = OpalR.RPackageDto.newBuilder();
      for(int i = 0; i < input.length; i++) {
        if(!Strings.isNullOrEmpty(input[i])) {
          OpalR.EntryDto.Builder entry = OpalR.EntryDto.newBuilder();
          entry.setKey(matrix.getColumnName(i));
          entry.setValue(input[i]);
          builder.addDescription(entry);
        }
      }
      builder.setName(matrix.getRowName(current++));
      return builder.build();
    }
  }

  protected static class DataShieldPackagePredicate implements Predicate<OpalR.RPackageDto> {
    @Override
    public boolean apply(@Nullable OpalR.RPackageDto input) {
      for(OpalR.EntryDto entry : input.getDescriptionList()) {
        if(entry.getKey().equals("AggregationMethods") || entry.getKey().equals("AssignMethods")) {
          return true;
        }
      }
      return false;
    }
  }
}
