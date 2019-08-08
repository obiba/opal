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
import org.obiba.opal.spi.r.RScriptROperation;
import org.obiba.opal.spi.r.RStringMatrix;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope("request")
@Path("/service/r/packages")
public class RServicePackagesResource {

  private static final Logger log = LoggerFactory.getLogger(RServicePackagesResource.class);

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @GET
  public List<OpalR.RPackageDto> getPackages() throws REXPMismatchException {
    RScriptROperation rop = rPackageHelper.getInstalledPackages();
    REXP rexp = rop.getResult();
    RStringMatrix matrix = new RStringMatrix(rexp);
    return StreamSupport.stream(matrix.iterateRows().spliterator(), false)
        .map(new RPackageResourceHelper.StringsToRPackageDto(matrix))
        .collect(Collectors.toList());
  }

  @PUT
  public Response updateAllPackages() {
    try {
      // dump all R sessions
      rPackageHelper.restartRServer();
      String cmd = ".libPaths()";
      RScriptROperation rop = rPackageHelper.execute(cmd);
      REXP rexp = rop.getResult();
      cmd = "getwd()";
      rop = rPackageHelper.execute(cmd);
      log.info("getwd={}", rop.getResult().asString());
      String libpath = rexp.asStrings()[0];
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
                                 @QueryParam("ref") String ref) {
    rPackageHelper.installPackage(name, ref, "obiba");

    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(name)).build();
  }
}
