/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.spi.search.QuerySettings;
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
  public Response search(@QueryParam("query") String query, @QueryParam("offset") @DefaultValue("0") int offset,
      @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("sort") List<String> sorts, @QueryParam("order") String order,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("field") List<String> fields,
      @SuppressWarnings("TypeMayBeWeakened") @QueryParam("facet") List<String> facets) {

    try {
      if(!searchServiceAvailable()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
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
      Search.QueryResultDto dtoResponse = opalSearchService.executeQuery(buildQuerySearch(query, offset, limit, fields, facets, sortsWithOrder),
          getSearchPath(), null);
      return Response.ok().entity(dtoResponse).build();
    } catch(Exception e) {
      log.error("Unable to perform variables search", e);
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getVariablesIndexManager().getName();
  }

  @Override
  protected QuerySettings buildQuerySearch(String query, int offset, int limit, Collection<String> fields,
                                           Collection<String> facets, List<String> sortWithOrder) {
    return super.buildQuerySearch(query, offset, limit, fields, facets, sortWithOrder).filterReferences(getTableReferencesFilter());
  }

  //
  // Private members
  //

  private Collection<String> getTableReferencesFilter() {
    Collection<String> references = new ArrayList<>();
    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(ValueTable valueTable : datasource.getValueTables()) {
        references.add(valueTable.getTableReference());
      }
    }
    return references;
  }

}
