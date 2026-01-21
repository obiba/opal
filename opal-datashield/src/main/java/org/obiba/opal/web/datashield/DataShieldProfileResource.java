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
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.DataShield;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

@Component
@Transactional
@Scope("request")
@Path("/datashield/profile/{name}")
@Tag(name = "DataSHIELD", description = "Operations on DataSHIELD")
public class DataShieldProfileResource {

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @GET
  @Operation(
    summary = "Get DataSHIELD profile",
    description = "Retrieves detailed information about a specific DataSHIELD profile including configuration, methods, and options. Access restricted for profiles with restricted access."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "403", description = "Access to profile forbidden"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public DataShield.DataShieldProfileDto getProfile(@PathParam("name") String name) {
    return Dtos.asDto(getProfileInternal(name));
  }

  @PUT
  @Path("_enable")
  @Operation(
    summary = "Enable DataSHIELD profile",
    description = "Enables a DataSHIELD profile, making it available for use in DataSHIELD operations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully enabled profile"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response enableProfile(@PathParam("name") String name) {
    doEnableProfile(name, true);
    return Response.ok().build();
  }

  @DELETE
  @Path("_enable")
  @Operation(
    summary = "Disable DataSHIELD profile",
    description = "Disables a DataSHIELD profile, making it unavailable for use in DataSHIELD operations."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully disabled profile"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response disableProfile(@PathParam("name") String name) {
    doEnableProfile(name, false);
    return Response.ok().build();
  }

  @PUT
  @Path("_access")
  @Operation(
    summary = "Set restricted access on DataSHIELD profile",
    description = "Enables restricted access mode on a DataSHIELD profile, requiring explicit permissions for access."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully set restricted access"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response restrictedAccessProfile(@PathParam("name") String name) {
    doRestrictAccessProfile(name, true);
    return Response.ok().build();
  }

  @DELETE
  @Path("_access")
  @Operation(
    summary = "Remove restricted access from DataSHIELD profile",
    description = "Disables restricted access mode on a DataSHIELD profile, making it accessible to all users."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully removed restricted access"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response unrestrictedAccessProfile(@PathParam("name") String name) {
    doRestrictAccessProfile(name, false);
    return Response.ok().build();
  }

  @PUT
  @Path("_rparser")
  @Operation(
    summary = "Set R parser version for DataSHIELD profile",
    description = "Configures the R parser version to be used for parsing R expressions in the DataSHIELD profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully set R parser version"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setRParser(@PathParam("name") String name, @QueryParam("version") String version) {
    doApplyRParserVersion(name, version);
    return Response.ok().build();
  }

  @DELETE
  @Path("_rparser")
  @Operation(
    summary = "Remove R parser version from DataSHIELD profile",
    description = "Removes the custom R parser version configuration from the DataSHIELD profile, reverting to default."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully removed R parser version"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response removeRParser(@PathParam("name") String name) {
    doApplyRParserVersion(name, null);
    return Response.ok().build();
  }

  /**
   * Remove a secondary profile, primary ones are sticked to their cluster.
   *
   * @param name
   * @return
   */
  @DELETE
  @Operation(
    summary = "Delete DataSHIELD profile",
    description = "Removes a secondary DataSHIELD profile. Primary profiles tied to clusters cannot be deleted unless forced. Use force parameter for obsolete cluster profiles."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Successfully deleted profile"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "403", description = "Cannot delete primary profile without force parameter"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteProfile(@PathParam("name") String name, @QueryParam("force") @DefaultValue("false") boolean force) {
    if (datashieldProfileService.hasProfile(name)) {
      // primary profiles cannot be removed (unless forced, in case a cluster is obsolete)
      DataShieldProfile profile = datashieldProfileService.getProfile(name);
      if (force || !profile.getName().equals(profile.getCluster()))
        datashieldProfileService.deleteProfile(new DataShieldProfile(name));
    }
    return Response.noContent().build();
  }

  //
  // Private methods
  //

  private void doApplyRParserVersion(String name, String version) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setRParserVersion(version);
    datashieldProfileService.saveProfile(profile);
  }

  private void doEnableProfile(String name, boolean enabled) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setEnabled(enabled);
    datashieldProfileService.saveProfile(profile);
  }

  private void doRestrictAccessProfile(String name, boolean restricted) {
    DataShieldProfile profile = getProfileInternal(name);
    profile.setRestrictedAccess(restricted);
    datashieldProfileService.saveProfile(profile);
  }

  private DataShieldProfile getProfileInternal(String name) {
    DataShieldProfile profile = datashieldProfileService.findProfile(name);
    if (profile == null)
      throw new NotFoundException("No DataSHIELD profile with name: " + name);
    if (profile.isRestrictedAccess() && !SecurityUtils.getSubject().isPermitted(String.format("rest:/datashield/profile/%s:GET", profile.getName())))
      throw new ForbiddenException("DataSHIELD profile access is forbidden: " + profile.getName());
    return profile;
  }

}
