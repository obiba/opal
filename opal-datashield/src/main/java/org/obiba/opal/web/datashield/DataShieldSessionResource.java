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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.obiba.datashield.r.expr.ParseException;
import org.obiba.opal.web.r.RSessionResource;

public interface DataShieldSessionResource extends RSessionResource {

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  @Operation(summary = "Aggregate DataShield expression as binary", description = "Execute DataShield aggregate expression and return binary results")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Aggregation completed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid DataShield expression"),
    @ApiResponse(responseCode = "500", description = "Server error during aggregation")
  })
  Response aggregateBinary(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException;

  @POST
  @Path("/aggregate")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Aggregate DataShield expression as JSON", description = "Execute DataShield aggregate expression and return JSON results")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Aggregation completed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid DataShield expression"),
    @ApiResponse(responseCode = "500", description = "Server error during aggregation")
  })
  Response aggregateJSON(@QueryParam("async") @DefaultValue("false") boolean async, String body) throws ParseException;

}
