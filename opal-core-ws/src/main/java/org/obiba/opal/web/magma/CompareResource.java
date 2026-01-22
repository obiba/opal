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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;

public interface CompareResource {

  void setComparedDatasource(Datasource comparedDatasource);

  void setComparedTable(ValueTable comparedTable);

  @GET
  @Path("/{with}")
  @Operation(summary = "Compare table", description = "Compare table with another table or view")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Comparison completed successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid comparison parameters"),
    @ApiResponse(responseCode = "404", description = "Table to compare with not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response compare(@PathParam("with") String with, @QueryParam("merge") @DefaultValue("false") boolean merge);
}
