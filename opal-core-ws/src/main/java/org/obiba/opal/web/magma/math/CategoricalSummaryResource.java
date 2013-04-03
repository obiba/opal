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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.FrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CategoricalSummaryResource extends AbstractSummaryResource {

  private static final Logger log = LoggerFactory.getLogger(CategoricalSummaryResource.class);

  public CategoricalSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable, null);
  }

  @GET
  @POST
  public Response get(@QueryParam("distinct") boolean distinct) {
    CategoricalSummaryDto summary = canQueryEsIndex() ? queryEs(distinct) : queryMagma(distinct);

    SummaryStatisticsDto statisticsDto = SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
        .setExtension(CategoricalSummaryDto.categorical, summary).build();
    return TimestampedResponses.ok(getValueTable(), statisticsDto).build();
  }

  private CategoricalSummaryDto queryEs(boolean distinct) {

    log.info("Query ES for {} summary", getVariable().getName());

    try {

      JSONObject esQuery = createEsQuery(distinct);
      log.debug("ES query: {}", esQuery.toString(2));

      JSONObject response = new EsQueryExecutor(esProvider).execute(esQuery);
      log.debug("ES Response: {}", response.toString(2));

      JSONObject jsonHitsInfo = response.getJSONObject("hits");
      if(jsonHitsInfo.getInt("total") != 1) {
        return queryMagma(distinct); // fallback
      }
      return parseJsonSummary(
          jsonHitsInfo.getJSONArray("hits").getJSONObject(0).getJSONObject("_source").getJSONObject("summary"));

    } catch(JSONException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject createEsQuery(boolean distinct) throws IOException, JSONException {
    String indexName = statsIndexManager.getIndex(getValueTable()).getIndexName();
    String variableReference = getVariable().getVariableReference(getValueTable());
    XContentBuilder builder = XContentFactory.jsonBuilder().startObject() //
        .startObject("query") //
        .startObject("bool") //
        .startArray("must") //

        .startObject() //
        .startObject("term").field("_id", variableReference).endObject() //
        .endObject() //

        .startObject() //
        .startObject("term").field("_type", indexName).endObject() //
        .endObject() //

        .startObject() //
        .startObject("term").field("nature", "categorical").endObject() //
        .endObject() //

        .startObject() //
        .startObject("term").field("distinct", distinct).endObject() //
        .endObject() //

        .endArray() // must
        .endObject() // bool
        .endObject(); //query
    return new JSONObject(builder.string());
  }

  private CategoricalSummaryDto parseJsonSummary(JSONObject jsonSummary) throws JSONException {
    CategoricalSummaryDto.Builder dtoBuilder = CategoricalSummaryDto.newBuilder();
    dtoBuilder.setMode(jsonSummary.getString("mode")).setN(jsonSummary.getLong("n"));
    JSONArray frequencies = jsonSummary.getJSONArray("frequencies");
    int nbFreq = frequencies.length();
    for(int i = 0; i < nbFreq; i++) {
      JSONObject jsonFreq = frequencies.getJSONObject(i);
      dtoBuilder.addFrequencies(FrequencyDto.newBuilder() //
          .setValue(jsonFreq.getString("value")) //
          .setFreq(jsonFreq.getLong("freq")) //
          .setPct(jsonFreq.getDouble("pct")));
    }
    return dtoBuilder.build();
  }

  private CategoricalSummaryDto queryMagma(boolean distinct) {

    log.info("Query Magma for {} summary", getVariable().getName());

    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .addTable(getValueTable()) //
        .build();

    // TODO should we store this summary to ES with a new thread?
    statsIndexManager.getIndex(getValueTable()).indexSummary(summary);

    CategoricalSummaryDto.Builder dtoBuilder = CategoricalSummaryDto.newBuilder();
    dtoBuilder.setMode(summary.getMode()).setN(summary.getN());
    for(CategoricalVariableSummary.Frequency frequency : summary.getFrequencies()) {
      dtoBuilder.addFrequencies(FrequencyDto.newBuilder() //
          .setValue(frequency.getValue()) //
          .setFreq(frequency.getFreq()) //
          .setPct(frequency.getPct()));
    }

    return dtoBuilder.build();
  }
}
