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
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.web.magma.math.SummaryResource;
import org.obiba.opal.web.model.Magma;

public interface VariableResource {

  void setName(String name);

  void setValueTable(ValueTable valueTable);

  void setVariableValueSource(VariableValueSource variableValueSource);

  @GET
  @Operation(summary = "Get variable", description = "Retrieve variable details and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variable retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Variable not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Magma.VariableDto get(@Context UriInfo uriInfo);

  @PUT
  @Operation(summary = "Update variable", description = "Update variable definition and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Variable updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid variable data"),
    @ApiResponse(responseCode = "404", description = "Variable not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateVariable(Magma.VariableDto variable);

  @DELETE
  @Operation(summary = "Delete variable", description = "Delete variable from the table")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Variable deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Variable not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteVariable();

  @PUT
  @Path("/attribute/{name}")
  @Operation(summary = "Update variable attribute", description = "Update or add an attribute to the variable")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Attribute updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid attribute data"),
    @ApiResponse(responseCode = "404", description = "Variable not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateVariableAttribute(@PathParam("name") String name, @QueryParam("namespace") String namespace, @QueryParam("locale") String locale, @QueryParam("value") String value);

  @DELETE
  @Path("/attribute/{name}")
  @Operation(summary = "Delete variable attribute", description = "Delete an attribute from the variable")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Attribute deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Variable or attribute not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response deleteVariableAttribute(@PathParam("name") String name, @QueryParam("namespace") String namespace, @QueryParam("locale") String locale);

  @Path("/summary")
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  SummaryResource getSummary(@Context UriInfo uriInfo, @Context Request request, @QueryParam("nature") String natureStr);

  VariableValueSource getVariableValueSource();
}
