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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Magma.LinkDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.obiba.opal.web.model.Magma.VariableDto.Builder;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.AuthorizeResource;

import com.google.common.base.Function;
import com.google.common.base.Functions;
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
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response getVariables(@Context final UriInfo uriInfo, @QueryParam("script") String script, @QueryParam("offset") @DefaultValue("0") Integer offset, @QueryParam("limit") Integer limit) {
    if(offset < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "offset", String.valueOf(limit));
    }
    if(limit != null && limit < 0) {
      throw new InvalidRequestException("IllegalParameterValue", "limit", String.valueOf(limit));
    }

    final UriBuilder ub = tableUriBuilder(uriInfo);
    String tableUri = ub.build().toString();
    ub.path(TableResource.class, "getVariable");

    LinkDto.Builder tableLinkBuilder = LinkDto.newBuilder().setLink(tableUri).setRel(getValueTable().getName());

    Iterable<Variable> variables = filterVariables(script, offset, limit);
    Iterable<VariableDto> entity = Iterables.transform(variables, Functions.compose(new Function<VariableDto.Builder, VariableDto>() {

      @Override
      public VariableDto apply(Builder input) {
        return input.setLink(ub.build(input.getName()).toString()).build();
      }
    }, Dtos.asDtoFunc(tableLinkBuilder.build())));

    // The use of "GenericEntity" is required because otherwise JAX-RS can't determine the type using reflection.
    return TimestampedResponses.ok(getValueTable(), new GenericEntity<Iterable<VariableDto>>(entity) {
      // Nothing to implement. Subclassed to keep generic information at runtime.
    }).build();
  }

  @GET
  @Path("/excel")
  @Produces("application/vnd.ms-excel")
  @AuthenticatedByCookie
  @AuthorizeResource
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response getExcelDictionary() throws MagmaRuntimeException, IOException {
    String destinationName = getValueTable().getDatasource().getName() + "." + getValueTable().getName() + "-dictionary";
    ByteArrayOutputStream excelOutput = new ByteArrayOutputStream();
    ExcelDatasource destinationDatasource = new ExcelDatasource(destinationName, excelOutput);

    destinationDatasource.initialise();
    try {
      DatasourceCopier.Builder.newCopier().dontCopyValues().build().copy(getValueTable(), destinationDatasource);
    } finally {
      Disposables.silentlyDispose(destinationDatasource);
    }

    return TimestampedResponses.ok(getValueTable()).entity(excelOutput.toByteArray()).type("application/vnd.ms-excel").header("Content-Disposition", "attachment; filename=\"" + destinationName + ".xlsx\"").build();
  }

  @POST
  public Response addOrUpdateVariables(List<VariableDto> variables) {
    VariableWriter vw = null;
    try {

      // @TODO Check if table can be modified and respond with "IllegalTableModification" (it seems like this cannot be
      // done with the current Magma implementation).

      if(getValueTable().isView() == false) {
        vw = addOrUpdateTableVariables(variables);
      } else {
        return Response.status(Status.BAD_REQUEST).entity(getErrorMessage(Status.BAD_REQUEST, "CannotWriteToView")).build();
      }

      return Response.ok().build();
    } catch(Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(getErrorMessage(Status.INTERNAL_SERVER_ERROR, e.toString())).build();
    } finally {
      StreamUtil.silentSafeClose(vw);
    }
  }

  protected VariableWriter addOrUpdateTableVariables(List<VariableDto> variables) {
    VariableWriter vw = getValueTable().getDatasource().createWriter(getValueTable().getName(), getValueTable().getEntityType()).writeVariables();
    for(VariableDto variable : variables) {
      vw.writeVariable(Dtos.fromDto(variable));
    }
    return vw;
  }

  @Path("/locales")
  public LocalesResource getLocalesResource() {
    return super.getLocalesResource();
  }

  //
  // private methods
  //

  protected ClientErrorDto getErrorMessage(Status responseStatus, String errorStatus) {
    return ClientErrorDto.newBuilder().setCode(responseStatus.getStatusCode()).setStatus(errorStatus).build();
  }

  private UriBuilder tableUriBuilder(UriInfo uriInfo) {
    ArrayList<PathSegment> segments = Lists.newArrayList(uriInfo.getPathSegments());
    segments.remove(segments.size() - 1);
    final UriBuilder ub = UriBuilder.fromPath("/");
    for(PathSegment segment : segments) {
      ub.segment(segment.getPath());
    }
    return ub;
  }

}
