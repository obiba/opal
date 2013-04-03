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

import java.util.Collection;
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
import org.obiba.opal.search.VariablesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.EsResultConverter;
import org.obiba.opal.web.search.support.ItemResultDtoStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/variables")
public class TableVariablesSearchResource extends AbstractVariablesSearchResource {

//  private static final Logger log = LoggerFactory.getLogger(TableVariablesSearchResource.class);

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public TableVariablesSearchResource(VariablesIndexManager manager, OpalSearchService opalSearchService,
      ElasticSearchProvider esProvider) {
    super(opalSearchService, esProvider, manager);
  }

  @GET
  @POST
  @Path("_search")
  public Response search(@QueryParam("query") String query, @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("10") int limit,
      @QueryParam("variable") @DefaultValue("false") boolean addVariableDto,
      @QueryParam("field") List<String> fields, @QueryParam("sortField") String sortField,
      @QueryParam("sortDir") String sortDir) {

    try {
      if(!searchServiceAvailable()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
      Search.QueryResultDto dtoResponse = convertResonse(
          executeQuery(query, offset, limit, fields, getFieldSortName(sortField), sortDir), addVariableDto);
      return Response.ok().entity(dtoResponse).build();
    } catch(NoSuchValueSetException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(NoSuchDatasourceException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  //
  // Protected methods
  //

  @Override
  protected String getSearchPath() {
    return indexManager.getIndex(getValueTable()).getRequestPath();
  }

  protected Search.QueryResultDto convertResonse(JSONObject jsonResponse, boolean addVariableDto) throws JSONException {
    EsResultConverter converter = new EsResultConverter();
    if(addVariableDto) converter.setStrategy(new ItemResultDtoStrategy(getValueTable()));
    return converter.convert(jsonResponse);
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }

  protected String getFieldSortName(String field) {
    return indexManager.getIndex(getValueTable()).getFieldSortName(field);
  }
}
