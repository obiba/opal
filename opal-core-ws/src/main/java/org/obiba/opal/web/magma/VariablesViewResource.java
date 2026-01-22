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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;

import jakarta.annotation.Nullable;

public interface VariablesViewResource extends VariablesResource {

  @POST
  @Path("/file")
  @Operation(summary = "Add/update variables from file", description = "Add or update variables in view from file upload")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variables updated successfully"),
    @ApiResponse(responseCode = "201", description = "Variables added successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid file or view data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response addOrUpdateVariablesFromFile(Magma.ViewDto viewDto, @Nullable String comment);

}
