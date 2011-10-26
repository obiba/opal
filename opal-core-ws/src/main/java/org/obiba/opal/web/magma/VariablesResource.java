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
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class VariablesResource extends AbstractValueTableResource {

  public VariablesResource(ValueTable valueTable, Set<Locale> locales) {
    super(valueTable, locales);
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
  public Iterable<VariableDto> getVariables(@Context final UriInfo uriInfo, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") Integer limit) {
    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    UriBuilder ub = tableUriBuilder(uriInfo);
    String tableUri = ub.build().toString();
    ub.path(TableResource.class, "getVariable");

    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    Iterable<Variable> variables = filterVariables(script, offset, limit);
    return Iterables.transform(variables, Dtos.asDtoFunc(tableLinkBuilder.build(), ub));
  }

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  @AuthorizeResource
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

  @GET
  @Path("/query")
  @AuthorizeResource
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
  @Path("/occurrenceGroup/{occurrenceGroup}")
  @AuthorizeResource
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

    return Iterables.transform(group, Dtos.asDtoFunc(tableLinkBuilder.build(), ub));
  }

  @POST
  public Response addOrUpdateVariables(List<VariableDto> variables) {
    VariableWriter vw = null;
    try {

      // @TODO Check if table can be modified and respond with "IllegalTableModification" (it seems like this cannot be
      // done with the current Magma implementation).

      if(getValueTable().isView() == false) {
        vw = getValueTable().getDatasource().createWriter(getValueTable().getName(), getValueTable().getEntityType()).writeVariables();
        for(VariableDto variable : variables) {
          vw.writeVariable(Dtos.fromDto(variable));
        }
      } else {
        // TODO: support this. We'll need to access the ViewManager to persist the modification though.
        return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "CannotWriteToView")).build();
      }

      return Response.ok().build();
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.toString())).build();
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }

  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

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

}
