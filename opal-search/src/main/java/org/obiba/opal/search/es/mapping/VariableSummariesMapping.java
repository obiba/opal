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
public class VariableSummariesMapping {

  public XContentBuilder createMapping(Version opalVersion, String indexName, ValueTable valueTable) {
    try {
      XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject(indexName);

      builder.startObject("_meta") //
          .field("_created", DateTimeType.get().valueOf(new Date()).toString()) //
          .field("_opalversion", opalVersion.toString()) //
          .field("_reference", valueTable.getDatasource().getName() + "." + valueTable.getName()) //
          .endObject();

      builder.startObject("categorical-summary") //
          .startObject("mode").field("type", "string").endObject() //
          .startObject("n").field("type", "long").endObject() //
          .startObject("frequency-value").field("type", "string").endObject() //
          .startObject("frequency-freq").field("type", "long").endObject() //
          .startObject("frequency-pct").field("type", "double").endObject() //
          .startObject("frequency-cummFreq").field("type", "long").endObject() //
          .startObject("frequency-cummPct").field("type", "double").endObject() //
          .endObject();

      return builder;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

}
