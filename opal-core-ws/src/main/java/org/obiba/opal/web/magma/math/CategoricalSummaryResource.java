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

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.core.magma.math.CategoricalVariableSummary;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.search.support.EsQueryBuilders;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.format.JsonFormat;

/**
 *
 */
public class CategoricalSummaryResource extends AbstractSummaryResource {

  private static final Logger log = LoggerFactory.getLogger(CategoricalSummaryResource.class);

  public CategoricalSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable, VariableValueSource vvs) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable, vvs);
  }

  @GET
  @POST
  public Response get(@QueryParam("distinct") boolean distinct) {
    SummaryStatisticsDto.Builder builder = SummaryStatisticsDto.newBuilder().setResource(getVariable().getName());
    CategoricalSummaryDto summary = canQueryEsIndex() ? queryEs(distinct) : queryMagma(distinct);
    if(summary != null) builder.setExtension(CategoricalSummaryDto.categorical, summary);
    return TimestampedResponses.ok(getValueTable(), builder.build()).build();
  }

  @Nullable
  private CategoricalSummaryDto queryEs(boolean distinct) {

    log.debug("Query ES for {} summary", getVariable().getName());

    try {

      JSONObject esQuery = new EsQueryBuilders.EsBoolTermsQueryBuilder() //
          .addTerm("_id", getVariable().getVariableReference(getValueTable())) //
          .addTerm("_type", statsIndexManager.getIndex(getValueTable()).getIndexName()) //
          .addTerm("categorical", String.valueOf(true)) // because I don't know to use field exists query filter
          .addTerm("distinct", String.valueOf(distinct)).build();
      log.trace("ES query: {}", esQuery.toString(2));

      JSONObject response = new EsQueryExecutor(esProvider).execute(esQuery);
      log.trace("ES Response: {}", response.toString(2));

      JSONObject jsonHitsInfo = response.getJSONObject("hits");
      if(jsonHitsInfo.getInt("total") != 1) {
        return queryMagma(distinct); // fallback
      }

      JSONObject jsonObject = jsonHitsInfo.getJSONArray("hits").getJSONObject(0).getJSONObject("_source");

      log.trace("jsonObject: {}", jsonObject.toString(2));

      Search.EsStatsDto.Builder builder = Search.EsStatsDto.newBuilder();

      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Search.EsCategoricalSummaryDto.categoricalSummary);

      JsonFormat.merge(jsonObject.toString(), registry, builder);
      return builder.build().getExtension(Search.EsCategoricalSummaryDto.categoricalSummary).getSummary();

    } catch(JSONException e) {
      log.error("Error while querying ES (will fallback to Magma)", e);
      return queryMagma(distinct);
    } catch(IOException e) {
      log.error("Error while querying ES (will fallback to Magma)", e);
      return queryMagma(distinct);
    }
  }

  @Nullable
  private CategoricalSummaryDto queryMagma(boolean distinct) {

    log.debug("Query Magma for {} summary", getVariable().getName());

    CategoricalVariableSummary summary = new CategoricalVariableSummary.Builder(getVariable()) //
        .distinct(distinct) //
        .addTable(getValueTable(), getVariableValueSource()) //
        .build();

    if(summary.isEmpty()) return null;

    if(!"_transient".equals(getVariable().getName()) && isEsAvailable()) {
      statsIndexManager.getIndex(getValueTable()).indexSummary(summary);
    }

    return Dtos.asDto(summary).build();
  }

}
