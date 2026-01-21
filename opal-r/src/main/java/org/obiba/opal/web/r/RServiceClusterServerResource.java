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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "R", description = "Operations on R")
public class RServiceClusterServerResource extends AbstractRServiceResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceClusterServerResource.class);

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  //
  // R server methods
  //

  @GET
  @Operation(
    summary = "Get R server in cluster",
    description = "Retrieves detailed information about a specific R server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public OpalR.RServerDto getServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName));
  }

  @PUT
  @Operation(
    summary = "Start R server in cluster",
    description = "Starts a specific R server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Server successfully started"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Failed to start server")
  })
  public Response startServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName).start();
    return Response.ok().build();
  }

  @DELETE
  @Operation(
    summary = "Stop R server in cluster",
    description = "Stops a specific R server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Server successfully stopped"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Failed to stop server")
  })
  public Response stopServer(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName).stop();
    return Response.ok().build();
  }

  @GET
  @Path("/packages")
  @Operation(
    summary = "Get server R packages",
    description = "Retrieves a list of all R packages installed on a specific R server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R packages"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RPackageDto> getPackages(@PathParam("cname") String clusterName, @PathParam("sname") String serverName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName));
  }

  @PUT
  @Path("/packages")
  @Operation(
    summary = "Update all server R packages",
    description = "Updates all installed R packages on a specific R server within a cluster to their latest versions from CRAN."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Packages successfully updated"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Failed to update packages")
  })
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
  @Operation(
    summary = "Install R package on server",
    description = "Installs a specified R package on a specific R server within a cluster from CRAN, GitHub, or other repositories."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Package successfully installed"),
    @ApiResponse(responseCode = "400", description = "Invalid package name or reference"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Failed to install package")
  })
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
  @Operation(
    summary = "Delete multiple R packages from server",
    description = "Removes multiple R packages from a specific R server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Packages successfully deleted"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Failed to delete packages")
  })
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
  @Operation(
    summary = "Get specific R package from server",
    description = "Retrieves detailed information about a specific R package installed on a server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R package"),
    @ApiResponse(responseCode = "404", description = "R server cluster, server, or package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public OpalR.RPackageDto getPackage(@PathParam("cname") String clusterName, @PathParam("sname") String serverName, @PathParam("pname") String pkgName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName)).stream()
        .filter(dto -> dto.getName().equals(pkgName))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(pkgName));
  }

  @DELETE
  @Path("/package/{pname}")
  @Operation(
    summary = "Delete R package from server",
    description = "Removes a specific R package from a server within a cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Package successfully deleted"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster, server, or package not found"),
    @ApiResponse(responseCode = "500", description = "Failed to delete package")
  })
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
  @Operation(
    summary = "Get server R server log",
    description = "Retrieves the R server log content for a specific server within a cluster, optionally limited to a specific number of lines."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server log"),
    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    @ApiResponse(responseCode = "404", description = "R server cluster or server not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response tailRserveLog(@PathParam("cname") String clusterName, @PathParam("sname") String serverName, @QueryParam("n") @DefaultValue("10000") Integer nbLines) {
    return tailRserveLog(rServerManagerService.getRServerCluster(clusterName).getRServerService(serverName), nbLines, clusterName + "-" + serverName);
  }

}