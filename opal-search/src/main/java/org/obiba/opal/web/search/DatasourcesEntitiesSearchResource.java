/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.search;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.jazdw.rql.parser.ASTNode;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.spi.search.QuerySettings;
import org.obiba.opal.spi.search.SearchException;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.RQLIdentifierCriterionParser;
import org.obiba.opal.web.search.support.RQLParserFactory;
import org.obiba.opal.web.search.support.RQLValueSetVariableCriterionParser;
import org.obiba.opal.web.search.support.ValueSetVariableCriterionParser;
import org.obiba.opal.web.ws.SortDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/datasources/entities")
public class DatasourcesEntitiesSearchResource extends AbstractSearchUtility {

  private static final Logger log = LoggerFactory.getLogger(DatasourcesEntitiesSearchResource.class);

  private String entityType;

  @GET
  @Transactional(readOnly = true)
  @Path("_search")
  public Response search(@QueryParam("query") String query,
                         @QueryParam("id") String idQuery,
                         @QueryParam("type") @DefaultValue("Participant") String entityType,
                         @QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("counts") @DefaultValue("false") boolean withCounts) throws SearchException {
    if (!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    if (Strings.isNullOrEmpty(query)) return Response.status(Response.Status.BAD_REQUEST).build();
    this.entityType = entityType;

    final RQLIdentifierCriterionParser idCriterion = Strings.isNullOrEmpty(idQuery) ? null : new RQLIdentifierCriterionParser(idQuery);

    ASTNode queryNode = RQLParserFactory.newParser().parse(query);
    List<ValueSetVariableCriterionParser> childQueries = extractChildQueries(queryNode);
    List<Search.EntitiesResultDto> partialResults = Lists.newArrayList();
    if (withCounts && childQueries.size() > 1) {
      for (ValueSetVariableCriterionParser childQuery : childQueries) {
        QuerySettings querySettings = buildHasChildQuerySearch(0, 0);
        querySettings.childQuery(childQuery.asChildQuery(idCriterion == null ? null : idCriterion.getQuery()));
        partialResults.add(opalSearchService.executeEntitiesQuery(querySettings, getSearchPath(), entityType, childQuery.getOriginalQuery()).build());
      }
    }

    // global query

    QuerySettings querySettings = buildHasChildQuerySearch(offset, limit);
    querySettings.childQueries(childQueries.stream().map(p -> p.asChildQuery(idCriterion == null ? null : idCriterion.getQuery())).collect(Collectors.toList()));
    if (childQueries.size() > 1) querySettings.childQueryOperator(queryNode.getName());
    Search.EntitiesResultDto.Builder dtoResponseBuilder = opalSearchService.executeEntitiesQuery(querySettings, getSearchPath(), entityType, query);
    dtoResponseBuilder.addAllPartialResults(partialResults);
    return Response.ok().entity(dtoResponseBuilder.build()).build();
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_count")
  public Response count(@QueryParam("query") String query,
                        @QueryParam("id") String idQuery,
                        @QueryParam("type") @DefaultValue("Participant") String entityType) throws SearchException {
    return search(query, idQuery, entityType, 0, 0, true);
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getValuesIndexManager().getName() + "/" + entityType;
  }

  //
  // Private methods
  //

  private List<ValueSetVariableCriterionParser> extractChildQueries(ASTNode queryNode) {
    // for now the query must look like: and(q1,q2,...) or or(q1,q2...) or (q1,q2...) or q1,q2,...
    if ("and".equals(queryNode.getName()) || "or".equals(queryNode.getName()) || "".equals(queryNode.getName()))
      return queryNode.getArguments().stream().map(q -> new RQLValueSetVariableCriterionParser(opalSearchService.getValuesIndexManager(), (ASTNode) q)).collect(Collectors.toList());
    else // single query
      return Lists.newArrayList(new RQLValueSetVariableCriterionParser(opalSearchService.getValuesIndexManager(), queryNode));
  }

  private QuerySettings buildHasChildQuerySearch(int offset, int limit) {
    QuerySettings querySettings = new QuerySettings();
    querySettings.from(offset).size(limit).sortField("identifier").sortDir(SortDir.ASC.name()).noDefaultFields();
    return querySettings;
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getValuesIndexManager().isReady();
  }

}
