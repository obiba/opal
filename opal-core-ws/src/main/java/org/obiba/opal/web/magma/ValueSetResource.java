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
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Set;

public interface ValueSetResource {

  void setEntity(@NotNull VariableEntity entity);

  void setVariableValueSource(@Nullable VariableValueSource vvs);

  void setLocales(Set<Locale> locales);

  void setValueTable(ValueTable valueTable);

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   *
   * @param select script for filtering the variables
   * @return
   */
  @GET
  @Operation(summary = "Get value set", description = "Retrieve a value set with optional variable filtering")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Value set retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
    @ApiResponse(responseCode = "404", description = "Value set not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getValueSet(@Context UriInfo uriInfo, @QueryParam("select") String select,
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary);

  /**
   * Remove this value set from its table.
   *
   * @return
   */
  @DELETE
  @Operation(summary = "Delete value set", description = "Remove this value set from its table")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Value set deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Value set not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response drop();

  /**
   * Get a value, optionally providing the position (start at 0) of the value in the case of a value sequence.
   */
  @GET
  @Path("/value")
  @Operation(summary = "Get value", description = "Get a specific value from the value set, optionally by position")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Value retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid position"),
    @ApiResponse(responseCode = "404", description = "Value not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response getValue(@QueryParam("pos") Integer pos);
}
