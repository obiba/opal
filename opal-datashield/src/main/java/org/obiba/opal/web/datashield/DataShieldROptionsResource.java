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

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.DataShield;

public interface DataShieldROptionsResource {

  @GET
  @Operation(summary = "Get DataShield R options", description = "Retrieve all DataShield R options for a specific profile")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "R options retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid profile"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<DataShield.DataShieldROptionDto> getDataShieldROptions(@QueryParam("profile") String profile);

  @DELETE
  @Operation(summary = "Delete DataShield R options", description = "Delete specified DataShield R options for a specific profile")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "R options deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid profile or option names"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteDataShieldROptions(@QueryParam("name") List<String> names, @QueryParam("profile") String profile);

}
