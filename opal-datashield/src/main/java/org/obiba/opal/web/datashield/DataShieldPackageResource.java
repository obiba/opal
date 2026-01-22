/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.datashield.support.DatashieldPackageMethodHelper;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.OpalR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manage a Datashield Package.
 */
@Component
@Transactional
@Scope("request")
@Path("/datashield/package/{name}")
@Tag(name = "DataSHIELD", description = "Operations on DataSHIELD")
public class DataShieldPackageResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @Autowired
  private DatashieldPackageMethodHelper dsPackageMethodeHelper;

  @Autowired
  private OpalGeneralConfigService opalGeneralConfigService;

  @GET
  @Operation(
    summary = "Get DataSHIELD package",
    description = "Retrieves information about a specific DataSHIELD package installed in the specified profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved package information"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<OpalR.RPackageDto> getPackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getPackage(getDataShieldProfile(profile), name);
  }

  /**
   * Get all the methods of the package.
   *
   * @return
   */
  @GET
  @Path("methods")
  @Operation(
    summary = "Get DataSHIELD package methods",
    description = "Retrieves all available methods for a specific DataSHIELD package in the specified profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved package methods"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShield.DataShieldPackageMethodsDto getPackageMethods(@PathParam("name") String name, @QueryParam("profile") String profile) {
    return dsPackageMethodeHelper.getPackageMethods(getDataShieldProfile(profile), name);
  }

  /**
   * Publish all the methods of the package.
   *
   * @return the installed methods
   */
  @PUT
  @Path("methods")
  @Deprecated
  @Operation(
    summary = "Publish DataSHIELD package methods (deprecated)",
    description = "Publishes all methods of a DataSHIELD package to the specified profiles. This endpoint is deprecated."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully published package methods"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    DataShield.DataShieldPackageMethodsDto rval = DataShield.DataShieldPackageMethodsDto.newBuilder().setName(name).build();
    if (profiles != null)
      dsPackageMethodeHelper.publish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return rval;
  }

  /**
   * Append package DataSHIELD settings to profile configuration.
   *
   * @param name
   * @param profiles
   * @return
   */
  @PUT
  @Path("_publish")
  @Operation(
    summary = "Publish DataSHIELD package settings",
    description = "Appends DataSHIELD package settings to the specified profile configurations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully published package settings"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response publishPackageSettings(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.publish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.ok().build();
  }

  /**
   * Delete all settings of the package from the listed profiles.
   *
   * @param name
   * @param profiles
   * @return
   */
  @DELETE
  @Path("methods")
  @Deprecated
  @Operation(
    summary = "Delete DataSHIELD package methods (deprecated)",
    description = "Removes all methods of a DataSHIELD package from the specified profiles. This endpoint is deprecated."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Successfully deleted package methods"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePackageMethods(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.unpublish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.noContent().build();
  }

  /**
   * Remove package DataSHIELD settings from each profile configuration.
   *
   * @param name
   * @param profiles
   * @return
   */
  @DELETE
  @Path("_publish")
  @Operation(
    summary = "Delete DataSHIELD package settings",
    description = "Removes DataSHIELD package settings from each specified profile configuration."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Successfully deleted package settings"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePackageSettings(@PathParam("name") String name, @QueryParam("profile") List<String> profiles) {
    if (profiles != null)
      dsPackageMethodeHelper.unpublish(profiles.stream().map(this::getDataShieldProfile).collect(Collectors.toList()), name);
    return Response.noContent().build();
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  @Operation(
    summary = "Delete DataSHIELD package",
    description = "Silently deletes a DataSHIELD package and all its methods from the specified profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully deleted package"),
    @ApiResponse(responseCode = "403", description = "R package management not allowed"),
    @ApiResponse(responseCode = "404", description = "Package not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePackage(@PathParam("name") String name, @QueryParam("profile") String profile) {
    if (!opalGeneralConfigService.getConfig().isAllowRPackageManagement())
      return Response.status(Response.Status.FORBIDDEN).build();

    try {
      dsPackageMethodeHelper.deletePackage(getDataShieldProfile(profile), name);
    } catch (Exception e) {
      // ignored
    }
    return Response.ok().build();
  }

  private DataShieldProfile getDataShieldProfile(String profileName) {
    return datashieldProfileService.getProfile(profileName);
  }

}
