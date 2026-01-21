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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.service.VariablesIndexManager;
import org.obiba.opal.search.service.support.ItemResultDtoStrategy;
import org.obiba.opal.web.model.Search;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Component
@Scope("request")
@Path("/datasource/{ds}/table/{table}/variables/_search")
@Tag(name = "Datasources", description = "Operations on datasources")
@Tag(name = "Search", description = "Operations on the search service")
public class TableVariablesSearchResource extends AbstractSearchUtility {

  @PathParam("ds")
  private String datasource;

  @PathParam("table")
  private String table;

  @GET
  @Transactional(readOnly = true)
  @Operation(summary = "Search variables in table", description = "Search for variables within a specific datasource table using Elasticsearch query syntax. Supports pagination, sorting, field selection, and faceted search.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Search results successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid search query or parameters"),
      @ApiResponse(responseCode = "404", description = "Table not found or not indexed"),
      @ApiResponse(responseCode = "503", description = "Search service unavailable")
  })
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public Response search(@QueryParam("query") String query, @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("lastDoc") String lastDoc, @QueryParam("variable") @DefaultValue("false") boolean addVariableDto,
                         @QueryParam("field") List<String> fields, @QueryParam("facet") List<String> facets,
                         @QueryParam("sortField") String sortField, @QueryParam("sortDir") String sortDir) {

    try {
      if (!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
      if (!opalSearchService.getVariablesIndexManager().hasIndex(getValueTable()))
        return Response.status(Response.Status.NOT_FOUND).build();
      String esQuery = "reference:\"" + getValueTable().getTableReference() + "\" AND " + query;
      Search.QueryResultDto dtoResponse = opalSearchService.executeQuery(buildQuerySearch(esQuery, lastDoc, limit, fields, facets, sortField, sortDir),
        getSearchPath(),
        addVariableDto ? new ItemResultDtoStrategy(getValueTable()) : null);
      return Response.ok().entity(dtoResponse).build();
    } catch (NoSuchValueSetException | NoSuchDatasourceException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
  }

  @Override
  protected String getSearchPath() {
    VariablesIndexManager manager = opalSearchService.getVariablesIndexManager();
    return manager.getName() + "/" + manager.getIndex(getValueTable()).getIndexType();
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getVariablesIndexManager().isReady() &&
      opalSearchService.getVariablesIndexManager().isIndexUpToDate(getValueTable());
  }

  private ValueTable getValueTable() {
    return MagmaEngine.get().getDatasource(datasource).getValueTable(table);
  }
}
