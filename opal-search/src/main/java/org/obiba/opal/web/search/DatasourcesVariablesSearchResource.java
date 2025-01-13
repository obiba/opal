/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
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

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.service.QuerySettings;
import org.obiba.opal.web.model.Search;
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
  @Path("_search")
  @Transactional(readOnly = true)
  public Response search(@QueryParam("query") String query, @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("lastDoc") String lastDoc, @QueryParam("sort") List<String> sorts, @QueryParam("order") String order,
                         @SuppressWarnings("TypeMayBeWeakened") @QueryParam("field") List<String> fields,
                         @SuppressWarnings("TypeMayBeWeakened") @QueryParam("facet") List<String> facets) {

    try {
      if (!searchServiceAvailable()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
     String defaultOrder = Strings.isNullOrEmpty(order) ? SortDir.ASC.toString() : order;
      List<String> sortsWithOrder = Lists.newArrayList();
      if (sorts == null || sorts.isEmpty()) {
        sortsWithOrder.add(DEFAULT_SORT_FIELD + ":desc");
      } else {
        for (String sortWithOrder : sorts) {
          String[] tokens = sortWithOrder.split(":");
          String sortField = tokens[0];
          String sortOrder = tokens.length == 1 || Strings.isNullOrEmpty(tokens[1]) ? defaultOrder : tokens[1];
          sortsWithOrder.add(sortField + ":" + sortOrder);
        }
      }
      Search.QueryResultDto dtoResponse = opalSearchService.executeQuery(buildQuerySearch(query, lastDoc, limit, fields, facets, sortsWithOrder),
        getSearchPath(), null);
      return Response.ok().entity(dtoResponse).build();
    } catch (Exception e) {
      log.error("Unable to perform variables search", e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @GET
  @Path("_count")
  @Transactional(readOnly = true)
  public Response count(@QueryParam("query") String query) {

    try {
      if (!searchServiceAvailable()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
      Search.QueryCountDto queryCountDto = opalSearchService.executeCount(buildQuerySearch(query, null, 0, null, null, null), getSearchPath());
      return Response.ok().entity(queryCountDto).build();
    } catch (Exception e) {
      log.error("Unable to perform variables search", e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getVariablesIndexManager().getName();
  }

  @Override
  protected QuerySettings buildQuerySearch(String query, String lastDoc, int limit, Collection<String> fields,
                                           Collection<String> facets, List<String> sortWithOrder) {
    return super.buildQuerySearch(query, lastDoc, limit, fields, facets, sortWithOrder).filterReferences(getTableReferencesFilter());
  }

  //
  // Private members
  //

  private Collection<String> getTableReferencesFilter() {
    Collection<String> references = new ArrayList<>();
    for (Datasource datasource : MagmaEngine.get().getDatasources()) {
      for (ValueTable valueTable : datasource.getValueTables()) {
        references.add(valueTable.getTableReference());
      }
    }
    return references;
  }

}
