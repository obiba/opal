/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueTableMapping {

  private static final Logger log = LoggerFactory.getLogger(ValueTableMapping.class);

  private VariableMappings variableMappings = new VariableMappings();

  public XContentBuilder createMapping(String name, ValueTable valueTable) {
    try {
      XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject(name);
      mapping.startObject("_all").field("enabled", false).endObject().startObject("_parent").field("type", valueTable.getEntityType()).endObject();
      mapping.startObject("properties");
      for(Variable variable : valueTable.getVariables()) {
        variableMappings.map(variable, mapping);
      }
      mapping.endObject();// properties

      mapping.startObject("_meta").field("_created", DateTimeType.get().valueOf(new Date()).toString()).field("_updated", DateTimeType.get().valueOf(new Date()).toString()).endObject();

      mapping.endObject() // type
      .endObject(); // mapping

      System.out.println(mapping.string());

      return mapping;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
