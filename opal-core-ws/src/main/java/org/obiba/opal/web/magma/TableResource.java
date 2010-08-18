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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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

import org.json.JSONObject;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.VariableEntityBean;
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
  public Iterable<VariableDto> getVariables(@Context final UriInfo uriInfo) {
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

    ArrayList<VariableDto> variables = Lists.newArrayList(Iterables.transform(valueTable.getVariables(), Dtos.asDtoFunc(tableLinkBuilder.build(), ub)));
    sortByName(variables);

    return variables;
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
  public Set<VariableEntityDto> getEntities() {
    return ImmutableSet.copyOf(Iterables.transform(valueTable.getVariableEntities(), new Function<VariableEntity, VariableEntityDto>() {
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
  @Path("/values.json")
  @Produces("application/json")
  public Response getValuesAsJson(@QueryParam("v") List<String> variables) {
    return Response.ok(new JSONObject(readValues(variables)).toString()).build();
  }

  @GET
  @Path("/values.xml")
  @Produces("application/xml")
  public Map<String, List<Object>> getValuesAsXml(@QueryParam("v") List<String> variables) {
    return readValues(variables);
  }

  @Path("/variable/{variable}")
  public VariableResource getVariable(@PathParam("variable") String name) {
    return getVariableResource(valueTable.getVariableValueSource(name));
  }

  @Bean
  @Scope("request")
  public VariableResource getVariableResource(VariableValueSource source) {
    return new VariableResource(this.valueTable, source);
  }

  private Map<String, List<Object>> readValues(List<String> variables) {
    Map<String, List<Object>> response = new LinkedHashMap<String, List<Object>>();

    if(variables == null || variables.size() == 0) {
      variables = ImmutableList.copyOf(Iterables.transform(valueTable.getVariables(), new Function<Variable, String>() {
        @Override
        public String apply(Variable from) {
          return from.getName();
        }
      }));
    }

    for(String name : variables) {
      response.put(name, new LinkedList<Object>());
    }

    for(ValueSet vs : valueTable.getValueSets()) {
      for(Map.Entry<String, List<Object>> entry : response.entrySet()) {
        entry.getValue().add(valueTable.getVariableValueSource(entry.getKey()).getValue(vs).getValue());
      }
    }
    return response;
  }

  @PUT
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

  @Path("/compare")
  @Bean
  public CompareResource getTableCompare() {
    return new CompareResource(valueTable);
  }

}
