/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
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
import org.obiba.opal.spi.r.ROperationWithResult;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Component
@Scope("request")
@Path("/service/r/packages")
public class RServicePackagesResource {

  private static final Logger log = LoggerFactory.getLogger(RServicePackagesResource.class);

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @GET
  public List<OpalR.RPackageDto> getPackages() {
    return rPackageHelper.getInstalledPackagesDtos();
  }

  @PUT
  public Response updateAllPackages() {
    try {
      // dump all R sessions
      rPackageHelper.restartRServer();
      String cmd = ".libPaths()";
      ROperationWithResult rop = rPackageHelper.execute(cmd);
      String libpath = rop.getResult().asStrings()[0];
      cmd = "getwd()";
      rop = rPackageHelper.execute(cmd);
      log.info("getwd={}", rop.getResult().asStrings()[0]);
      String repos = Joiner.on("','").join(rPackageHelper.getDefaultRepos());
      cmd = String.format("update.packages(ask = FALSE, repos = c('%s'), instlib = '%s')", repos, libpath);
      rPackageHelper.execute(cmd);
      rPackageHelper.restartRServer();
    } catch (Exception e) {
      log.error("Failed at updating all R packages", e);
    }
    return Response.ok().build();
  }

  @POST
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager) {
    if (Strings.isNullOrEmpty(manager) || "cran".equalsIgnoreCase(manager))
      rPackageHelper.installCRANPackage(name);
    else if ("gh".equalsIgnoreCase(manager) || "github".equalsIgnoreCase(manager))
      rPackageHelper.installGitHubPackage(name, ref);
    else if ("bioc".equalsIgnoreCase(manager) || "bioconductor".equalsIgnoreCase(manager))
      rPackageHelper.installBioconductorPackage(name);

    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(name)).build();
  }
}
