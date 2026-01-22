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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.web.model.OpalR;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Management of a cluster of R servers.
 */
@Component
@Scope("request")
@Path("/service/r/cluster/{cname}")
@Tag(name = "R", description = "Operations on R")
public class RServiceClusterResource extends AbstractRServiceResource {

  private static final Logger log = LoggerFactory.getLogger(RServiceClusterResource.class);

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  @Operation(
    summary = "Get R server cluster",
    description = "Retrieves detailed information about a specific R server cluster including its configuration and status."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server cluster"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public OpalR.RServerClusterDto getCluster(@PathParam("cname") String clusterName) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(clusterName));
  }

  @PUT
  @Operation(
    summary = "Start R server cluster",
    description = "Starts all R servers within the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cluster successfully started"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Failed to start cluster")
  })
  public Response startCluster(@PathParam("cname") String clusterName) {
    rServerManagerService.getRServerCluster(clusterName).start();
    return Response.ok().build();
  }

  @DELETE
  @Operation(
    summary = "Stop R server cluster",
    description = "Stops all R servers within the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Cluster successfully stopped"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Failed to stop cluster")
  })
  public Response stopCluster(@PathParam("cname") String clusterName) {
    rServerManagerService.getRServerCluster(clusterName).stop();
    return Response.ok().build();
  }

  @GET
  @Path("/packages")
  @Operation(
    summary = "Get cluster R packages",
    description = "Retrieves a list of all R packages installed in the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R packages"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RPackageDto> getPackages(@PathParam("cname") String clusterName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName));
  }

  @PUT
  @Path("/packages")
  @Operation(
    summary = "Update all cluster R packages",
    description = "Updates all installed R packages in the specified cluster to their latest versions from CRAN."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Packages successfully updated"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Failed to update packages")
  })
  public Response updateAllPackages(@PathParam("cname") String clusterName) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      rPackageHelper.updateAllCRANPackages(rServerManagerService.getRServerCluster(clusterName));
    } catch (Exception e) {
      log.error("Failed at updating all R packages", e);
    }
    return Response.ok().build();
  }

  @POST
  @Path("/packages")
  @Operation(
    summary = "Install R package in cluster",
    description = "Installs a specified R package in the cluster. The package can be installed from CRAN, GitHub, or other repositories."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Package successfully installed"),
    @ApiResponse(responseCode = "400", description = "Invalid package name or reference"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Failed to install package")
  })
  public Response installPackage(@Context UriInfo uriInfo, @PathParam("cname") String clusterName, @QueryParam("name") String pkgName,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    rPackageHelper.installPackage(rServerManagerService.getRServerCluster(clusterName), pkgName, ref, manager);
    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(pkgName)).build();
  }

  @DELETE
  @Path("/packages")
  @Operation(
    summary = "Delete multiple R packages from cluster",
    description = "Removes multiple R packages from the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Packages successfully deleted"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Failed to delete packages")
  })
  public Response deletePackages(@PathParam("cname") String clusterName, 
                                 @QueryParam("name") List<String> pkgNames) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    if (pkgNames != null) {
      pkgNames.forEach(pkgName -> deletePackage(clusterName, pkgName));
    }
    return Response.noContent().build();
  }

  @GET
  @Path("/packages/_check")
  @Operation(
    summary = "Check cluster R packages",
    description = "Checks the status and availability of R packages installed in the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully checked R packages"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RPackageDto> checkPackages(@PathParam("cname") String clusterName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName));
  }

  @GET
  @Path("/package/{pname}")
  @Operation(
    summary = "Get specific R package from cluster",
    description = "Retrieves detailed information about a specific R package installed in the cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R package"),
    @ApiResponse(responseCode = "404", description = "R server cluster or package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public OpalR.RPackageDto getPackage(@PathParam("cname") String clusterName, @PathParam("pname") String pkgName) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServerCluster(clusterName)).stream()
        .filter(dto -> dto.getName().equals(pkgName))
        .findFirst()
        .orElseThrow(() -> new NoSuchRPackageException(pkgName));
  }

  @DELETE
  @Path("/package/{pname}")
  @Operation(
    summary = "Delete R package from cluster",
    description = "Removes a specific R package from the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Package successfully deleted"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "404", description = "R server cluster or package not found"),
    @ApiResponse(responseCode = "500", description = "Failed to delete package")
  })
  public Response deletePackage(@PathParam("cname") String clusterName, @PathParam("pname") String pkgName) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      rPackageHelper.removePackage(rServerManagerService.getRServerCluster(clusterName), pkgName);
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }

  @GET
  @Path("/_log")
  @Operation(
    summary = "Get cluster R server log",
    description = "Retrieves the R server log content for the specified cluster, optionally limited to a specific number of lines."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R server log"),
    @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response tailRserveLog(@PathParam("cname") String clusterName, @QueryParam("n") @DefaultValue("10000") Integer nbLines) {
    return tailRserveLog(rServerManagerService.getRServerCluster(clusterName), nbLines, clusterName);
  }

  @GET
  @Path("/servers")
  @Operation(
    summary = "Get cluster servers",
    description = "Retrieves a list of all R servers within the specified cluster."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cluster servers"),
    @ApiResponse(responseCode = "404", description = "R server cluster not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RServerDto> getServers(@PathParam("cname") String clusterName) {
    return Dtos.asDto(rServerManagerService.getRServerCluster(clusterName)).getServersList();
  }

}