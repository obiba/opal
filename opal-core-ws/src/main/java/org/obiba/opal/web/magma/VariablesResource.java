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

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface VariablesResource {

  void setValueTable(ValueTable valueTable);

  void setLocales(Set<Locale> locales);

  /**
   * Get a chunk of variables, optionally filtered by a script
   *
   * @param uriInfo
   * @param script  script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @Operation(summary = "Get variables", description = "Retrieve variables from table with optional filtering and pagination")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variables retrieved successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameters"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Iterable<Magma.VariableDto> getVariables(@Context Request request, @Context UriInfo uriInfo, @QueryParam("script") String script,
                                           @QueryParam("offset") @DefaultValue("0") Integer offset, @Nullable @QueryParam("limit") Integer limit);

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  @AuthorizeResource
  @Operation(summary = "Get Excel dictionary", description = "Export variables dictionary as Excel file")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Excel file generated successfully"),
    @ApiResponse(responseCode = "500", description = "Server error during Excel generation")
  })
  Response getExcelDictionary(@Context Request request) throws MagmaRuntimeException, IOException;

  @PUT
  @Path("/_order")
  @Operation(summary = "Set variable order", description = "Set the display order of variables")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variable order updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid variable list"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response setVariableOrder(@QueryParam("variable") List<String> variables);

  /**
   * Batch edition of an attribute in all the specified variables.
   *
   * @param namespace
   * @param name
   * @param locales
   * @param values If null or empty, the attribute is removed.
   * @param action Type of edition: 'apply' (default) or 'delete'.
   * @param variables
   * @return
   */
  @PUT
  @Path("/_attribute")
  @Operation(summary = "Update variable attributes", description = "Batch update attributes in specified variables")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Attributes updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid attribute data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateAttribute(@QueryParam("namespace") String namespace, @QueryParam("name") String name,
                        @QueryParam("locale") List<String> locales, @QueryParam("value") List<String> values,
                        @QueryParam("action") @DefaultValue("apply") String action, @FormParam("variable") List<String> variables);

  /**
   * Batch removal of a specific attribute in all the specified variables.
   * @param namespace
   * @param name
   * @param locale
   * @param value If null or empty, the attribute is removed whatever the value is.
   * @return
   */
  @DELETE
  @Path("/_attribute")
  @Operation(summary = "Delete variable attributes", description = "Batch delete attributes from specified variables")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Attributes deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid attribute data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteAttribute(@QueryParam("namespace") String namespace, @QueryParam("name") String name,
                           @QueryParam("locale") String locale, @QueryParam("value") String value);

  @POST
  @Operation(summary = "Add or update variables", description = "Batch create or update multiple variables")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variables updated successfully"),
    @ApiResponse(responseCode = "201", description = "Variables created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid variable data"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response addOrUpdateVariables(List<Magma.VariableDto> variables, @Nullable @QueryParam("comment") String comment);

  @DELETE
  @Operation(summary = "Delete variables", description = "Batch delete variables from table")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Variables deleted successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid variable list"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteVariables(@QueryParam("variable") List<String> variables);

  @Path("/locales")
  LocalesResource getLocalesResource();
}
