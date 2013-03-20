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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.obiba.magma.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeMapping {

  private final ValueTypeMappings valueTypeMappings = new ValueTypeMappings();

  public XContentBuilder map(Attribute attribute, XContentBuilder builder) {
    try {
      builder.startObject(getFieldName(attribute));
      valueTypeMappings.forType(attribute.getValueType()).map(builder);
      return builder.endObject();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getFieldName(Attribute attribute) {
    String field = attribute.getName();
    if (attribute.hasNamespace()) {
      field = attribute.getNamespace() + "-" + field;
    }
    if (attribute.isLocalised()) {
      field += "-" + attribute.getLocale().toString();
    }
    return field;
  }
}
