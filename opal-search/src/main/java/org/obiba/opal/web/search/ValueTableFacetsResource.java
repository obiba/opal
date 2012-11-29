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
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facets")

/**
 * Elastic Search API resource that provides a secure mechanism of performing queries on the indexes without exposing
 * individual data,
 */
public class ValueTableFacetsResource {

  private final IndexManager indexManager;

  private final ElasticSearchProvider esProvider;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @Autowired
  public ValueTableFacetsResource(IndexManager indexManager, ElasticSearchProvider esProvider) {
    this.indexManager = indexManager;
    this.esProvider = esProvider;
  }

  @POST
  @Path("/_search")
  public Response search(@Context HttpServletRequest servletRequest, Search.QueryTermsDto dtoQueries) {
    Search.QueryResultDto dtoResult = Search.QueryResultDto.newBuilder().build();

    try {
      IndexManagerHelper indexManagerHelper = new IndexManagerHelper(indexManager, datasource, table);
      ElasticSearchQuery esQuery = new ElasticSearchQuery(servletRequest, esProvider);

      dtoResult = esQuery.execute(indexManagerHelper, dtoQueries);

    } catch(UnsupportedOperationException e) {
      return Response.status(501).build();
    } catch(JSONException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().entity(dtoResult).build();
  }
}
