/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.math;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary.Distribution;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.DescriptiveStatsDto;
import org.obiba.opal.web.model.Math.IntervalFrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.obiba.opal.web.search.support.EsQueryBuilders;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.protobuf.format.JsonFormat;

public class ContinuousSummaryResource extends AbstractSummaryResource {

  private static final Logger log = LoggerFactory.getLogger(ContinuousSummaryResource.class);

  public ContinuousSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable);
  }

  @GET
  @POST
  public Response compute(@QueryParam("d") @DefaultValue("normal") Distribution distribution,
      @QueryParam("p") List<Double> percentiles, @QueryParam("intervals") @DefaultValue("10") int intervals) {

    ContinuousSummaryDto summary = canQueryEsIndex()
        ? queryEs(distribution, percentiles, intervals)
        : queryMagma(distribution, percentiles, intervals);

    SummaryStatisticsDto statisticsDto = SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
        .setExtension(ContinuousSummaryDto.continuous, summary).build();
    return TimestampedResponses.ok(getValueTable(), statisticsDto).build();

  }

  private ContinuousSummaryDto queryEs(Distribution distribution, List<Double> percentiles, int intervals) {
    log.info("Query ES for {} summary", getVariable().getName());

    try {

      JSONObject esQuery = createEsQuery(distribution, percentiles, intervals);
      log.debug("ES query: {}", esQuery.toString(2));

      JSONObject response = new EsQueryExecutor(esProvider).execute(esQuery);
      log.debug("ES Response: {}", response.toString(2));

      JSONObject jsonHitsInfo = response.getJSONObject("hits");
      if(jsonHitsInfo.getInt("total") != 1) {
        return queryMagma(distribution, percentiles, intervals); // fallback
      }
      return asDto(
          jsonHitsInfo.getJSONArray("hits").getJSONObject(0).getJSONObject("_source").getJSONObject("summary"));

    } catch(JSONException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject createEsQuery(Distribution distribution, List<Double> percentiles, int intervals)
      throws IOException, JSONException {
    // TODO query on percentiles
    return new EsQueryBuilders.EsBoolTermsQueryBuilder() //
        .addTerm("_id", getVariable().getVariableReference(getValueTable())) //
        .addTerm("_type", statsIndexManager.getIndex(getValueTable()).getIndexName()) //
        .addTerm("nature", "continuous") //
        .addTerm("distribution", distribution.name()) //
        .addTerm("intervals", String.valueOf(intervals)).build();
  }

  private ContinuousSummaryDto asDto(JSONObject jsonSummary) throws JSONException {

    DescriptiveStatsDto.Builder descriptiveBuilder = DescriptiveStatsDto.newBuilder() //
        .setN(jsonSummary.getLong("n")) //
        .setMin(jsonSummary.getDouble("min")) //
        .setMax(jsonSummary.getDouble("max")) //
        .setMean(jsonSummary.getDouble("mean")) //
        .setSum(jsonSummary.getDouble("sum")) //
        .setSumsq(jsonSummary.getDouble("sumsq")) //
        .setStdDev(jsonSummary.getDouble("stdDev")) //
        .setVariance(jsonSummary.getDouble("variance")) //
        .setSkewness(jsonSummary.getDouble("skewness")) //
        .setGeometricMean(jsonSummary.getDouble("geometricMean")) //
        .setKurtosis(jsonSummary.getDouble("kurtosis")) //
        .setMedian(jsonSummary.getDouble("median"));

    ContinuousSummaryDto.Builder continuousBuilder = ContinuousSummaryDto.newBuilder();

    parseJsonValues(jsonSummary, descriptiveBuilder);
    parseJsonPercentiles(jsonSummary, descriptiveBuilder);
    parseJsonFrequencies(jsonSummary, continuousBuilder);
    parseJsonDistPercentiles(jsonSummary, continuousBuilder);

    ContinuousSummaryDto summaryDto = continuousBuilder.setSummary(descriptiveBuilder).build();
    try {
      JsonFormat.print(summaryDto, System.out);
    } catch(IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return summaryDto;
  }

  private void parseJsonDistPercentiles(JSONObject jsonSummary, ContinuousSummaryDto.Builder continuousBuilder)
      throws JSONException {
    JSONArray jsonDistPercentiles = jsonSummary.getJSONArray("distributionPercentiles");
    if(jsonDistPercentiles != null) {
      int nbDistPercentiles = jsonDistPercentiles.length();
      for(int i = 0; i < nbDistPercentiles; i++) {
        continuousBuilder.addDistributionPercentiles(jsonDistPercentiles.getDouble(i));
      }
    }
  }

  private void parseJsonFrequencies(JSONObject jsonSummary, ContinuousSummaryDto.Builder continuousBuilder)
      throws JSONException {
    JSONArray jsonFrequencies = jsonSummary.getJSONArray("intervalFrequencies");
    if(jsonFrequencies != null) {
      int nbFreq = jsonFrequencies.length();
      for(int i = 0; i < nbFreq; i++) {
        JSONObject jsonFreq = jsonFrequencies.getJSONObject(i);
        continuousBuilder.addIntervalFrequency(IntervalFrequencyDto.newBuilder() //
            .setLower(jsonFreq.getDouble("lower")) //
            .setUpper(jsonFreq.getDouble("upper")) //
            .setFreq(jsonFreq.getLong("freq")) //
            .setDensity(jsonFreq.getDouble("density")) //
            .setDensityPct(jsonFreq.getDouble("densityPct")));
      }
    }
  }

  private void parseJsonPercentiles(JSONObject jsonSummary, DescriptiveStatsDto.Builder descriptiveBuilder)
      throws JSONException {
    JSONArray jsonPercentiles = jsonSummary.getJSONArray("percentiles");
    if(jsonPercentiles != null) {
      int nbPercentiles = jsonPercentiles.length();
      for(int i = 0; i < nbPercentiles; i++) {
        descriptiveBuilder.addPercentiles(jsonPercentiles.getDouble(i));
      }
    }
  }

  private void parseJsonValues(JSONObject jsonSummary, DescriptiveStatsDto.Builder descriptiveBuilder)
      throws JSONException {
    JSONArray jsonValues = jsonSummary.getJSONArray("values");
    if(jsonValues != null) {
      int nbValues = jsonValues.length();
      for(int i = 0; i < nbValues; i++) {
        descriptiveBuilder.addValues(jsonValues.getDouble(i));
      }
    }
  }

  private ContinuousSummaryDto queryMagma(Distribution distribution, List<Double> percentiles, int intervals) {

    log.info("Query Magma for {} summary", getVariable().getName());

    ContinuousVariableSummary summary = new ContinuousVariableSummary.Builder(getVariable(), distribution) //
        .defaultPercentiles(percentiles) //
        .intervals(intervals) //
        .addTable(getValueTable()) //
        .build();

    // TODO should we store this summary to ES with a new thread?
    statsIndexManager.getIndex(getValueTable()).indexSummary(summary);

    return Dtos.asDto(summary).build();
  }

}
