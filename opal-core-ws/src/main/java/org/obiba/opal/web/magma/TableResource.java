/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.service.ImportService;
import org.obiba.opal.core.service.NoSuchFunctionalUnitException;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueSetsDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class TableResource extends AbstractValueTableResource {

  private final ImportService importService;

  public TableResource(ValueTable valueTable) {
    this(valueTable, Collections.<Locale>emptySet(), null);
  }

  public TableResource(ValueTable valueTable, Set<Locale> locales) {
    this(valueTable, locales, null);
  }

  public TableResource(ValueTable valueTable, Set<Locale> locales, ImportService importService) {
    super(valueTable, locales);
    this.importService = importService;
  }

  @GET
  public TableDto get(@Context UriInfo uriInfo, @QueryParam("counts") @DefaultValue("true") Boolean counts) {
    String path = uriInfo.getPath(false);
    TableDto.Builder builder = Dtos.asDto(getValueTable(), counts).setLink(path);
    if(getValueTable().isView()) {
      builder.setViewLink(path.replaceFirst("table", "view"));
    }
    return builder.build();
  }

  @Path("/variables")
  public VariablesResource getVariables(@Context Request request) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new VariablesResource(getValueTable(), getLocales());
  }

  /**
   * Get the entities, optionally filtered by a script.
   *
   * @param script script for filtering the entities
   * @return
   */
  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities(@QueryParam("script") String script) {
    Iterable<VariableEntity> entities = filterEntities(script);

    return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntity, VariableEntityDto>() {
      @Override
      public VariableEntityDto apply(VariableEntity from) {
        return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build();
      }
    }));
  }

  /**
   * Get the value set from the given entity identifier, for the variables filtered by the optional 'select' script.
   *
   * @param identifier
   * @param select script for filtering the variables
   * @return
   */
  @Path("/valueSet/{identifier}")
  public ValueSetResource getValueSet(@Context Request request, @PathParam("identifier") String identifier,
      @QueryParam("select") String select, @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary) {
    // TODO timestamp is a composite of value set timestamp that fall backs to value table timestamps
    TimestampedResponses.evaluate(request, getValueTable());
    return new ValueSetResource(getValueTable(),
        new VariableEntityBean(this.getValueTable().getEntityType(), identifier));
  }

  /**
   * Get timestamps of a value set.
   * @param request
   * @param identifier
   * @return
   */
  @GET
  @Path("/valueSet/{identifier}/timestamps")
  public Magma.TimestampsDto getValueSetTimestamps(@Context Request request, @PathParam("identifier") String identifier) {
    TimestampedResponses.evaluate(request, getValueTable());
    ValueSet vs = getValueTable().getValueSet(new VariableEntityBean(getValueTable().getEntityType(), identifier));
    return Dtos.asDto(vs.getTimestamps()).build();
  }

  /**
   * Get the value set from the given entity identifier and variable.
   *
   * @param request
   * @param identifier
   * @param variable
   * @param filterBinary
   * @return
   */
  @Path("/valueSet/{identifier}/variable/{variable}")
  public ValueSetResource getVariableValueSet(@Context Request request, @PathParam("identifier") String identifier,
      @PathParam("variable") String variable, @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new ValueSetResource(getValueTable(), getValueTable().getVariableValueSource(variable),
        new VariableEntityBean(this.getValueTable().getEntityType(), identifier));
  }

  /**
   * Update the value set by importing the data.
   *
   * @param valueSetsDto the set of data (entity identifier, variable name and value)
   * @param unitName optional functional unit name
   * @param generateIds ignored if unit name is not specified, otherwise operation will fail if no entity can be found
   * in the given unit and creating a new entity is not allowed
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  // This should be /valueSets, but its POST is already implemented in ValueSetsResource due to GET not allowing a body
  @POST
  @Path("/valueSet")
  public Response updateValueSet(ValueSetsDto valueSetsDto, @QueryParam("unit") String unitName,
      @QueryParam("generateIds") @DefaultValue("false") boolean generateIds,
      @QueryParam("ignoreUnknownIds") @DefaultValue(
          "false") boolean ignoreUnknownIds) throws IOException, InterruptedException {
    ValueTable vt = getValueTable();
    if(vt.getDatasource() == null) {
      return Response.status(Status.BAD_REQUEST).entity(ClientErrorDtos
          .getErrorMessage(Status.BAD_REQUEST, "DatasourceCopierIOException",
              "Cannot write to a table without datasource").build()).build();
    }
    try {
      if(importService == null) {
        writeValueSets(vt.getDatasource().createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
      } else {
        StaticDatasource ds = new StaticDatasource("import");
        // static writers will add entities and variables while writing values
        writeValueSets(ds.createWriter(vt.getName(), valueSetsDto.getEntityType()), valueSetsDto);
        importService
            .importData(unitName, ds.getValueTables(), vt.getDatasource().getName(), generateIds, ignoreUnknownIds);
      }
    } catch(NoSuchFunctionalUnitException ex) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "NoSuchFunctionalUnit", unitName).build())
          .build();
    } catch(RuntimeException ex) {
      return Response.status(Status.BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "DatasourceCopierIOException", ex).build())
          .build();
    }
    return Response.ok().build();
  }

  /**
   * Get Value sets resource for all table variables.
   *
   * @param request
   * @return
   */
  @Path("/valueSets")
  public ValueSetsResource getValueSets(@Context Request request) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new ValueSetsResource(getValueTable());
  }

  /**
   * Get value sets resource for provided variable.
   *
   * @param request
   * @param name
   * @return
   */
  @Path("/valueSets/variable/{variable}")
  public ValueSetsResource getVariableValueSets(@Context Request request, @PathParam("variable") String name) {
    TimestampedResponses.evaluate(request, getValueTable());
    return new ValueSetsResource(getValueTable(), getValueTable().getVariableValueSource(name));
  }

  /**
   * Get variable resource.
   *
   * @param request
   * @param name
   * @return
   */
  @Path("/variable/{variable}")
  public VariableResource getVariable(@Context Request request, @PathParam("variable") String name) {
    TimestampedResponses.evaluate(request, getValueTable());
    return getVariableResource(getValueTable().getVariableValueSource(name));
  }

  /**
   * Get transient derived variable.
   *
   * @param valueTypeName
   * @param repeatable
   * @param scriptQP
   * @param categoriesQP
   * @param scriptFP
   * @param categoriesFP
   * @return
   */
  @Path("/variable/_transient")
  public VariableResource getTransientVariable(@QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP) {
    JavascriptVariableValueSource jvvs = getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP,
        categoriesQP, scriptFP, categoriesFP);
    return getVariableResource(jvvs);
  }

  /**
   * Compile a derived variable script.
   *
   * @param valueTypeName
   * @param repeatable
   * @param scriptQP
   * @param categoriesQP
   * @param scriptFP
   * @param categoriesFP
   * @return
   */
  @GET
  @POST
  @Path("/variable/_transient/_compile")
  public Response compileTransientVariable(@QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP) {
    getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP, categoriesQP, scriptFP, categoriesFP);
    return Response.ok().build();
  }

  /**
   * Get value sets resource for the transient derived variable.
   *
   * @param valueTypeName
   * @param repeatable
   * @param scriptQP
   * @param categoriesQP
   * @param scriptFP
   * @param categoriesFP
   * @return
   */
  @Path("/valueSets/variable/_transient")
  public ValueSetsResource getTransientVariableValueSets(
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP) {
    JavascriptVariableValueSource jvvs = getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP,
        categoriesQP, scriptFP, categoriesFP);
    return new ValueSetsResource(getValueTable(), jvvs);
  }

  /**
   * Get value set resource for the transient derived variable and entity.
   *
   * @param request
   * @param identifier
   * @param filterBinary
   * @param valueTypeName
   * @param repeatable
   * @param scriptQP
   * @param categoriesQP
   * @param scriptFP
   * @param categoriesFP
   * @return
   */
  @Path("/valueSet/{identifier}/variable/_transient")
  @SuppressWarnings({"unchecked", "PMD.ExcessiveParameterList"})
  public ValueSetResource getTransientVariableValueSet(@Context Request request,
      @PathParam("identifier") String identifier,
      @QueryParam("filterBinary") @DefaultValue("true") Boolean filterBinary,
      @QueryParam("valueType") @DefaultValue("text") String valueTypeName,
      @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String scriptQP,
      @QueryParam("category") List<String> categoriesQP, @FormParam("script") String scriptFP,
      @FormParam("category") List<String> categoriesFP) {
    JavascriptVariableValueSource jvvs = getJavascriptVariableValueSource(valueTypeName, repeatable, scriptQP,
        categoriesQP, scriptFP, categoriesFP);
    return new ValueSetResource(getValueTable(), jvvs,
        new VariableEntityBean(this.getValueTable().getEntityType(), identifier));
  }

  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getValueTable());
  }

  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

  private void writeValueSets(ValueTableWriter tableWriter, ValueSetsDto valueSetsDto) {
    try {
      for(ValueSetsDto.ValueSetDto valueSetDto : valueSetsDto.getValueSetsList()) {
        VariableEntity entity = new VariableEntityBean(valueSetsDto.getEntityType(), valueSetDto.getIdentifier());
        ValueSetWriter writer = tableWriter.writeValueSet(entity);
        try {
          for(int i = 0; i < valueSetsDto.getVariablesCount(); i++) {
            Variable variable = getValueTable().getVariable(valueSetsDto.getVariables(i));
            Value value = Dtos.fromDto(valueSetDto.getValues(i), variable.getValueType(), variable.isRepeatable());
            writer.writeValue(variable, value);
          }
        } finally {
          Closeables.closeQuietly(writer);
        }
      }
    } finally {
      Closeables.closeQuietly(tableWriter);
    }
  }

  private ValueType resolveValueType(String valueTypeName) {
    ValueType valueType = null;
    try {
      valueType = ValueType.Factory.forName(valueTypeName);
    } catch(IllegalArgumentException ex) {
      throw new InvalidRequestException("IllegalParameterValue", "valueType", valueTypeName);
    }
    return valueType;
  }

  private VariableResource getVariableResource(VariableValueSource source) {
    return new VariableResource(this.getValueTable(), source);
  }

  private JavascriptVariableValueSource getJavascriptVariableValueSource(String valueTypeName, Boolean repeatable,
      String scriptQP, List<String> categoriesQP, String scriptFP, List<String> categoriesFP) {
    String script = scriptQP;
    List<String> categories = categoriesQP;
    if(script == null || script.equals("")) {
      script = scriptFP;
    }
    if(script == null || script.equals("")) {
      script = "null";
    }
    if(categories == null || categories.isEmpty()) {
      categories = categoriesFP;
    }
    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script,
        categories == null ? ImmutableList.<String>of() : categories);
    JavascriptVariableValueSource jvvs = new JavascriptVariableValueSource(transientVariable, getValueTable());
    jvvs.initialise();
    return jvvs;
  }

  private Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script,
      List<String> categories) {
    Variable.Builder builder = new Variable.Builder("_transient", valueType, getValueTable().getEntityType())
        .extend(JavascriptVariableBuilder.class).setScript(script);

    if(repeatable) {
      builder.repeatable();
    }
    builder.addCategories(categories.toArray(new String[] {}));

    return builder.build();
  }

}
