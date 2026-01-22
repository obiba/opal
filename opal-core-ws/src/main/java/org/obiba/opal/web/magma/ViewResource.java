/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

public interface ViewResource extends TableResource {

  @GET
  @Operation(summary = "Get view", description = "Retrieve view definition and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "View retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "View not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Magma.ViewDto getView();

  @PUT
  @Operation(summary = "Update view", description = "Update view definition and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "View updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid view data"),
    @ApiResponse(responseCode = "404", description = "View not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateView(Magma.ViewDto viewDto, @Nullable @QueryParam("comment") String comment);

  @DELETE
  @Operation(summary = "Remove view", description = "Remove the view from the system")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "View removed successfully"),
    @ApiResponse(responseCode = "404", description = "View not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response removeView();

  @PUT
  @Path("/_init")
  @Operation(summary = "Initialize view", description = "Initialize the view structure")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "View initialized successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response initView();

  @GET
  @Path("/xml")
  @Produces("application/xml")
  @Operation(summary = "Download view definition", description = "Download view definition as XML file")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "XML definition downloaded successfully"),
    @ApiResponse(responseCode = "404", description = "View not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response downloadViewDefinition();

  @Path("/from")
  @Bean
  @Scope("request")
  TableResource getFrom();

  /**
   * Get variable resource.
   *
   * @param name
   * @return
   */
  @Path("/variable/{variable}")
  VariableViewResource getVariable(@PathParam("variable") String name);
}
