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
import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.RQLParser;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.search.SearchQueryException;
import org.obiba.opal.spi.search.ValuesIndexManager;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.QuerySearchJsonBuilder;
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
import java.text.SimpleDateFormat;
import java.util.Collection;
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
  public Response search(@QueryParam("query") List<String> queries,
                         @QueryParam("type") @DefaultValue("Participant") String entityType, @QueryParam("offset") @DefaultValue("0") int offset,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("partials") @DefaultValue("false") boolean withPartials,
                         @QueryParam("format") @DefaultValue("es") String format) throws JSONException {
    if(!canQueryEsIndex()) return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
    this.entityType = entityType;

    List<ChildQueryParser> childQueries = extractChildQueries(queries, format);
    List<Search.EntitiesResultDto> partialResults = Lists.newArrayList();
    if (withPartials && childQueries.size()>1) {
      for (ChildQueryParser childQuery : childQueries) {
        QuerySearchJsonBuilder builder = buildHasChildQuerySearch(0, 0);
        builder.childQuery(childQuery.asChildQuery());
        JSONObject jsonResponse = executeQuery(builder.build());
        partialResults.add(getResultDtoBuilder(jsonResponse, childQuery.getOriginalQuery()).build());
      }
    }

    // global query
    QuerySearchJsonBuilder builder = buildHasChildQuerySearch(offset, limit);
    builder.childQueries(childQueries.stream().map(ChildQueryParser::asChildQuery).collect(Collectors.toList()));
    //childQueries.forEach(child -> builder.childQuery(child.asChildQuery()));
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
                         @QueryParam("type") @DefaultValue("Participant") String entityType,
                        @QueryParam("format") @DefaultValue("rql") String format) throws JSONException {
    return search(queries, entityType, 0, 0, true, format);
  }

  @Override
  protected String getSearchPath() {
    return opalSearchService.getValuesIndexManager().getName() + "/" + entityType;
  }

  //
  // Private methods
  //

  private List<ChildQueryParser> extractChildQueries(List<String> queries, String format) {
    if ("rql".equals(format))
      return queries.stream().map(RQLChildQueryParser::new).collect(Collectors.toList());
    else
      return queries.stream().map(ESChildQueryParser::new).collect(Collectors.toList());
  }


  private QuerySearchJsonBuilder buildHasChildQuerySearch(int offset, int limit) {
    QuerySearchJsonBuilder jsonBuilder = new QuerySearchJsonBuilder();
    jsonBuilder.from(offset).size(limit).noDefaultFields();
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

  private interface ChildQueryParser {

    /**
     * Get the query as a child query.
     *
     * @return
     */
    QuerySearchJsonBuilder.ChildQuery asChildQuery();

    /**
     * Get the query that was parsed.
     *
     * @return
     */
    String getOriginalQuery();
  }

  private class ESChildQueryParser implements ChildQueryParser {

    private final String query;

    private final String field;

    private final ValueTable table;

    private ESChildQueryParser(String query) {
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

    @Override
    public String getOriginalQuery() {
      return query;
    }

    @Override
    public QuerySearchJsonBuilder.ChildQuery asChildQuery() {
      return new QuerySearchJsonBuilder.ChildQuery(opalSearchService.getValuesIndexManager().getIndex(table).getIndexType(), query);
    }

    private String extractField() {
      String nQuery = query.startsWith("NOT ") ? query.substring(4) : query;
      if (nQuery.startsWith("_exists_:")) return nQuery.substring(9);
      return nQuery.substring(0, nQuery.indexOf(":"));
    }
  }

  private class RQLChildQueryParser implements ChildQueryParser {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final String rqlQuery;

    private final String query;

    private ValueTable table;

    private Variable variable;

    private RQLChildQueryParser(String query) {
      this.rqlQuery = query;
      this.query = parseNode(new RQLParser().parse(query));
    }

    @Override
    public QuerySearchJsonBuilder.ChildQuery asChildQuery() {
      return new QuerySearchJsonBuilder.ChildQuery(opalSearchService.getValuesIndexManager().getIndex(table).getIndexType(), query);
    }

    @Override
    public String getOriginalQuery() {
      return rqlQuery;
    }

    private String parseNode(ASTNode node) {
      log.info(node.getName());
      switch (node.getName()) {
        case "not":
          return "NOT " + parseNode((ASTNode) node.getArgument(0));
        case "exists":
          return "_exists_:" + parseField(node.getArgument(0).toString());
        case "in":
          return parseField(node.getArgument(0).toString()) + ":(" + parseValue(node.getArgument(1)) + ")";
        case "range":
          return parseField(node.getArgument(0).toString()) + ":[" + parseRange(node.getArgument(1)) + "]";
        case "and":
          return parseAnd(node.getArguments());
        case "or":
          return parseOr(node.getArguments());
      }
      return node.toString();
    }

    private String parseField(String variablePath) {
      MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variablePath);
      table = MagmaEngine.get().getDatasource(resolver.getDatasourceName()).getValueTable(resolver.getTableName());
      variable = table.getVariable(resolver.getVariableName());
      return opalSearchService.getValuesIndexManager().getIndex(table).getFieldName(resolver.getVariableName());
    }

    private String parseValue(Object value) {
      if (value instanceof ASTNode) return parseNode((ASTNode) value);
      if (value instanceof Collection) {
        Collection<?> values = (Collection)value;
        if (values.size() == 1 && getValueType().isDateTime()) return parseSingleDate(values.iterator().next());
        return parseOr(values);
      }
      return value.toString();
    }

    private String parseRange(Object value) {
      if (value instanceof Collection) {
        return join(" TO ", (Collection) value);
      }
      return value  + " TO *";
    }

    private String parseSingleDate(Object value) {
      String dateString = DATE_FORMAT.format(((DateTime)value).toDate());
      return ">=" + dateString + " AND " + "<=" + dateString;
    }

    private String parseAnd(Collection<?> args) {
      return join(" AND ", args);
    }

    private String parseOr(Collection<?> args) {
      return join(" OR ", args);
    }

    private String join(String on, Collection<?> args) {
      String nOn = on;
      boolean toQuote = getNature().equals(VariableNature.CATEGORICAL);
      List<String> nArgs = args.stream().map(arg -> {
        String nArg = getValueType().isDateTime() ? DATE_FORMAT.format(((DateTime)arg).toDate()) : arg.toString();
        if (toQuote) return "\"" + nArg + "\"";
        else return nArg;
      }).collect(Collectors.toList());

      return Joiner.on(nOn).join(nArgs);
    }

    private ValueType getValueType() {
      return variable == null ? TextType.get() : variable.getValueType();
    }

    private VariableNature getNature() {
      return variable == null ? VariableNature.UNDETERMINED : VariableNature.getNature(variable);
    }
  }
}
