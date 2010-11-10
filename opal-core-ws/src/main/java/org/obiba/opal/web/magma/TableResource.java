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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.mozilla.javascript.Scriptable;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.js.JavascriptVariableBuilder;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.web.magma.support.DefaultPagingVectorSourceImpl;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.magma.support.PagingVectorSource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Opal.LocaleDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TableResource extends CommonTable {

  public TableResource(ValueTable valueTable) {
    super(valueTable);
  }

  @GET
  public TableDto get(@Context final UriInfo uriInfo) {
    return Dtos.asDto(getValueTable(), null).setLink(uriInfo.getPath()).build();
  }

  @GET
  @Path("/variables/xlsx")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = getValueTable().getDatasource().getName() + "." + getValueTable().getName() + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
      copier.copy(getValueTable(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }

    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  /**
   * Get a chunk of variables, optionally filtered by a script
   * @param uriInfo
   * @param script script for filtering the variables
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @Path("/variables")
  public Iterable<VariableDto> getVariables(@Context final UriInfo uriInfo, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") Integer limit) {
    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    ArrayList<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    segments.remove(segments.size() - 1);
    final UriBuilder ub = UriBuilder.fromPath("/");
    final UriBuilder tableub = UriBuilder.fromPath("/");
    for(PathSegment segment : segments) {
      ub.segment(segment.getPath());
      tableub.segment(segment.getPath());
    }
    ub.path(TableResource.class, "getVariable");
    String tableUri = tableub.build().toString();
    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    Iterable<Variable> variables = filterVariables(getValueTable(), script, offset, limit);
    ArrayList<VariableDto> variableDtos = Lists.newArrayList(Iterables.transform(variables, Dtos.asDtoFunc(tableLinkBuilder.build(), ub)));
    sortVariableDtoByName(variableDtos);

    return variableDtos;
  }

  @GET
  @Path("variables/query")
  public Iterable<ValueDto> getVariablesQuery(@QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") Integer limit) {
    if(script == null) {
      throw new InvalidRequestException("RequiredParameter", "script");
    }
    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    Iterable<Value> values = queryVariables(getValueTable(), script, offset, limit);
    ArrayList<ValueDto> valueDtos = Lists.newArrayList(Iterables.transform(values, new Function<Value, ValueDto>() {
      public ValueDto apply(Value from) {
        return Dtos.asDto(from).build();
      }
    }));

    return valueDtos;
  }

  /**
   * Get the variables in a occurrence group.
   * @param uriInfo
   * @param occurrenceGroup
   * @return
   */
  @GET
  @Path("/variables/occurrenceGroup/{occurrenceGroup}")
  public Iterable<VariableDto> getOccurrenceGroupVariables(@Context final UriInfo uriInfo, @PathParam("occurrenceGroup") String occurrenceGroup) {
    ArrayList<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    final UriBuilder ub = uriInfo.getBaseUriBuilder();
    final UriBuilder tableub = uriInfo.getBaseUriBuilder();
    for(int i = 0; i < segments.size() - 3; i++) {
      PathSegment segment = segments.get(i);
      ub.segment(segment.getPath());
      tableub.segment(segment.getPath());
    }
    ub.path(TableResource.class, "getVariable");
    String tableUri = tableub.build().toString();
    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    List<Variable> group = Lists.newArrayList();
    for(Variable var : getValueTable().getVariables()) {
      String gp = var.getOccurrenceGroup();
      if(gp != null && gp.equals(occurrenceGroup)) {
        group.add(var);
      }
    }

    ArrayList<VariableDto> variables = Lists.newArrayList(Iterables.transform(group, Dtos.asDtoFunc(tableLinkBuilder.build(), ub)));
    sortVariableDtoByName(variables);

    return variables;
  }

  /**
   * Get the entities, optionally filtered by a script.
   * @param script script for filtering the entities
   * @return
   */
  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities(@QueryParam("script") String script) {
    Iterable<VariableEntity> entities = filterEntities(getValueTable(), script);

    return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntity, VariableEntityDto>() {
      @Override
      public VariableEntityDto apply(VariableEntity from) {
        return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build();
      }
    }));
  }

  /**
   * Get the value set from the given entity identifier, for the variables filtered by the optional 'select' script.
   * @param identifier
   * @param select script for filtering the variables
   * @return
   */
  @GET
  @Path("/valueSet/{identifier}")
  public ValueSetDto getValueSet(@PathParam("identifier") String identifier, @QueryParam("select") String select) {
    VariableEntity entity = new VariableEntityBean(this.getValueTable().getEntityType(), identifier);
    Iterable<Variable> variables = filterVariables(getValueTable(), select, 0, null);
    return getValueSet(entity, variables);
  }

  private ValueSetDto getValueSet(VariableEntity entity, Iterable<Variable> variables) {
    ValueSet valueSet = this.getValueTable().getValueSet(entity);
    ValueSetDto.Builder builder = ValueSetDto.newBuilder();
    builder.setEntity(VariableEntityDto.newBuilder().setIdentifier(entity.getIdentifier()));
    for(Variable variable : variables) {
      Value value = this.getValueTable().getValue(variable, valueSet);
      builder.addVariables(variable.getName());
      ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(variable.getValueType().getName()).setIsSequence(value.isSequence());
      if(value.isNull() == false) {
        valueBuilder.setValue(value.toString());
      }
      builder.addValues(valueBuilder);
    }
    return builder.build();
  }

  /**
   * Get a chunk of value sets, optionally filters the variables and/or the entities.
   * @param select script for filtering the variables
   * @param where script for filtering the entities
   * @param offset
   * @param limit
   * @return
   */
  @GET
  @Path("/valueSets")
  public Collection<ValueSetDto> getValueSets(@QueryParam("select") String select, @QueryParam("where") String where, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("100") int limit) {
    Iterable<Variable> variables = filterVariables(getValueTable(), select, 0, null);

    List<VariableEntity> entities;
    if(where != null) {
      entities = getFilteredEntities(getValueTable(), where);
    } else {
      entities = new ArrayList<VariableEntity>(getValueTable().getVariableEntities());
    }
    int end = Math.min(offset + limit, entities.size());

    ImmutableList.Builder<ValueSetDto> dtos = ImmutableList.builder();
    for(VariableEntity entity : entities.subList(offset, end)) {
      dtos.add(getValueSet(entity, variables));
    }
    return dtos.build();
  }

  @GET
  @Path("/eval")
  public Collection<ValueDto> eval(@QueryParam("valueType") String valueType, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("10") int limit) {
    JavascriptValueSource jvs = new JavascriptValueSource(ValueType.Factory.forName(valueType), script) {
      @Override
      protected void enterContext(MagmaContext ctx, Scriptable scope) {
        if(getValueTable() instanceof ValueTableWrapper) {
          ctx.push(ValueTable.class, ((ValueTableWrapper) getValueTable()).getWrappedValueTable());
        } else {
          ctx.push(ValueTable.class, getValueTable());
        }
      }
    };
    jvs.initialise();

    List<VariableEntity> entities = new ArrayList<VariableEntity>(getValueTable().getVariableEntities());
    int end = Math.min(offset + limit, entities.size());
    Iterable<Value> values = jvs.asVectorSource().getValues(new TreeSet<VariableEntity>(entities.subList(offset, end)));

    ImmutableList.Builder<ValueDto> dtos = ImmutableList.builder();
    for(Value value : values) {
      ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(jvs.getValueType().getName()).setIsSequence(value.isSequence());
      if(value.isNull() == false) {
        valueBuilder.setValue(value.toString());
      }
      dtos.add(valueBuilder.build());
    }
    return dtos.build();
  }

  @Path("/variable/{variable}")
  public VariableResource getVariable(@PathParam("variable") String name) {
    return getVariableResource(getValueTable().getVariableValueSource(name));
  }

  @GET
  @Path("/variable/_transient/values")
  public Iterable<ValueDto> getTransientValues(@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") @DefaultValue("10") Integer limit, @QueryParam("valueType") @DefaultValue("text") String valueTypeName, @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String script) {
    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }
    if(script == null) {
      throw new InvalidRequestException("RequiredParameter", "script");
    }

    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script);
    VariableValueSource vvs = getTransientVariableValueSource(transientVariable);

    Iterable<Value> values = getPagingVectorSource(vvs).getValues(offset, limit);

    List<ValueDto> valueDtos = new ArrayList<ValueDto>();
    for(Value value : values) {
      valueDtos.add(Dtos.asDto(value).build());
    }

    return valueDtos;
  }

  VariableValueSource getTransientVariableValueSource(Variable transientVariable) {
    JavascriptVariableValueSource jvvs = new JavascriptVariableValueSource(transientVariable, getValueTable());
    jvvs.initialise();

    return jvvs;
  }

  PagingVectorSource getPagingVectorSource(VariableValueSource vvs) {
    return new DefaultPagingVectorSourceImpl(getValueTable(), vvs);
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

  @POST
  @Path("/variables")
  public Response addOrUpdateVariables(List<VariableDto> variables) {
    VariableWriter vw = null;
    try {

      // @TODO Check if table can be modified and respond with "IllegalTableModification" (it seems like this cannot be
      // done with the current Magma implementation).

      vw = getValueTable().getDatasource().createWriter(getValueTable().getName(), getValueTable().getEntityType()).writeVariables();
      for(VariableDto variable : variables) {
        vw.writeVariable(Dtos.fromDto(variable));
      }

      return Response.ok().build();
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.toString())).build();
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }

  @Bean
  @Scope("request")
  public VariableResource getVariableResource(VariableValueSource source) {
    return new VariableResource(this.getValueTable(), source);
  }

  @Bean
  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(getValueTable());
  }

  @GET
  @Path("/locales")
  public Iterable<LocaleDto> getLocales(@QueryParam("locale") String displayLocale) {
    List<LocaleDto> localeDtos = new ArrayList<LocaleDto>();
    for(Locale locale : getLocales()) {
      localeDtos.add(Dtos.asDto(locale, displayLocale != null ? new Locale(displayLocale) : null));
    }

    return localeDtos;
  }

  private Iterable<Variable> filterVariables(ValueTable valueTable, String script, Integer offset, Integer limit) {
    List<Variable> filteredVariables = null;

    if(script != null) {
      JavascriptClause jsClause = new JavascriptClause(script);
      jsClause.initialise();

      filteredVariables = new ArrayList<Variable>();
      for(Variable variable : getValueTable().getVariables()) {
        if(jsClause.select(variable)) {
          filteredVariables.add(variable);
        }
      }
    } else {
      filteredVariables = Lists.newArrayList(getValueTable().getVariables());
    }

    int fromIndex = (offset < filteredVariables.size()) ? offset : filteredVariables.size();
    int toIndex = (limit != null) ? Math.min(fromIndex + limit, filteredVariables.size()) : filteredVariables.size();

    return filteredVariables.subList(fromIndex, toIndex);
  }

  private Iterable<Value> queryVariables(ValueTable valueTable, String script, Integer offset, Integer limit) {
    JavascriptClause jsClause = new JavascriptClause(script);
    jsClause.initialise();

    List<Variable> variables = Lists.newArrayList(valueTable.getVariables());
    sortVariableByName(variables);

    int fromIndex = (offset < variables.size()) ? offset : variables.size();
    int toIndex = (limit != null) ? Math.min(fromIndex + limit, variables.size()) : variables.size();

    List<Value> values = new ArrayList<Value>();
    for(Variable variable : variables.subList(fromIndex, toIndex)) {
      values.add(jsClause.query(variable));
    }

    return values;
  }

  private Iterable<VariableEntity> filterEntities(ValueTable valueTable, String script) {
    if(script == null) {
      return valueTable.getVariableEntities();
    }

    return getFilteredEntities(valueTable, script);
  }

  private List<VariableEntity> getFilteredEntities(ValueTable valueTable, String script) {
    if(script == null) {
      throw new IllegalArgumentException("Entities filter script cannot be null.");
    }

    JavascriptClause jsClause = new JavascriptClause(script);
    jsClause.initialise();

    List<VariableEntity> filteredEntities = new ArrayList<VariableEntity>();
    for(ValueSet valueSet : valueTable.getValueSets()) {
      if(jsClause.where(valueSet)) {
        filteredEntities.add(valueSet.getVariableEntity());
      }
    }

    return filteredEntities;
  }

  Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script) {
    Variable.Builder builder = new Variable.Builder("transient", valueType, getValueTable().getEntityType()).extend(JavascriptVariableBuilder.class).setScript(script);

    if(repeatable) {
      builder.repeatable();
    }

    return builder.build();
  }

  private ClientErrorDto getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private void sortVariableByName(List<Variable> variables) {
    Collections.sort(variables, new Comparator<Variable>() {

      @Override
      public int compare(Variable v1, Variable v2) {
        return v1.getName().compareTo(v2.getName());
      }

    });
  }

  private void sortVariableDtoByName(List<Magma.VariableDto> variables) {
    // sort alphabetically
    Collections.sort(variables, new Comparator<Magma.VariableDto>() {

      @Override
      public int compare(VariableDto v1, VariableDto v2) {
        return v1.getName().compareTo(v2.getName());
      }

    });
  }

}
