/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.app;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.cfg.AppsService;
import org.obiba.opal.web.model.Apps;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Path("/apps")
@Tag(name = "Applications", description = "Operations on registered applications")
public class AppsResource {

  private static final String APP_AUTH_HEADER = "X-App-Auth";

  @Autowired
  private AppsService appsService;

  @GET
  @Operation(summary = "List applications", description = "Returns a list of registered applications, optionally filtered by type.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Applications successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<Apps.AppDto> list(@QueryParam("type") String type) {
    return appsService.getApps(type).stream()
        .map(Dtos::asDto).collect(Collectors.toList());
  }

  @GET
  @Path("/config")
  @Operation(summary = "Get applications configuration", description = "Returns the current applications configuration.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configuration successfully retrieved"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Apps.AppsConfigDto getConfig() {
    return Dtos.asDto(appsService.getAppsConfig());
  }

  @PUT
  @Path("/config")
  @Operation(summary = "Update applications configuration", description = "Updates the applications configuration with the provided settings.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configuration successfully updated"),
      @ApiResponse(responseCode = "400", description = "Invalid request body"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response updateConfig(Apps.AppsConfigDto configDto) {
    appsService.updateAppsConfig(Dtos.fromDto(configDto));
    return Response.ok().build();
  }

  @DELETE
  @Path("/config")
  @Operation(summary = "Reset applications configuration", description = "Resets the applications configuration to default values.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Configuration successfully reset"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response resetConfig() {
    appsService.resetConfig();
    return Response.ok().build();
  }

  /**
   * Self registration.
   *
   * @param servletRequest
   * @param appDto
   * @return
   */
  @POST
  @NotAuthenticated
  @Operation(summary = "Register application", description = "Registers a new application with self-registration using authentication token.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Application successfully registered"),
      @ApiResponse(responseCode = "400", description = "Invalid request body or authentication token"),
      @ApiResponse(responseCode = "403", description = "Self-registration not allowed or token invalid"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response registerApp(@Context HttpServletRequest servletRequest, Apps.AppDto appDto) {
    appsService.checkToken(servletRequest.getHeader(APP_AUTH_HEADER));
    appsService.checkSelfRegistrationRules(appDto.getServer());
    appsService.registerApp(Dtos.fromDto(appDto));
    return Response.ok().build();
  }

  /**
   * Self unregistration.
   *
   * @param servletRequest
   * @param appDto
   * @return
   */
  @DELETE
  @NotAuthenticated
  @Operation(summary = "Unregister application", description = "Unregisters an application with self-unregistration using authentication token.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Application successfully unregistered"),
      @ApiResponse(responseCode = "400", description = "Invalid request body or authentication token"),
      @ApiResponse(responseCode = "403", description = "Token invalid or application not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response unRegisterApp(@Context HttpServletRequest servletRequest, Apps.AppDto appDto) {
    appsService.checkToken(servletRequest.getHeader(APP_AUTH_HEADER));
    appsService.unregisterApp(Dtos.fromDto(appDto));
    return Response.ok().build();
  }
}
