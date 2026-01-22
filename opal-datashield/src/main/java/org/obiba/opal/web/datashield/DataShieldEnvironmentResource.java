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
import java.util.List;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.web.model.DataShield;

public interface DataShieldEnvironmentResource {

  void setMethodType(DSMethodType methodType);

  @GET
  @Path("/methods")
  @Operation(summary = "Get DataShield methods", description = "Retrieve all DataShield methods for a specific profile")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Methods retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid profile"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<DataShield.DataShieldMethodDto> getDataShieldMethods(@QueryParam("profile") String profile);

  @DELETE
  @Path("/methods")
  @Operation(summary = "Delete DataShield methods", description = "Delete specified DataShield methods for a specific profile")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Methods deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid profile or method names"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteDataShieldMethods(@QueryParam("name") List<String> names, @QueryParam("profile") String profile);

  @POST
  @Path("/methods")
  @Operation(summary = "Create DataShield method", description = "Create a new DataShield method")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Method created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid method data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response createDataShieldMethod(@Context UriInfo uri, @QueryParam("profile") String profile, DataShield.DataShieldMethodDto dto);

  @GET
  @Path("/method/{name}")
  @Operation(summary = "Get DataShield method", description = "Retrieve a specific DataShield method")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Method retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Method not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile);

  @PUT
  @Path("/method/{name}")
  @Operation(summary = "Update DataShield method", description = "Update an existing DataShield method")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Method updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid method data"),
    @ApiResponse(responseCode = "404", description = "Method not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile, DataShield.DataShieldMethodDto dto);

  @DELETE
  @Path("/method/{name}")
  @Operation(summary = "Delete DataShield method", description = "Delete a specific DataShield method")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Method deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Method not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteDataShieldMethod(@PathParam("name") String name, @QueryParam("profile") String profile);
}
