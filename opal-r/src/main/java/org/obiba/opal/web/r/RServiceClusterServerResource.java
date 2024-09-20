/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.r;

import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Management of a R server in a cluster.
 */
@Component
@Scope("request")
@Path("/service/r/cluster/{cname}/server/{sname}")
public class RServiceClusterServerResource extends AbstractRServiceResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceClusterServerResource.class);

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  //
  // R server methods
  //

  @GET
  public OpalR.RServerDto getServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName));
  }

  @PUT
  public Response startServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName).start();
    return Response.ok().build();
  }

  @DELETE
  public Response stopServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName).stop();
    return Response.ok().build();
  }

  @GET
  @Path("/packages")
  public List<OpalR.RPackageDto> getPackages(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName));
  }

  @PUT
  @Path("/packages")
  public Response updateAllPackages(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      rPackageHelper.updateAllCRANPackages(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName));
    } catch (Exception e) {
      log.error("Failed at updating all R packages", e);
    }
    return Response.ok().build();
  }

  @POST
  @Path("/packages")
  public Response installPackage(@Context UriInfo uriInfo, @PathParam("cname") String clusterName, @PathParam("sname") String serverName,
                                 @QueryParam("name") String pkgName,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    rPackageHelper.installPackage(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName), pkgName, ref, manager);
    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(pkgName)).build();
  }

  @DELETE
  @Path("/packages")
  public Response deletePackages(@PathParam("cname") String clusterName, @PathParam("sname") String serverName,
                                 @QueryParam("name") List<String> pkgNames) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    if (pkgNames != null) {
      pkgNames.forEach(pkgName -> deletePackage(clusterName, serverName, pkgName));
    }
    return Response.noContent().build();
  }

  @GET
  @Path("/package/{pname}")
  public OpalR.RPackageDto getPackage(@PathParam("cname") String clusterName, @PathParam("sname") String serverName, @PathParam("pname") String pkgName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName)).stream()
        .filter(dto -> dto.getName().equals(pkgName))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(pkgName));
  }

  @DELETE
  @Path("/package/{pname}")
  public Response deletePackage(@PathParam("cname") String clusterName, @PathParam("sname") String serverName, @PathParam("pname") String pkgName) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      rPackageHelper.removePackage(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName), pkgName);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }

  @GET
  @Path("/_log")
  public Response tailRserveLog(@PathParam("cname") String clusterName, @PathParam("sname") String serverName, @QueryParam("n") @DefaultValue("10000") Integer nbLines) {
    return tailRserveLog(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName), nbLines, clusterName + "-" + serverName);
  }

}