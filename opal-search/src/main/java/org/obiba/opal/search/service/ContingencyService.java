/*
 * Copyright (c) 2024 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import jakarta.ws.rs.BadRequestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.magma.Category;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableNature;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.core.service.SQLService;
import org.obiba.opal.web.model.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContingencyService {

  private static final Logger log = LoggerFactory.getLogger(ContingencyService.class);

  private final SQLService sqlService;

  @Autowired
  public ContingencyService(SQLService sqlService) {
    this.sqlService = sqlService;
  }

  public Search.QueryResultDto getContingency(String crossVar0, String crossVar1) {
    if (Strings.isNullOrEmpty(crossVar0) || Strings.isNullOrEmpty(crossVar1))
      throw new BadRequestException();
    log.info("Contingency table: {} x {}", crossVar0, crossVar1);

    MagmaEngineVariableResolver var0Resolver = MagmaEngineVariableResolver.valueOf(crossVar0);
    MagmaEngineVariableResolver var1Resolver = MagmaEngineVariableResolver.valueOf(crossVar1);
    // support only contingency table between variables from the same table (for now)
    if (!var0Resolver.getDatasourceName().equals(var1Resolver.getDatasourceName()) || !var0Resolver.getTableName().equals(var1Resolver.getTableName()))
      throw new BadRequestException();

    ValueTable table0 = getValueTable(var0Resolver);
    Variable var0 = table0.getVariable(var0Resolver.getVariableName());
    if (!VariableNature.getNature(var0).equals(VariableNature.CATEGORICAL) && !BooleanType.get().equals(var0.getValueType()))
      throw new BadRequestException();

    ValueTable table1 = getValueTable(var1Resolver);
    // verify variable exists and is accessible
    Variable var1 = table1.getVariable(var1Resolver.getVariableName());

    return getFacetsFromSQLService(table0, var0, var1);
  }

  private Search.QueryResultDto getFacetsFromSQLService(ValueTable table0, Variable var0, Variable var1) {
    List<String> categories = getCategories(var0);
    if (VariableNature.getNature(var1).equals(VariableNature.CATEGORICAL)) {
      return getFacetFrequencies(table0, var0, categories, var1);
    } else if (VariableNature.getNature(var1).equals(VariableNature.CONTINUOUS)) {
      return getFacetStatistics(table0, var0, categories, var1);
    }
    throw new BadRequestException();
  }

  private Search.QueryResultDto getFacetFrequencies(ValueTable table0, Variable var0, List<String> categories, Variable var1) {
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
        int count = row.isNull(2) ? 0 : row.getInt(2);
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
      totalFacetBuilder.addFilters(Search.FacetResultDto.FilterResultDto.newBuilder().setCount(totalHits));
      builder.addFacets(totalFacetBuilder);
      builder.setTotalHits(totalHits);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      output.delete();
    }
  }

  private Search.QueryResultDto getFacetStatistics(ValueTable table0, Variable var0, List<String> categories, Variable var1) {
    String var1Filter = String.format("`%s` IS NOT NULL", var1.getName());
    if (var1.hasCategories()) {
      // exclude missing values described by categories
      String missings = var1.getCategories().stream()
          .filter(Category::isMissing)
          .map(Category::getName)
          .map(name -> name.replace("'", "''")) // escape single quotes
          .collect(Collectors.joining("','"));
      var1Filter = String.format("%s AND `%s` NOT IN ('%s')", var1Filter, var1.getName(), missings);
    }
    String avgStatement = String.format("`%s` - (SELECT avg(`%s`) FROM `%s` WHERE %s)", var1.getName(), var1.getName(), table0.getName(), var1Filter);
    String statsStatement = String.format(
        """
            count(*) AS _count,
            total(`%s`) AS _total,
            min(`%s`) AS _min,
            max(`%s`) AS _max,
            avg(`%s`) AS _mean,
            sum(`%s` * `%s`) AS _sum_of_squares,
            avg((%s) * (%s)) AS _variance,
            sqrt(avg((%s) * (%s))) AS _stdev
            FROM `%s`
            WHERE `%s` IS NOT NULL AND %s""",
        var1.getName(), // total
        var1.getName(), // min
        var1.getName(), // max
        var1.getName(), // mean
        var1.getName(), var1.getName(), // sum of squares
        avgStatement, avgStatement, // variance
        avgStatement, avgStatement, // stdev
        table0.getName(), // select
        var0.getName(), var1Filter // where
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
        Search.FacetResultDto.Builder facetBuilder = Search.FacetResultDto.newBuilder();
        facetBuilder.setFacet(cat0);
        Search.FacetResultDto.StatisticalResultDto.Builder statsBuilder = Search.FacetResultDto.StatisticalResultDto.newBuilder();
        int count = row.isNull(1) ? 0 : row.getInt(1);
        statsBuilder.setCount(count);
        if (!row.isNull(2)) statsBuilder.setTotal(row.getFloat(2));
        if (!row.isNull(3)) statsBuilder.setMin(row.getFloat(3));
        if (!row.isNull(4)) statsBuilder.setMax(row.getFloat(4));
        if (!row.isNull(5)) statsBuilder.setMean(row.getFloat(5));
        if (!row.isNull(6)) statsBuilder.setSumOfSquares(row.getFloat(6));
        if (!row.isNull(7)) statsBuilder.setVariance(row.getFloat(7));
        if (!row.isNull(8)) statsBuilder.setStdDeviation(row.getFloat(8));
        facetBuilder.setStatistics(statsBuilder);
        facetBuilder.addFilters(Search.FacetResultDto.FilterResultDto.newBuilder().setCount(count));
        builder.addFacets(facetBuilder);
        totalHits = totalHits + count;
      }

      result = readJSONObject(outputTotal);
      rows = result.getJSONArray("rows");
      JSONArray row = rows.getJSONArray(0);
      Search.FacetResultDto.Builder totalFacetBuilder = Search.FacetResultDto.newBuilder();
      totalFacetBuilder.setFacet("_total");
      Search.FacetResultDto.StatisticalResultDto.Builder statsBuilder = Search.FacetResultDto.StatisticalResultDto.newBuilder();
      statsBuilder.setCount(row.isNull(0) ? 0 : row.getInt(0));
      if (!row.isNull(1)) statsBuilder.setTotal(row.getFloat(1));
      if (!row.isNull(2)) statsBuilder.setMin(row.getFloat(2));
      if (!row.isNull(3)) statsBuilder.setMax(row.getFloat(3));
      if (!row.isNull(4)) statsBuilder.setMean(row.getFloat(4));
      if (!row.isNull(5)) statsBuilder.setSumOfSquares(row.getFloat(5));
      if (!row.isNull(6)) statsBuilder.setVariance(row.getFloat(6));
      if (!row.isNull(7)) statsBuilder.setStdDeviation(row.getFloat(7));
      totalFacetBuilder.setStatistics(statsBuilder);
      totalFacetBuilder.addFilters(Search.FacetResultDto.FilterResultDto.newBuilder().setCount(totalHits));
      builder.addFacets(totalFacetBuilder);

      builder.setTotalHits(totalHits);
      return builder.build();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      output.delete();
      outputTotal.delete();
    }
  }

  private ValueTable getValueTable(MagmaEngineVariableResolver varResolver) {
    return MagmaEngine.get().getDatasource(varResolver.getDatasourceName()).getValueTable(varResolver.getTableName());
  }

  private List<String> getCategories(Variable var) {
    List<String> categories = null;
    if (var.hasCategories())
      categories = var.getCategories().stream().map(Category::getName).collect(Collectors.toList());
    else if (var.getValueType().equals(BooleanType.get())) categories = Lists.newArrayList("true", "false");
    return categories == null ? Lists.newArrayList() : categories;
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
}
