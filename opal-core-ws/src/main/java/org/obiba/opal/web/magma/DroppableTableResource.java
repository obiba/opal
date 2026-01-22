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
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.Response;

public interface DroppableTableResource extends TableResource {

  @DELETE
  @Operation(summary = "Drop table", description = "Drop the entire table and all its data")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Table dropped successfully"),
    @ApiResponse(responseCode = "404", description = "Table not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response drop();
}
