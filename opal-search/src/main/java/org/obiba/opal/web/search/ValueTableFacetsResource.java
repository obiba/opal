/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.IndexManagerHelper;
import org.obiba.opal.web.search.support.SearchQueryExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Elastic Search API resource that provides a secure mechanism of performing queries on the indexes without exposing
 * individual data,
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facets")
public class ValueTableFacetsResource {

  private final IndexManager indexManager;

  private final SearchQueryExecutorFactory searchQueryFactory;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public ValueTableFacetsResource(IndexManager indexManager, SearchQueryExecutorFactory searchQueryFactory) {
    this.indexManager = indexManager;
    this.searchQueryFactory = searchQueryFactory;
  }

  @POST
  @Path("/_search")
  public Response search(@Context HttpServletRequest servletRequest, Search.QueryTermsDto dtoQueries) {
    Search.QueryResultDto dtoResult = Search.QueryResultDto.newBuilder().setTotalHits(0).build();

    try {
      IndexManagerHelper indexManagerHelper = new IndexManagerHelper(indexManager).setDatasource(datasource).setTable(table);
      dtoResult = searchQueryFactory.create().execute(indexManagerHelper, dtoQueries);

    } catch(UnsupportedOperationException e) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    } catch(JSONException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().entity(dtoResult).build();
  }
}