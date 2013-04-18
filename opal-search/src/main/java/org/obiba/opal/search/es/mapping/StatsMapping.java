/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es.mapping;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.type.DateTimeType;
import org.obiba.runtime.Version;

/**
 *
 */
public class StatsMapping {

  public XContentBuilder createMapping(Version opalVersion, String indexName, ValueTable valueTable) {
    try {
      XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject(indexName);
      builder.startObject("properties");
      mapCategoricalSummary(builder);
      mapContinuousSummary(builder);
      builder.endObject();  // properties
      mapMetadata(opalVersion, valueTable, builder);

      return builder;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * force mapping because of wrong guessed fields
   */
  private void mapCategoricalSummary(XContentBuilder builder) throws IOException {

    builder.startObject("Search.EsCategoricalSummaryDto.categoricalSummary") //
        .field("dynamic", "strict") //
        .startObject("properties");

    builder.startObject("categorical").field("type", "boolean").endObject();
    builder.startObject("distinct").field("type", "boolean").endObject();

    // summary
    builder.startObject("summary").startObject("properties");
    builder.startObject("n").field("type", "long").endObject();
    builder.startObject("mode").field("type", "string").endObject();
    builder.startObject("frequencies").startObject("properties") //
        .startObject("pct").field("type", "double").endObject() //
        .startObject("freq").field("type", "long").endObject() //
        .startObject("value").field("type", "string").endObject() //
        .endObject().endObject();
    builder.endObject().endObject();  // properties / summary

    builder.endObject().endObject();  // properties / Search.EsCategoricalSummaryDto.categoricalSummary
  }

  private void mapContinuousSummary(XContentBuilder builder) throws IOException {

    builder.startObject("Search.EsContinuousSummaryDto.continuousSummary") //
        .field("dynamic", "strict") //
        .startObject("properties");
    builder.startObject("continuous").field("type", "boolean").endObject();

    builder.startObject("defaultPercentiles").field("type", "double").endObject();
    builder.startObject("intervals").field("type", "long").endObject();
    builder.startObject("distribution").field("type", "string").endObject();

    builder.startObject("summary").startObject("properties");
    builder.startObject("distributionPercentiles").field("type", "double").endObject();
    mapInnerContinuousSummary(builder);
    mapIntervalFrequency(builder);
    builder.endObject().endObject();  // properties / summary

    builder.endObject().endObject();  // properties / Search.EsContinuousSummaryDto.continuousSummary
  }

  private void mapInnerContinuousSummary(XContentBuilder builder) throws IOException {
    builder.startObject("summary").startObject("properties");
    builder.startObject("min").field("type", "double").endObject();
    builder.startObject("max").field("type", "double").endObject();
    builder.startObject("median").field("type", "double").endObject();
    builder.startObject("geometricMean").field("type", "double").endObject();
    builder.startObject("kurtosis").field("type", "double").endObject();
    builder.startObject("n").field("type", "long").endObject();
    builder.startObject("sum").field("type", "double").endObject();
    builder.startObject("sumsq").field("type", "double").endObject();
    builder.startObject("stdDev").field("type", "double").endObject();
    builder.startObject("variance").field("type", "double").endObject();
    builder.startObject("percentiles").field("type", "double").endObject();
    builder.startObject("skewness").field("type", "double").endObject();
    builder.startObject("mean").field("type", "double").endObject();
    builder.endObject().endObject();  // properties / summary
  }

  private void mapIntervalFrequency(XContentBuilder builder) throws IOException {
    builder.startObject("intervalFrequency").startObject("properties");
    builder.startObject("lower").field("type", "double").endObject();
    builder.startObject("upper").field("type", "double").endObject();
    builder.startObject("freq").field("type", "long").endObject();
    builder.startObject("density").field("type", "double").endObject();
    builder.startObject("densityPct").field("type", "double").endObject();
    builder.endObject().endObject();  // properties / intervalFrequency
  }

  private void mapMetadata(Version opalVersion, ValueTable valueTable, XContentBuilder builder) throws IOException {
    builder.startObject("_meta") //
        .field("_created", DateTimeType.get().valueOf(new Date()).toString()) //
        .field("_opalversion", opalVersion.toString()) //
        .field("_reference", valueTable.getDatasource().getName() + "." + valueTable.getName()) //
        .endObject();
  }

}
