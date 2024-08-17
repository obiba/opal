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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import net.jazdw.rql.parser.ASTNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.search.AbstractSearchUtility;
import org.obiba.opal.spi.search.QuerySettings;
import org.obiba.opal.spi.search.SearchException;
import org.obiba.opal.web.model.Identifiers;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.RQLIdentifierCriterionParser;
import org.obiba.opal.web.search.support.RQLParserFactory;
import org.obiba.opal.web.search.support.RQLValueSetVariableCriterionParser;
import org.obiba.opal.web.search.support.ValueSetVariableCriterionParser;
import org.obiba.opal.web.ws.SortDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("request")
@Path("/datasources/entities")
public class DatasourcesEntitiesSearchResource extends AbstractSearchUtility {

  private static final Logger log = LoggerFactory.getLogger(DatasourcesEntitiesSearchResource.class);

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private SQLService sqlService;

  private String entityType;

  @GET
  @Transactional(readOnly = true)
  @Path("_suggest")
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
    try {
      Search.EntitiesResultDto.Builder dtoResponseBuilder = opalSearchService.executeEntitiesQuery(querySettings, getSearchPath(), entityType, query);
      dtoResponseBuilder.addAllPartialResults(partialResults);
      return Response.ok().entity(dtoResponseBuilder.build()).build();
    } catch (Exception e) {
      // Search engine exception
      throw new SearchException("Query failed to be executed: " + query, e.getCause() == null ? e : e.getCause());
    }
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_count")
  public Response count(@QueryParam("query") String query,
                        @QueryParam("id") String idQuery,
                        @QueryParam("type") @DefaultValue("Participant") String entityType) throws SearchException {
    return search(query, idQuery, entityType, 0, 0, true);
  }

  @GET
  @Transactional(readOnly = true)
  @Path("_contingency")
  public Response facets(@QueryParam("v0") String crossVar0, @QueryParam("v1") String crossVar1) throws SearchException {
    if (Strings.isNullOrEmpty(crossVar0) || Strings.isNullOrEmpty(crossVar1))
      return Response.status(Response.Status.BAD_REQUEST).build();
    log.info("Contingency table: {} x {}", crossVar0, crossVar1);

    MagmaEngineVariableResolver var0Resolver = MagmaEngineVariableResolver.valueOf(crossVar0);
    MagmaEngineVariableResolver var1Resolver = MagmaEngineVariableResolver.valueOf(crossVar1);
    // support only contingency table between variables from the same table (for now)
    if (!var0Resolver.getDatasourceName().equals(var1Resolver.getDatasourceName()) || !var0Resolver.getTableName().equals(var1Resolver.getTableName()))
      return Response.status(Response.Status.BAD_REQUEST).build();

    ValueTable table0 = getValueTable(var0Resolver);
    Variable var0 = table0.getVariable(var0Resolver.getVariableName());
    if (!VariableNature.getNature(var0).equals(VariableNature.CATEGORICAL))
      return Response.status(Response.Status.BAD_REQUEST).build();

    ValueTable table1 = getValueTable(var1Resolver);
    // verify variable exists and is accessible
    Variable var1 = table1.getVariable(var1Resolver.getVariableName());

    List<String> categories = getCategories(var0);
    if (categories.isEmpty())
      return Response.status(Response.Status.BAD_REQUEST).build();

    if (searchServiceAvailable()) {
      return getFacetsFromSearchService(crossVar0, crossVar1, table0, var0, categories);
    } else {
      return getFacetsFromSQLService(table0, var0, categories, var1);
    }
  }

  private List<String> getCategories(Variable var) {
    List<String> categories = null;
    if (var.hasCategories())
      categories = var.getCategories().stream().map(Category::getName).collect(Collectors.toList());
    else if (var.getValueType().equals(BooleanType.get())) categories = Lists.newArrayList("true", "false");
    return categories == null ? Lists.newArrayList() : categories;
  }

  private Response getFacetsFromSQLService(ValueTable table0, Variable var0, List<String> categories, Variable var1) {
    if (VariableNature.getNature(var1).equals(VariableNature.CATEGORICAL)) {
      return getFacetFrequencies(table0, var0, categories, var1);
    } else if (VariableNature.getNature(var1).equals(VariableNature.CONTINUOUS)) {
      return getFacetStatistics(table0, var0, categories, var1);
    }
    return Response.status(Response.Status.BAD_REQUEST).build();
  }

