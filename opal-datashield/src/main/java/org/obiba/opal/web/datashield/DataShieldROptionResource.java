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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;

public interface DataShieldROptionResource {

  @DELETE
  @Operation(summary = "Delete DataShield R option", description = "Delete a specific DataShield R option")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "R option deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid profile or option name"),
    @ApiResponse(responseCode = "404", description = "Option not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteDataShieldROption(@QueryParam("name") String name, @QueryParam("profile") String profile);

  @POST
  @PUT
  @Operation(summary = "Add or update DataShield R option", description = "Create or update a DataShield R option")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R option updated successfully"),
    @ApiResponse(responseCode = "201", description = "R option created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid option data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response addOrUpdateDataShieldROption(@QueryParam("profile") String profile, DataShield.DataShieldROptionDto dto);

  @GET
  @Operation(summary = "Get DataShield R option", description = "Retrieve a specific DataShield R option")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R option retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Option not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getDataShieldROption(@QueryParam("name") String name, @QueryParam("profile") String profile);
}
