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
import org.obiba.opal.r.service.RServerManagerService;
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
 * R package management of the default R server cluster.
 */
@Component
@Scope("request")
@Path("/service/r/packages")
@Tag(name = "R", description = "Operations on R")
public class RServicePackagesResource {

  private static final Logger log = LoggerFactory.getLogger(RServicePackagesResource.class);

  @Autowired
  private RPackageResourceHelper rPackageHelper;

  @Autowired
  private RServerManagerService rServerManagerService;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  @Operation(
    summary = "Get all R packages",
    description = "Retrieves a list of all R packages installed in the default R server."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved R packages"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RPackageDto> getPackages(@QueryParam("profile") String profile) {
    return rPackageHelper.getInstalledPackagesDtos(rServerManagerService.getRServer(profile));
  }

  @PUT
  @Operation(
    summary = "Update all R packages",
    description = "Updates all installed R packages to their latest versions from CRAN."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Packages successfully updated"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "500", description = "Failed to update packages")
  })
  public Response updateAllPackages(@QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      rPackageHelper.updateAllCRANPackages(rServerManagerService.getRServer(profile));
    } catch (Exception e) {
      log.error("Failed at updating all R packages", e);
    }
    return Response.ok().build();
  }

  @POST
  @Operation(
    summary = "Install R package",
    description = "Installs a specified R package from CRAN, GitHub, or other repositories."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Package successfully installed"),
    @ApiResponse(responseCode = "400", description = "Invalid package name or reference"),
    @ApiResponse(responseCode = "403", description = "R package management is not allowed"),
    @ApiResponse(responseCode = "500", description = "Failed to install package")
  })
  public Response installPackage(@Context UriInfo uriInfo, @QueryParam("name") String name,
                                 @QueryParam("ref") String ref, @QueryParam("manager") String manager,
                                 @QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    rPackageHelper.installPackage(rServerManagerService.getRServer(profile), name, ref, manager);
    UriBuilder ub = uriInfo.getBaseUriBuilder().path(RServicePackageResource.class);
    return Response.created(ub.build(name)).build();
  }
}
