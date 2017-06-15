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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import net.jazdw.rql.parser.ASTNode;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.SearchQueryException;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.*;
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
                         @QueryParam("counts") @DefaultValue("false") boolean withCounts) throws JSONException {
    if (!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    this.entityType = entityType;

    final RQLIdentifierCriterionParser idCriterion = Strings.isNullOrEmpty(idQuery) ? null : new RQLIdentifierCriterionParser(idQuery);

    ASTNode queryNode = RQLParserFactory.newParser().parse(query);
    List<ValueSetVariableCriterionParser> childQueries = extractChildQueries(queryNode);
    List<Search.EntitiesResultDto> partialResults = Lists.newArrayList();
    if (withCounts && childQueries.size() > 1) {
      for (ValueSetVariableCriterionParser childQuery : childQueries) {
        QuerySearchJsonBuilder builder = buildHasChildQuerySearch(0, 0);
        builder.childQuery(childQuery.asChildQuery(idCriterion == null ? null : idCriterion.getQuery()));
        JSONObject jsonResponse = executeQuery(builder.build());
        partialResults.add(getResultDtoBuilder(jsonResponse, childQuery.getOriginalQuery()).build());
      }
    }

    // global query

    QuerySearchJsonBuilder builder = buildHasChildQuerySearch(offset, limit);
    builder.childQueries(childQueries.stream().map(p -> p.asChildQuery(idCriterion == null ? null : idCriterion.getQuery())).collect(Collectors.toList()));
    if (childQueries.size()>1) builder.childQueryOperator(queryNode.getName());
    JSONObject jsonResponse = executeQuery(builder.build());

    if (!jsonResponse.isNull("error")) {
      throw new SearchQueryException(jsonResponse.get("error").toString());
    }

    Search.EntitiesResultDto.Builder dtoResponseBuilder = getResultDtoBuilder(jsonResponse, query);
    dtoResponseBuilder.addAllPartialResults(partialResults);
    return Response.ok().entity(dtoResponseBuilder.build()).build();
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_count")
  public Response count(@QueryParam("query") String query,
                        @QueryParam("id") String idQuery,
                        @QueryParam("type") @DefaultValue("Participant") String entityType) throws JSONException {
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
      return queryNode.getArguments().stream().map(q -> new RQLValueSetVariableCriterionParser(opalSearchService.getValuesIndexManager(), (ASTNode)q)).collect(Collectors.toList());
    else // single query
      return Lists.newArrayList(new RQLValueSetVariableCriterionParser(opalSearchService.getValuesIndexManager(), queryNode));
  }

  private QuerySearchJsonBuilder buildHasChildQuerySearch(int offset, int limit) {
    QuerySearchJsonBuilder jsonBuilder = new QuerySearchJsonBuilder();
    jsonBuilder.from(offset).size(limit).sortField("identifier").sortDir(SortDir.ASC.name()).noDefaultFields();
    return jsonBuilder;
  }

  private Search.EntitiesResultDto.Builder getResultDtoBuilder(JSONObject jsonResponse, String query) throws JSONException {
    Search.EntitiesResultDto.Builder builder = Search.EntitiesResultDto.newBuilder();
    builder.setEntityType(entityType);
    if (jsonResponse.has("hits")) {
      JSONObject jsonHits = jsonResponse.getJSONObject("hits");
      builder.setTotalHits(jsonHits.getInt("total"));
      builder.setQuery(query);

      JSONArray hits = jsonHits.getJSONArray("hits");
      if (hits.length() > 0) {
        for (int i = 0; i < hits.length(); i++) {
          Search.ItemResultDto.Builder dtoItemResultBuilder = Search.ItemResultDto.newBuilder();
          JSONObject jsonHit = hits.getJSONObject(i);
          dtoItemResultBuilder.setIdentifier(jsonHit.getString("_id"));
          builder.addHits(dtoItemResultBuilder);
        }
      }
    } else {
      builder.setTotalHits(0);
      builder.setQuery(query);
    }
    return builder;
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getValuesIndexManager().isReady();
  }

}