  private Response getFacetFrequencies(ValueTable table0, Variable var0, List<String> categories, Variable var1) {
    String query = String.format("SELECT `%s`, `%s`, count(*) as _count FROM `%s` WHERE `%s` IS NOT NULL AND `%s` IS NOT NULL GROUP BY `%s`, `%s`",
        var0.getName(), var1.getName(), table0.getName(), // select from
        var0.getName(), var1.getName(), // where
        var0.getName(), var1.getName() // group by
    );
    File output = sqlService.execute(table0.getDatasource().getName(), query, SQLService.DEFAULT_ID_COLUMN, SQLService.Output.JSON);
    try {
      JSONObject result = readJSONObject(output);
      JSONArray rows = result.getJSONArray("rows");
      Map<String, Map<String, Integer>> facets = Maps.newHashMap();
      for (int i = 0; i < rows.length(); i++) {
        JSONArray row = rows.getJSONArray(i);
        String cat0 = row.get(0).toString();
        String cat1 = row.get(1).toString();
        int count = row.getInt(2);
        if (!facets.containsKey(cat0)) facets.put(cat0, Maps.newHashMap());
        facets.get(cat0).put(cat1, count);
      }
      List<String> categories1 = getCategories(var1);
      Search.QueryResultDto.Builder builder = Search.QueryResultDto.newBuilder();
      int totalHits = 0;
      Map<String, Integer> totalFreq = Maps.newHashMap();
      // for each var0 category and each var1 category, get the count
      for (String category0 : categories.stream().filter(facets::containsKey).toList()) {
        Map<String, Integer> facet = facets.get(category0);
        int facetHits = 0;
        Search.FacetResultDto.Builder facetBuilder = Search.FacetResultDto.newBuilder();
        facetBuilder.setFacet(category0);
        for (String category1 : categories1.stream().filter(facet::containsKey).toList()) {
          Search.FacetResultDto.TermFrequencyResultDto.Builder freqBuilder = Search.FacetResultDto.TermFrequencyResultDto.newBuilder();
          freqBuilder.setTerm(category1);
          // find count
          int count = facet.get(category1);
          freqBuilder.setCount(count);
          if (totalFreq.containsKey(category1)) {
            totalFreq.put(category1, totalFreq.get(category1) + count);
          } else {
            totalFreq.put(category1, count);
          }
          facetHits = facetHits + count;
          totalHits = totalHits + count;
          facetBuilder.addFrequencies(freqBuilder);
        }
        facetBuilder.addFilters(Search.FacetResultDto.FilterResultDto.newBuilder().setCount(facetHits));
        builder.addFacets(facetBuilder);
      }
      Search.FacetResultDto.Builder totalFacetBuilder = Search.FacetResultDto.newBuilder();
      totalFacetBuilder.setFacet("_total");
      totalFacetBuilder.addAllFrequencies(totalFreq.keySet().stream()
          .map((cat1) -> Search.FacetResultDto.TermFrequencyResultDto.newBuilder().setTerm(cat1).setCount(totalFreq.get(cat1)).build())
          .toList());
      builder.addFacets(totalFacetBuilder);
      builder.setTotalHits(totalHits);
      return Response.ok().entity(builder.build()).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      output.delete();
    }
  }

  private Response getFacetStatistics(ValueTable table0, Variable var0, List<String> categories, Variable var1) {
    String statsStatement = String.format(
        """
            count(*) AS _count,
            total(`%s`) AS _total,
            min(`%s`) AS _min,
            max(`%s`) AS _max,
            avg(`%s`) AS _mean,
            sum(`%s` * `%s`) AS _sum_of_squares,
            avg((`%s` - (SELECT avg(`%s`) FROM `%s`)) * (`%s` - (SELECT avg(`%s`) FROM `%s`))) AS _variance,
            sqrt(avg((`%s` - (SELECT avg(`%s`) FROM `%s`)) * (`%s` - (SELECT avg(`%s`) FROM `%s`)))) AS _stdev
            FROM `%s`
            WHERE `%s` IS NOT NULL AND `%s` IS NOT NULL""",
        var1.getName(), // total
        var1.getName(), // min
        var1.getName(), // max
        var1.getName(), // mean
        var1.getName(), var1.getName(), // sum of squares
        var1.getName(), var1.getName(), table0.getName(), var1.getName(), var1.getName(), table0.getName(), // variance
        var1.getName(), var1.getName(), table0.getName(), var1.getName(), var1.getName(), table0.getName(), // stdev
        table0.getName(), // select
        var0.getName(), var1.getName() // where
    );
    String query = String.format("""
            SELECT `%s`,
            %s
            GROUP BY `%s`
            ORDER BY `%s`""",
        var0.getName(), // select
        statsStatement, // stats
        var0.getName(), // group by
        var0.getName()  // order by
    );
    File output = sqlService.execute(table0.getDatasource().getName(), query, SQLService.DEFAULT_ID_COLUMN, SQLService.Output.JSON);
    query = String.format("SELECT %s", statsStatement);
    File outputTotal = sqlService.execute(table0.getDatasource().getName(), query, SQLService.DEFAULT_ID_COLUMN, SQLService.Output.JSON);
    try {
      JSONObject result = readJSONObject(output);
      JSONArray rows = result.getJSONArray("rows");
      Search.QueryResultDto.Builder builder = Search.QueryResultDto.newBuilder();
      int totalHits = 0;
      // for each var0 category and each var1 category, get the count
      for (int i = 0; i < rows.length(); i++) {
        JSONArray row = rows.getJSONArray(i);
        String cat0 = row.get(0).toString();
        int count = row.getInt(1);
        float total = row.getFloat(2);
        float min = row.getFloat(3);
        float max = row.getFloat(4);
        float mean = row.getFloat(5);
        float sumOfSquares = row.getFloat(6);
        float variance = row.getFloat(7);
        float stdDev = row.getFloat(8);
        Search.FacetResultDto.Builder facetBuilder = Search.FacetResultDto.newBuilder();
        facetBuilder.setFacet(cat0);
        Search.FacetResultDto.StatisticalResultDto.Builder statsBuilder = Search.FacetResultDto.StatisticalResultDto.newBuilder();
        statsBuilder.setCount(count).setTotal(total)
            .setMin(min).setMax(max).setMean(mean)
            .setSumOfSquares(sumOfSquares).setVariance(variance).setStdDeviation(stdDev);
        facetBuilder.setStatistics(statsBuilder);
        builder.addFacets(facetBuilder);
        totalHits = totalHits + count;
      }

      result = readJSONObject(outputTotal);
      rows = result.getJSONArray("rows");
      JSONArray row = rows.getJSONArray(0);
      int count = row.getInt(0);
      float total = row.getFloat(1);
      float min = row.getFloat(2);
      float max = row.getFloat(3);
      float mean = row.getFloat(4);
      float sumOfSquares = row.getFloat(5);
      float variance = row.getFloat(6);
      float stdDev = row.getFloat(7);
      Search.FacetResultDto.Builder facetBuilder = Search.FacetResultDto.newBuilder();
      facetBuilder.setFacet("_total");
      Search.FacetResultDto.StatisticalResultDto.Builder statsBuilder = Search.FacetResultDto.StatisticalResultDto.newBuilder();
      statsBuilder.setCount(count).setTotal(total)
          .setMin(min).setMax(max).setMean(mean)
          .setSumOfSquares(sumOfSquares).setVariance(variance).setStdDeviation(stdDev);
      facetBuilder.setStatistics(statsBuilder);
      builder.addFacets(facetBuilder);

      builder.setTotalHits(totalHits);
      return Response.ok().entity(builder.build()).build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      output.delete();
      outputTotal.delete();
    }
  }

