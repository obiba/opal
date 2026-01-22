/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.subject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.obiba.opal.web.model.SQL;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import java.util.List;

public interface SQLHistoryResource {

  void setSubject(String subject);

  @GET
  @Operation(summary = "Get SQL history", description = "Retrieve SQL execution history for a subject with pagination")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SQL history retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<SQL.SQLExecutionDto> getSQLHistory(@QueryParam("datasource") String datasource,
                                          @QueryParam("offset") @DefaultValue("0") int offset,
                                          @QueryParam("limit") @DefaultValue("100") int limit);
}
