/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es.mapping;

import java.io.IOException;
import java.util.Date;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.DateTimeType;
import org.obiba.runtime.Version;

public class ValueTableMapping {

  private final VariableMappings variableMappings = new VariableMappings();

  public XContentBuilder createMapping(Version opalVersion, String indexName, ValueTable valueTable) {
    try {
      XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(indexName);
      mapping.startObject("_all").field("enabled", false).endObject();
      mapping.startObject("_parent").field("type", valueTable.getEntityType()).endObject();

      mapping.startObject("properties");

      mapping.startObject("_id.analyzed") //
          .field("type", "string") //
          .field("index", "analyzed") //
          .field("index_analyzer", "opal_index_analyzer") //
          .field("search_analyzer", "opal_search_analyzer");
      mapping.endObject();

      for(Variable variable : valueTable.getVariables()) {
        variableMappings.map(indexName, variable, mapping);
      }

      mapping.endObject();// properties

      mapping.startObject("_meta") //
          .field("_created", DateTimeType.get().valueOf(new Date()).toString()) //
          .field("_opalversion", opalVersion.toString()) //
          .field("_reference", valueTable.getDatasource().getName() + "." + valueTable.getName()) //
          .endObject();

      mapping.endObject() // type
          .endObject(); // mapping
      return mapping;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }


  public XContentBuilder updateTimestamps(String name) {
    try {
      XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(name);

      mapping.startObject("_meta").field("_updated", DateTimeType.get().valueOf(new Date()).toString()).endObject();

      mapping.endObject() // type
          .endObject(); // mapping
      return mapping;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
