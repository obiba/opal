/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.services;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.domain.sql.SQLExecution;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.web.model.SQL;
import org.obiba.opal.web.sql.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/service/sql")
@Tag(name = "Services", description = "Operations on services")
public class SQLServiceResource {

  @Autowired
  private SQLService sqlService;

  @GET
  @Path("/history")
  @Operation(
    summary = "Get SQL execution history",
    description = "Retrieves the history of SQL executions with optional filtering by user and datasource"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "SQL execution history retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<SQL.SQLExecutionDto> getHistory(@QueryParam("user") String user, @QueryParam("datasource") String datasource,
                                              @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    List<SQLExecution> execs = sqlService.getSQLExecutions(user, datasource);
    return execs.subList(offset, Math.min(execs.size(), offset + limit)).stream()
        .map(Dtos::asDto).collect(Collectors.toList());
  }


}
