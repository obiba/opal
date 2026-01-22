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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/app/{id}")
@Tag(name = "Applications", description = "Operations on registered applications")
public class AppResource {

  @PathParam("id")
  private String id;

  @Autowired
  private AppsService appsService;

  @GET
  @Operation(
    summary = "Get application details",
    description = "Retrieves detailed information about a specific registered application"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Application details retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Application not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Apps.AppDto get() {
    return Dtos.asDto(appsService.getApp(id));
  }

  @DELETE
  @Operation(
    summary = "Unregister application",
    description = "Removes a specific application from the registry"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Application unregistered successfully"),
    @ApiResponse(responseCode = "404", description = "Application not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response unregister() {
    try {
      appsService.unregisterApp(appsService.getApp(id));
    } catch (Exception e) {
      // ignore
    }
    return Response.noContent().build();
  }

}
