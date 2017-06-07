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
import com.google.common.collect.Lists;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.SearchQueryException;
import org.obiba.opal.spi.search.ValuesIndexManager;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

import static com.orientechnologies.orient.core.index.OIndexDefinitionFactory.extractFieldName;

@Component
@Scope("request")
@Path("/datasources/entities")
public class DatasourcesEntitiesSearchResource extends AbstractSearchUtility {

//  private static final Logger log = LoggerFactory.getLogger(DatasourcesEntitiesSearchResource.class);

  private String entityType;

  @GET
  @Transactional(readOnly = true)
  @Path("_search")
  public Response search(@QueryParam("query") List<String> queries,
                         @QueryParam("type") @DefaultValue("Participant") String entityType, @QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("partials") @DefaultValue("false") boolean withPartials) throws JSONException {
    if(!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    this.entityType = entityType;

    List<ChildQueryParser> childQueries = extractChildQueries(queries);
    List<Search.EntitiesResultDto> partialResults = Lists.newArrayList();
    if (withPartials) {
      for (ChildQueryParser childQuery : childQueries) {
        QuerySearchJsonBuilder builder = buildHasChildQuerySearch(0, 0);
        builder.childQuery(childQuery.asChildQuery());
        JSONObject jsonResponse = executeQuery(builder.build());
        partialResults.add(getResultDtoBuilder(jsonResponse, childQuery.getQuery()).build());
      }
    }

    // global query
    QuerySearchJsonBuilder builder = buildHasChildQuerySearch(offset, limit);
    builder.childQueries(childQueries.stream().map(ChildQueryParser::asChildQuery).collect(Collectors.toList()));
    childQueries.forEach(child -> builder.childQuery(child.asChildQuery()));
    JSONObject jsonResponse = executeQuery(builder.build());

    if(!jsonResponse.isNull("error")) {
      throw new SearchQueryException(jsonResponse.get("error").toString());
    }

    Search.EntitiesResultDto.Builder dtoResponseBuilder = getResultDtoBuilder(jsonResponse, Joiner.on(" AND ").join(queries));
    dtoResponseBuilder.addAllPartialResults(partialResults);
    return Response.ok().entity(dtoResponseBuilder.build()).build();
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_count")
  public Response count(@QueryParam("query") List<String> queries,
                         @QueryParam("type") @DefaultValue("Participant") String entityType) throws JSONException {
    return search(queries, entityType, 0, 0, true);
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getValuesIndexManager().getName() + "/" + entityType;
  }

  //
  // Private methods
  //

  private List<ChildQueryParser> extractChildQueries(List<String> queries) {
    return queries.stream().map(q -> new ChildQueryParser(q)).collect(Collectors.toList());
  }


  private QuerySearchJsonBuilder buildHasChildQuerySearch(int offset, int limit) {
    QuerySearchJsonBuilder jsonBuilder = new QuerySearchJsonBuilder();
    jsonBuilder.from(offset).size(limit).noDefaultFields();
    return jsonBuilder;
  }

  private Search.EntitiesResultDto.Builder getResultDtoBuilder(JSONObject jsonResponse, String query) throws JSONException {
    Search.EntitiesResultDto.Builder builder = Search.EntitiesResultDto.newBuilder();
    builder.setEntityType(entityType);
    JSONObject jsonHits = jsonResponse.getJSONObject("hits");
    builder.setTotalHits(jsonHits.getInt("total"));
    builder.setQuery(query);

    JSONArray hits = jsonHits.getJSONArray("hits");
    if (hits.length()>0) {
      for(int i = 0; i < hits.length(); i++) {
        Search.ItemResultDto.Builder dtoItemResultBuilder = Search.ItemResultDto.newBuilder();
        JSONObject jsonHit = hits.getJSONObject(i);
        dtoItemResultBuilder.setIdentifier(jsonHit.getString("_id"));
        builder.addHits(dtoItemResultBuilder);
      }
    }
    return builder;
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getValuesIndexManager().isReady();
  }

  private class ChildQueryParser {

    private final String query;

    private final String field;

    private final ValueTable table;

    private ChildQueryParser(String query) {
      this.query = query;
      this.field = extractField();
      String[] tokens = field.split(ValuesIndexManager.FIELD_SEP);
      // verify variable access (exists and allowed)
      Datasource ds = MagmaEngine.get().getDatasource(tokens[0]);
      this.table = ds.getValueTable(tokens[1]);
      this.table.getVariable(tokens[2]);
    }

    public ValueTable getValueTable() {
      return table;
    }

    public String getField() {
      return field;
    }

    public String getQuery() {
      return query;
    }

    public QuerySearchJsonBuilder.ChildQuery asChildQuery() {
      return new QuerySearchJsonBuilder.ChildQuery(opalSearchService.getValuesIndexManager().getIndex(table).getIndexType(), query);
    }

    private String extractField() {
      if (query.startsWith("_exists_:")) return query.substring(9);
      else if (query.startsWith("NOT _exists_:")) return query.substring(13);
      return query.substring(0, query.indexOf(":"));
    }
  }
}
