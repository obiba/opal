/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.collect.Maps;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EsMapping {
  private static final Logger log = LoggerFactory.getLogger(EsMapping.class);

  private final String name;

  private final Map<String, Object> mapping;

  EsMapping(String name, byte[] mappingSource) throws IOException {
    this.name = name;
    mapping = XContentFactory.xContent(mappingSource).createParser(mappingSource).map();
  }

  EsMapping(String name) throws IOException {
    this.name = name;
    mapping = Maps.<String, Object> newHashMap();
  }

  XContentBuilder toXContent() throws IOException {
    return JsonXContent.contentBuilder().map(mapping);
  }

  Meta meta() {
    return new Meta();
  }

  private Map<String, Object> type() {
    return newIfAbsent(mapping, name);
  }

  class Meta {

    public String getString(String name) {
      return (String) meta().get(name);
    }

    public Meta setString(String name, String value) {
      meta().put(name, value);
      return this;
    }

    private Map<String, Object> meta() {
      return newIfAbsent(type(), "_meta");
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> newIfAbsent(Map<String, Object> map, String key) {
    Map<String, Object> inner = (Map<String, Object>) map.get(key);
    if(inner == null) {
      inner = Maps.<String, Object> newHashMap();
      map.put(key, inner);
    }
    return inner;
  }

}
