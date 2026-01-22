/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.magma.math;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface CategoricalSummaryResource extends SummaryResource {

  @GET
  @POST // requires POST since request body contains variable info (categories, script, etc)
  @Operation(summary = "Get categorical variable summary", description = "Generate statistical summary for categorical variables")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Summary generated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters"),
    @ApiResponse(responseCode = "500", description = "Server error during summary generation")
  })
  Response get(@QueryParam("distinct") boolean distinct, //
      @QueryParam("offset") Integer offset, //
      @QueryParam("limit") Integer limit, //
      @QueryParam("fullIfCached") @DefaultValue("false") boolean fullIfCached, //
      @QueryParam("resetCache") @DefaultValue("false") boolean resetCache);

}
