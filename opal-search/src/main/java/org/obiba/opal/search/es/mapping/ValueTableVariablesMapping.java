/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
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
import org.obiba.magma.Attribute;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.runtime.Version;

public class ValueTableVariablesMapping {

  private final ValueTypeMappings valueTypeMappings = new ValueTypeMappings();

  private final AttributeMapping attributeMapping = new AttributeMapping();

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public XContentBuilder createMapping(Version opalVersion, String name, ValueTable valueTable) {
    try {
      XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(name);

      mapping.startObject("properties");

      mapString("datasource", mapping);
      mapString("table", mapping);
      mapString("name", mapping);
      mapString("fullName", mapping);
      mapString("entityType", mapping);
      mapString("valueType", mapping);
      mapString("occurrenceGroup", mapping);
      mapString("unit", mapping);
      mapString("mimeType", mapping);
      mapString("referencedEntityType", mapping);
      mapString("category", mapping);

      mapping.startObject("repeatable");
      valueTypeMappings.forType(BooleanType.get()).map(mapping);
      mapping.endObject();

      for(Variable variable : valueTable.getVariables()) {
        if(variable.hasAttributes()) {
          for(Attribute attribute : variable.getAttributes()) {
            attributeMapping.map(attribute, mapping);
          }
        }
      }
      mapping.endObject(); // properties

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

  private void mapString(String field, XContentBuilder mapping) throws IOException {
    mapping.startObject(field);
    valueTypeMappings.forType(TextType.get()).map(mapping);
    mapping.endObject();
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
