/*
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.search;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.obiba.opal.search.IndexManager;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.IndexManagerHelper;
import org.obiba.opal.web.search.support.QueryTermDtoBuilder;
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
@Path("/datasource/{ds}/table/{table}/facet")
public class ValueTableFacetResource {

  private final IndexManager indexManager;

  private final SearchQueryExecutorFactory searchQueryFactory;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public ValueTableFacetResource(IndexManager indexManager, SearchQueryExecutorFactory searchQueryFactory) {
    this.indexManager = indexManager;
    this.searchQueryFactory = searchQueryFactory;
  }

  /**
   * Given a variable name, returns its corresponding facet (terms, statistical). Only categorical and continuous
   * variables are treated.
   *
   * @param servletRequest
   * @param variable
   * @return
   */
  @GET
  @Path("/variable/{variable}/_search")
  public Response search(@PathParam("variable") String variable) {
    Search.QueryResultDto dtoResult = Search.QueryResultDto.newBuilder().setTotalHits(0).build();

    try {
      IndexManagerHelper indexManagerHelper = new IndexManagerHelper(indexManager).setDatasource(datasource).setTable(table);
      QueryTermDtoBuilder dtoBuilder = new QueryTermDtoBuilder("0").variableTermDto(variable);

      dtoResult = searchQueryFactory.create().execute(indexManagerHelper, dtoBuilder.build());

    } catch(UnsupportedOperationException e) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    } catch(JSONException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().entity(dtoResult).build();
  }
}