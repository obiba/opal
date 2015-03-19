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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
import org.obiba.opal.web.ws.SortDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("request")
@Path("/datasources/variables")
public class DatasourcesVariablesSearchResource extends AbstractSearchUtility {

  private static final Logger log = LoggerFactory.getLogger(DatasourcesVariablesSearchResource.class);

  @GET
  @POST
  @Path("_search")
  @Transactional(readOnly = true)
  public Response search(@QueryParam("query") String query, @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("10") int limit,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("field") List<String> fields,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("facet") List<String> facets) {

    try {
      if(!searchServiceAvailable()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
      QuerySearchJsonBuilder jsonBuilder = //
          buildQuerySearch(query, offset, limit, fields, facets, DEFAULT_SORT_FIELD, SortDir.DESC.toString());

      Search.QueryResultDto dtoResponse = convertResponse(executeQuery(jsonBuilder.build()));
      return Response.ok().entity(dtoResponse).build();
    } catch(Exception e) {
      log.error("Unable to perform variables search", e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  protected String getSearchPath() {
    return variablesIndexManager.getName();
  }

  @Override
  protected QuerySearchJsonBuilder buildQuerySearch(String query, int offset, int limit, Collection<String> fields,
      Collection<String> facets, String sortField, String sortDir) {
    return super.buildQuerySearch(query, offset, limit, fields, facets, sortField, sortDir).setFilterTypes(getFilterTypes());
  }

  //
  // Private members
  //

  private Collection<String> getFilterTypes() {
    Collection<String> types = new ArrayList<>();

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        types.add(variablesIndexManager.getIndex(valueTable).getIndexName());
      }
    }

    return types;
  }

}
