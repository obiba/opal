/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.EsResultConverter;
import org.obiba.opal.web.search.support.ItemResultDtoStrategy;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/variables/_search")
public class TableVariablesSearchResource extends AbstractVariablesSearchResource {

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @GET
  @POST
  @Transactional(readOnly = true)
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public Response search(@QueryParam("query") String query, @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("10") int limit,
      @QueryParam("variable") @DefaultValue("false") boolean addVariableDto, @QueryParam("field") List<String> fields,
      @QueryParam("facet") List<String> facets, @QueryParam("sortField") String sortField,
      @QueryParam("sortDir") String sortDir) {

    try {
      if(!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
      if(!variablesIndexManager.hasIndex(getValueTable())) return Response.status(Response.Status.NOT_FOUND).build();
      QuerySearchJsonBuilder jsonBuiler = buildQuerySearch(query, offset, limit, fields, facets, sortField, sortDir);
      JSONObject jsonResponse = executeQuery(jsonBuiler.build());
      Search.QueryResultDto dtoResponse = convertResponse(jsonResponse, addVariableDto);
      return Response.ok().entity(dtoResponse).build();
    } catch(NoSuchValueSetException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(NoSuchDatasourceException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  protected String getSearchPath() {
    return variablesIndexManager.getIndex(getValueTable()).getRequestPath();
  }

  protected Search.QueryResultDto convertResponse(JSONObject jsonResponse, boolean addVariableDto)
      throws JSONException {
    EsResultConverter converter = new EsResultConverter();
    if(addVariableDto) converter.setStrategy(new ItemResultDtoStrategy(getValueTable()));
    return converter.convert(jsonResponse);
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && variablesIndexManager.isReady() &&
        variablesIndexManager.isIndexUpToDate(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }
}
