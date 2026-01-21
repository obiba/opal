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

import com.google.common.base.Strings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.DeprecatedOperationException;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.service.ContingencyService;
import org.obiba.opal.search.service.SearchException;
import org.obiba.opal.web.model.Identifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/datasources/entities")
@Tag(name = "Search", description = "Operations on the search service")
public class DatasourcesEntitiesSearchResource extends AbstractSearchUtility {

  private static final Logger log = LoggerFactory.getLogger(DatasourcesEntitiesSearchResource.class);

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private ContingencyService contingencyService;

  private String entityType;

  @GET
  @Transactional(readOnly = true)
  @Path("_suggest")
  @Operation(summary = "Suggest entity identifiers", description = "Get suggestions for entity identifiers based on partial match. Returns matching identifiers from the identifiers table for the specified entity type.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Identifier suggestions successfully retrieved"),
      @ApiResponse(responseCode = "404", description = "Identifiers table not found for entity type")
  })
  public Identifiers.IdentifierSuggestions search(@QueryParam("query") String query,
                                                  @QueryParam("type") @DefaultValue("Participant") String entityType,
                                                  @QueryParam("limit") @DefaultValue("10") int limit) {
    String queryStr = Strings.isNullOrEmpty(query) ? "" : query.trim();
    Identifiers.IdentifierSuggestions.Builder builder = Identifiers.IdentifierSuggestions.newBuilder()
        .setEntityType(entityType)
        .setLimit(limit)
        .setQuery(queryStr);

    if (!identifiersTableService.hasIdentifiersTable(entityType)) return builder.build();
    List<VariableEntity> entities = identifiersTableService.getIdentifiersTable(entityType).getVariableEntities();

    if ("*".equals(queryStr))
      builder.addAllIdentifiers(entities.stream().map(VariableEntity::getIdentifier)
          .limit(limit).sorted().collect(Collectors.toList()));
    else {
      VariableEntity entity = new VariableEntityBean(entityType, queryStr);
      if (entities.contains(entity)) builder.addIdentifiers(queryStr);
      builder.addAllIdentifiers(entities.stream().map(VariableEntity::getIdentifier)
          .filter(id -> !id.equals(queryStr) && id.startsWith(queryStr)).limit(limit - builder.getIdentifiersCount()).sorted().collect(Collectors.toList()));
      builder.addAllIdentifiers(entities.stream().map(VariableEntity::getIdentifier)
          .filter(id -> !id.startsWith(queryStr) && id.contains(queryStr)).limit(limit - builder.getIdentifiersCount()).sorted().collect(Collectors.toList()));
    }

    return builder.build();
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_search")
  @Operation(summary = "Search entities", description = "Search for entities across datasources. Note: This operation is deprecated since Opal 5 as values are no longer indexed.")
  @ApiResponses({
      @ApiResponse(responseCode = "501", description = "Operation deprecated and not implemented")
  })
  public Response search(@QueryParam("query") String query,
                         @QueryParam("id") String idQuery,
                         @QueryParam("type") @DefaultValue("Participant") String entityType,
                         @QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("counts") @DefaultValue("false") boolean withCounts) throws SearchException {
    throw new DeprecatedOperationException("Unsupported operation: since Opal 5 values are not indexed");
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_count")
  @Operation(summary = "Count entities", description = "Count entities matching search criteria. Note: This operation is deprecated since Opal 5 as values are no longer indexed.")
  @ApiResponses({
      @ApiResponse(responseCode = "501", description = "Operation deprecated and not implemented")
  })
  public Response count(@QueryParam("query") String query,
                        @QueryParam("id") String idQuery,
                        @QueryParam("type") @DefaultValue("Participant") String entityType) throws SearchException {
    return search(query, idQuery, entityType, 0, 0, true);
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_contingency")
  @Operation(summary = "Get contingency table", description = "Generate contingency table for two variables across all datasources.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Contingency table successfully generated"),
      @ApiResponse(responseCode = "400", description = "Invalid variables or search parameters")
  })
  public Response facets(@QueryParam("v0") String crossVar0, @QueryParam("v1") String crossVar1) throws SearchException {
    return Response.ok().entity(contingencyService.getContingency(crossVar0, crossVar1)).build();
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getValuesIndexManager().getName() + "/" + entityType;
  }

}
