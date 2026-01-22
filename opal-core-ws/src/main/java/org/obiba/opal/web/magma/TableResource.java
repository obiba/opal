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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;

public interface TableResource {

  void setLocales(Set<Locale> locales);

  Set<Locale> getLocales();

  void setValueTable(ValueTable valueTable);

  ValueTable getValueTable();

  @GET
  @Operation(summary = "Get table", description = "Retrieve table definition and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Table not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Magma.TableDto get(@Context Request request, @Context UriInfo uriInfo,
      @QueryParam("counts") @DefaultValue("false") Boolean counts);

  @PUT
  @Operation(summary = "Update table", description = "Update table definition and metadata")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Table updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid table data"),
    @ApiResponse(responseCode = "404", description = "Table not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response update(Magma.TableDto table);

  @Path("/variables")
  VariablesResource getVariables();

  /**
   * Get the entities.
   */
  @GET
  @Path("/entities")
  @Operation(summary = "Get table entities", description = "Retrieve all entities in the table")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Entities retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  List<Magma.VariableEntityDto> getEntities();

  /**
   * Get the value set from the given entity identifier, for the variables filtered by the optional 'select' script.
   */
  @Path("/valueSet/{identifier}")
  ValueSetResource getValueSet(@Context Request request, @PathParam("identifier") String identifier,
      @QueryParam("select") String select, @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary);

  /**
   * Get timestamps of a value set.
   */
  @GET
  @Path("/valueSet/{identifier}/timestamps")
  @Operation(summary = "Get value set timestamps", description = "Retrieve timestamps for a specific entity's value set")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Timestamps retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Entity not found"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Magma.TimestampsDto getValueSetTimestamps(@Context Request request, @PathParam("identifier") String identifier);

  /**
   * Get the value set from the given entity identifier and variable.
   */
  @Path("/valueSet/{identifier}/variable/{variable}")
  ValueSetResource getVariableValueSet(@Context Request request, @PathParam("identifier") String identifier,
      @PathParam("variable") String variable, @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary);

  /**
   * Update the value set by importing the data.
   * <p/>
   * This should be /valueSets, but its POST is already implemented in ValueSetsResource due to GET not allowing a body
   *
   * @param valueSetsDto the set of data (entity identifier, variable name and value)
   * @param unitName optional functional unit name
   * @param generateIds ignored if unit name is not specified, otherwise operation will fail if no entity can be found
   * in the given unit and creating a new entity is not allowed
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  @POST
  @Path("/valueSet")
  @Operation(summary = "Update value set", description = "Update value set by importing data with optional entity generation")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Value set updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid data or parameters"),
    @ApiResponse(responseCode = "500", description = "Server error")
  })
  Response updateValueSet(Magma.ValueSetsDto valueSetsDto, //
      @QueryParam("unit") String unitName, //
      @QueryParam("generateIds") @DefaultValue("false") boolean generateIds, //
      @QueryParam("ignoreUnknownIds") @DefaultValue("false") boolean ignoreUnknownIds)
      throws IOException, InterruptedException;

  /**
   * Get Value sets resource for all table variables.
   */
  @Path("/valueSets")
  ValueSetsResource getValueSets(@Context Request request);

  /**
   * Get value sets resource for provided variable.
   */
  @Path("/valueSets/variable/{variable}")
  ValueSetsResource getVariableValueSets(@Context Request request, @PathParam("variable") String name);

  /**
   * Get variable resource.
   */
  @Path("/variable/{variable}")
  VariableResource getVariable(@Context Request request, @PathParam("variable") String name);

  /**
   * Get transient derived variable.
   */
  @Path("/variable/_transient")
  VariableResource getTransientVariable(@QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @QueryParam("self") @DefaultValue("true") Boolean self, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP, @FormParam("missingCategory") List<String> missingCategory);

  /**
   * Compile a derived variable script.
   */
  @GET
  @POST
  @Path("/variable/_transient/_compile")
  @Operation(summary = "Compile transient variable", description = "Compile derived variable script for validation")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Script compiled successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid script or parameters"),
    @ApiResponse(responseCode = "500", description = "Server error during compilation")
  })
  Response compileTransientVariable(@QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @QueryParam("self") @DefaultValue("true") Boolean self,
      @FormParam("script") String scriptFP, @FormParam("category") List<String> categoriesFP);

  /**
   * Get value sets resource for the transient derived variable.
   */
  @Path("/valueSets/variable/_transient")
  ValueSetsResource getTransientVariableValueSets(@QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @QueryParam("self") @DefaultValue("true") Boolean self, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP);

  /**
   * Get value set resource for the transient derived variable and entity.
   */
  @Path("/valueSet/{identifier}/variable/_transient")
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "MethodWithTooManyParameters" })
  ValueSetResource getTransientVariableValueSet( //
      @Context Request request, //
      @PathParam("identifier") String identifier, //
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary, //
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName, //
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, //
      @QueryParam("script") String scriptQP, //
      @QueryParam("category") List<String> categoriesQP, //
      @QueryParam("self") @DefaultValue("true") Boolean self, //
      @FormParam("script") String scriptFP, //
      @FormParam("category") List<String> categoriesFP);

  @Path("/compare")
  CompareResource getTableCompare();

  @Path("/locales")
  LocalesResource getLocalesResource();
}
