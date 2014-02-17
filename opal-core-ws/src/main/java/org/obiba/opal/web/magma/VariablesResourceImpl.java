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
import java.util.List;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableDto.Builder;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.Nullable;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component("variablesResource")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Transactional
public class VariablesResourceImpl extends AbstractValueTableResource implements VariablesResource {

  @Override
  public Response getVariables(Request request, UriInfo uriInfo, String script, Integer offset,
      @Nullable Integer limit) {
    TimestampedResponses.evaluate(request, getValueTable());

    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    final UriBuilder uriBuilder = tableUriBuilder(uriInfo);
    String tableUri = uriBuilder.build().toString();
    uriBuilder.path(TableResource.class, "getVariable");

    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    Iterable<Variable> variables = filterVariables(script, offset, limit);
    Iterable<VariableDto> entity = Iterables
        .transform(variables, Functions.compose(new Function<VariableDto.Builder, VariableDto>() {

          @Override
          public VariableDto apply(Builder input) {
            return input.setLink(uriBuilder.build(input.getName()).toString()).build();
          }
        }, Dtos.asDtoFunc(tableLinkBuilder.build())));

    // The use of "GenericEntity" is required because otherwise JAX-RS can't determine the type using reflection.
    //noinspection EmptyClass
    return TimestampedResponses.ok(getValueTable(), new GenericEntity<Iterable<VariableDto>>(entity) {
      // Nothing to implement. Subclassed to keep generic information at runtime.
    }).build();
  }

  @Override
  public Response getExcelDictionary(Request request) throws MagmaRuntimeException, IOException {
    TimestampedResponses.evaluate(request, getValueTable());
    String destinationName = getValueTable().getDatasource().getName() + "." + getValueTable().getName() +
        "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    Datasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(getValueTable(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }

    return TimestampedResponses.ok(getValueTable()).entity(excelOutput.toByteArray()).type("application/vnd.ms-excel")
        .header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @Override
  public Response addOrUpdateVariables(List<VariableDto> variables, @Nullable String comment) {

    // @TODO Check if table can be modified and respond with "IllegalTableModification"
    // (it seems like this cannot be done with the current Magma implementation).

    if(getValueTable().isView()) {
      return Response.status(BAD_REQUEST).entity(getErrorMessage(BAD_REQUEST, "CannotWriteToView")).build();
    }
    addOrUpdateTableVariables(variables);

    return Response.ok().build();
  }

  void addOrUpdateTableVariables(Iterable<VariableDto> variables) {
    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
        VariableWriter variableWriter = tableWriter.writeVariables()) {
      for(VariableDto variable : variables) {
        variableWriter.writeVariable(Dtos.fromDto(variable));
      }
    }
  }

  @Override
  public Response deleteVariables(List<String> variables) {

    if(getValueTable().isView()) throw new InvalidRequestException("Derived variable must be deleted by the view");

    try(ValueTableWriter tableWriter = getValueTable().getDatasource()
        .createWriter(getValueTable().getName(), getValueTable().getEntityType());
        ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
      for(String name : variables) {
        // The variable must exist
        Variable v = getValueTable().getVariable(name);
        variableWriter.removeVariable(v);
      }
      return Response.ok().build();
    }
  }

  @Override
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

  ClientErrorDto getErrorMessage(Response.StatusType responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private static UriBuilder tableUriBuilder(UriInfo uriInfo) {
    List<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    segments.remove(segments.size() - 1);
    UriBuilder ub = UriBuilder.fromPath("/");
    for(PathSegment segment : segments) {
      ub.segment(segment.getPath());
    }
    return ub;
  }

}