  private JSONObject readJSONObject(File input) throws IOException {
    StringBuilder jsonData = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(input))) {
      String line;
      while ((line = reader.readLine()) != null) {
        jsonData.append(line);
      }
    }

    JSONObject jsonObject = new JSONObject(jsonData.toString());
    return jsonObject;
  }

  private Response getFacetsFromSearchService(String crossVar0, String crossVar1, ValueTable table0, Variable var0, List<String> categories) throws SearchException {
    // one facet per crossVar0 category
    Search.QueryTermsDto.Builder queryBuilder = Search.QueryTermsDto.newBuilder();
    categories.forEach(ct ->
        queryBuilder.addQueries(Search.QueryTermDto.newBuilder()
            .setFacet(ct)
            .setExtension(Search.VariableTermDto.field, Search.VariableTermDto.newBuilder().setVariable(crossVar1).build())
            .setExtension(Search.LogicalTermDto.facetFilter, Search.LogicalTermDto.newBuilder()
                .setOperator(Search.TermOperator.AND_OP)
                .addExtension(Search.FilterDto.filters, Search.FilterDto.newBuilder()
                    .setVariable(crossVar0)
                    .setExtension(Search.InTermDto.terms, Search.InTermDto.newBuilder()
                        .addValues(ct)
                        .setMinimumMatch(1).build())
                    .build())
                .build()))
    );

    // one facet for the total
    queryBuilder.addQueries(Search.QueryTermDto.newBuilder()
        .setFacet("_total")
        .setExtension(Search.VariableTermDto.field, Search.VariableTermDto.newBuilder().setVariable(crossVar1).build())
        .setExtension(Search.LogicalTermDto.facetFilter, Search.LogicalTermDto.newBuilder()
            .setOperator(Search.TermOperator.AND_OP)
            .addExtension(Search.FilterDto.filters, Search.FilterDto.newBuilder()
                .setVariable(crossVar0)
                .setExtension(Search.InTermDto.terms, Search.InTermDto.newBuilder()
                    .addAllValues(var0.getCategories().stream().map(Category::getName).collect(Collectors.toList()))
                    .setMinimumMatch(1).build())
                .build())
            .build()));

    try {
      Search.QueryResultDto dtoResult = opalSearchService.executeQuery(table0.getDatasource().getName(), table0.getName(), queryBuilder.build());
      return Response.ok().entity(dtoResult).build();
    } catch (Exception e) {
      // Search engine exception
      throw new SearchException("Contingency query failed to be executed: " + crossVar0 + " x " + crossVar1, e.getCause() == null ? e : e.getCause());
    }
  }

  private ValueTable getValueTable(MagmaEngineVariableResolver varResolver) {
    return MagmaEngine.get().getDatasource(varResolver.getDatasourceName()).getValueTable(varResolver.getTableName());
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
    querySettings.from(offset).size(limit).sortField("identifier", SortDir.ASC.name()).noDefaultFields();
    return querySettings;
  }

  private boolean canQueryEsIndex() {
    return searchServiceAvailable() && opalSearchService.getValuesIndexManager().isReady();
  }

}
