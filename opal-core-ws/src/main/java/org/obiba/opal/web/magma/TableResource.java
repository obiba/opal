package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Magma;

import com.wordnik.swagger.annotations.ApiOperation;

public interface TableResource {

  void setLocales(Set<Locale> locales);

  Set<Locale> getLocales();

  void setValueTable(ValueTable valueTable);

  ValueTable getValueTable();

  @GET
  @ApiOperation(value = "Get the Table", response = Magma.TableDto.class)
  Response get(@Context Request request, @Context UriInfo uriInfo,
      @QueryParam("counts") @DefaultValue("false") Boolean counts);

  @PUT
  Response update(Magma.TableDto table);

  @Path("/variables")
  VariablesResource getVariables();

  /**
   * Get the entities, optionally filtered by a script.
   */
  @GET
  @Path("/entities")
  Set<Magma.VariableEntityDto> getEntities();

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
  VariableResource getTransientVariable(@QueryParam("name") String name,
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP);

  /**
   * Compile a derived variable script.
   */
  @GET
  @POST
  @Path("/variable/_transient/_compile")
  Response compileTransientVariable(@QueryParam("name") String name,
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP);

  /**
   * Get value sets resource for the transient derived variable.
   */
  @Path("/valueSets/variable/_transient")
  ValueSetsResource getTransientVariableValueSets(@QueryParam("name") String name,
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP);

  /**
   * Get value set resource for the transient derived variable and entity.
   */
  @Path("/valueSet/{identifier}/variable/_transient")
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "MethodWithTooManyParameters" })
  ValueSetResource getTransientVariableValueSet( //
      @Context Request request, //
      @PathParam("identifier") String identifier, //
      @QueryParam("name") String name, //
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary, //
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName, //
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, //
      @QueryParam("script") String scriptQP, //
      @QueryParam("category") List<String> categoriesQP, //
      @FormParam("script") String scriptFP, //
      @FormParam("category") List<String> categoriesFP);

  @Path("/compare")
  CompareResource getTableCompare();

  @Path("/locales")
  LocalesResource getLocalesResource();
}
