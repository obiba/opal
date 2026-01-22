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

import java.util.Locale;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.web.model.Magma;

import jakarta.annotation.Nullable;

public interface VariableViewResource extends VariableResource {

  void setLocales(Set<Locale> locales);

  @PUT
  @Operation(summary = "Create or update view variable", description = "Create a new variable or update existing variable in view")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variable updated successfully"),
    @ApiResponse(responseCode = "201", description = "Variable created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid variable data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response createOrUpdateVariable(Magma.VariableDto variable, @Nullable @QueryParam("comment") String comment);
}
