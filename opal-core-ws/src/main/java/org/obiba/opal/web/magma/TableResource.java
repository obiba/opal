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
import org.obiba.magma.VectorSource;
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
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.TableDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.ValueSetDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableEntityDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class TableResource {

  private final ValueTable valueTable;

  public TableResource(ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @GET
  public TableDto get(@Context final UriInfo uriInfo) {
    return Dtos.asDto(valueTable, null).setLink(uriInfo.getPath()).build();
  }

  @GET
  @Path("/variables/xlsx")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = valueTable.getDatasource().getName() + "." + valueTable.getName() + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
      copier.copy(valueTable, destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }

    return Response.ok(excelOutput.toByteArray(), "application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @GET
  @Path("/variables")
  public Iterable<VariableDto> getVariables(@Context final UriInfo uriInfo, @QueryParam("script") String script) {
    ArrayList<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    segments.remove(segments.size() - 1);
    final UriBuilder ub = uriInfo.getBaseUriBuilder();
    final UriBuilder tableub = uriInfo.getBaseUriBuilder();
    for(PathSegment segment : segments) {
      ub.segment(segment.getPath());
      tableub.segment(segment.getPath());
    }
    ub.path(TableResource.class, "getVariable");
    String tableUri = tableub.build().toString();
    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(valueTable.getName());

    Iterable<Variable> variables = filterVariables(valueTable, script);
    ArrayList<VariableDto> variableDtos = Lists.newArrayList(Iterables.transform(variables, Dtos.asDtoFunc(tableLinkBuilder.build(), ub)));
    sortByName(variableDtos);

    return variableDtos;
  }

  @GET
  @Path("variables/query")
  public Iterable<ValueDto> getVariablesQuery(@QueryParam("script") String script) {
    if(script == null) {
      throw new InvalidRequestException("RequiredParameter", "script");
    }

    Iterable<Value> values = queryVariables(valueTable, script);
    ArrayList<ValueDto> valueDtos = Lists.newArrayList(Iterables.transform(values, new Function<Value, ValueDto>() {
      public ValueDto apply(Value from) {
        return Dtos.asDto(from).build();
      }
    }));

    return valueDtos;
  }

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
    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(valueTable.getName());

    List<Variable> group = Lists.newArrayList();
    for(Variable var : valueTable.getVariables()) {
      String gp = var.getOccurrenceGroup();
      if(gp != null && gp.equals(occurrenceGroup)) {
        group.add(var);
      }
    }

    ArrayList<VariableDto> variables = Lists.newArrayList(Iterables.transform(group, Dtos.asDtoFunc(tableLinkBuilder.build(), ub)));
    sortByName(variables);

    return variables;
  }

  @GET
  @Path("/entities")
  public Set<VariableEntityDto> getEntities(@QueryParam("script") String script) {
    Iterable<VariableEntity> entities = filterEntities(valueTable, script);

    return ImmutableSet.copyOf(Iterables.transform(entities, new Function<VariableEntity, VariableEntityDto>() {
      @Override
      public VariableEntityDto apply(VariableEntity from) {
        return VariableEntityDto.newBuilder().setIdentifier(from.getIdentifier()).build();
      }
    }));
  }

  @GET
  @Path("/valueSet/{identifier}")
  public ValueSetDto getValueSet(@PathParam("identifier") String identifier) {
    VariableEntity entity = new VariableEntityBean(this.valueTable.getEntityType(), identifier);
    ValueSet valueSet = this.valueTable.getValueSet(entity);
    ValueSetDto.Builder builder = ValueSetDto.newBuilder();
    builder.setEntity(VariableEntityDto.newBuilder().setIdentifier(identifier));
    for(Variable variable : this.valueTable.getVariables()) {
      Value value = this.valueTable.getValue(variable, valueSet);
      builder.addVariables(variable.getName());
      ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(variable.getValueType().getName()).setIsSequence(value.isSequence());
      if(value.isNull() == false) {
        valueBuilder.setValue(value.toString());
      }
      builder.addValues(valueBuilder);
    }
    return builder.build();
  }

  @GET
  @Path("/eval")
  public Collection<ValueDto> eval(@QueryParam("valueType") String valueType, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") int offset, @QueryParam("limit") @DefaultValue("10") int limit) {
    JavascriptValueSource jvs = new JavascriptValueSource(ValueType.Factory.forName(valueType), script) {
      @Override
      protected void enterContext(MagmaContext ctx, Scriptable scope) {
        if(valueTable instanceof ValueTableWrapper) {
          ctx.push(ValueTable.class, ((ValueTableWrapper) valueTable).getWrappedValueTable());
        } else {
          ctx.push(ValueTable.class, valueTable);
        }
      }
    };
    jvs.initialise();

    List<VariableEntity> entities = new ArrayList<VariableEntity>(valueTable.getVariableEntities());
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
    return getVariableResource(valueTable.getVariableValueSource(name));
  }

  @GET
  @Path("/variable/_transient/values")
  public Iterable<ValueDto> getTransientValues(@QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") @DefaultValue("10") Integer limit, @QueryParam("valueType") @DefaultValue("text") String valueTypeName, @QueryParam("repeatable") @DefaultValue("false") Boolean repeatable, @QueryParam("script") String script) {
    if(limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }
    if(script == null) {
      throw new InvalidRequestException("RequiredParameter", "script");
    }

    Variable transientVariable = buildTransientVariable(resolveValueType(valueTypeName), repeatable, script);
    JavascriptVariableValueSource jvvs = new JavascriptVariableValueSource(transientVariable, valueTable);
    jvvs.initialise();
    VectorSource vectorSource = jvvs.asVectorSource();

    List<VariableEntity> entities = new ArrayList<VariableEntity>(valueTable.getVariableEntities());
    int end = Math.min(offset + limit, entities.size());
    Iterable<Value> values = vectorSource.getValues(new TreeSet<VariableEntity>(entities.subList(offset, end)));

    List<ValueDto> valueDtos = new ArrayList<ValueDto>();
    for(Value value : values) {
      valueDtos.add(Dtos.asDto(value).build());
    }

    return valueDtos;
  }

  /**
   * @param valueTypeName
   * @return
   */
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

      vw = valueTable.getDatasource().createWriter(valueTable.getName(), valueTable.getEntityType()).writeVariables();
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
    return new VariableResource(this.valueTable, source);
  }

  @Bean
  @Path("/compare")
  public CompareResource getTableCompare() {
    return new CompareResource(valueTable);
  }

  ValueTable getValueTable() {
    return valueTable;
  }

  private Iterable<Variable> filterVariables(ValueTable valueTable, String script) {
    if(script == null) {
      return valueTable.getVariables();
    }

    JavascriptClause jsClause = new JavascriptClause(script);
    jsClause.initialise();

    List<Variable> filteredVariables = new ArrayList<Variable>();
    for(Variable variable : valueTable.getVariables()) {
      if(jsClause.select(variable)) {
        filteredVariables.add(variable);
      }
    }

    return filteredVariables;
  }

  private Iterable<Value> queryVariables(ValueTable valueTable, String script) {
    JavascriptClause jsClause = new JavascriptClause(script);
    jsClause.initialise();

    List<Value> values = new ArrayList<Value>();
    for(Variable variable : valueTable.getVariables()) {
      values.add(jsClause.query(variable));
    }

    return values;
  }

  private Iterable<VariableEntity> filterEntities(ValueTable valueTable, String script) {
    if(script == null) {
      return valueTable.getVariableEntities();
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

  private Variable buildTransientVariable(ValueType valueType, boolean repeatable, String script) {
    Variable.Builder builder = new Variable.Builder("transient", valueType, valueTable.getEntityType()).extend(JavascriptVariableBuilder.class).setScript(script);

    if(repeatable) {
      builder.repeatable();
    }

    return builder.build();
  }

  private ClientErrorDto getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private void sortByName(List<Magma.VariableDto> variables) {
    // sort alphabetically
    Collections.sort(variables, new Comparator<Magma.VariableDto>() {

      @Override
      public int compare(VariableDto v1, VariableDto v2) {
        return v1.getName().compareTo(v2.getName());
      }

    });
  }
}
