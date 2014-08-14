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

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.IndexManagerHelper;
import org.obiba.opal.web.search.support.SearchQueryExecutorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Elastic Search API resource that provides a secure mechanism of performing queries on the indexes without exposing
 * individual data,
 */
@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/facets")
public class ValueTableFacetsResource {

  @Autowired
  protected ElasticSearchProvider esProvider;

  @Autowired
  private ValuesIndexManager indexManager;

  @Autowired
  private SearchQueryExecutorFactory searchQueryFactory;

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @POST
  @Path("/_search")
  @Transactional(readOnly = true)
  public Response search(Search.QueryTermsDto dtoQueries) {
    if(!esProvider.isEnabled()) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("SearchServiceUnavailable").build();
    }

    try {
      IndexManagerHelper indexManagerHelper = new IndexManagerHelper(indexManager).setDatasource(datasource)
          .setTable(table);
      Search.QueryResultDto dtoResult = searchQueryFactory.create().execute(indexManagerHelper, dtoQueries);
      return Response.ok().entity(dtoResult).build();
    } catch(UnsupportedOperationException e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    } catch(JSONException e) {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    }
  }
}