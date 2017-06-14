/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.search.es.mapping;

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MappingHelper {

  private static final String ANALYZED_FIELD_POSTFIX = ".analyzed";

  public static void mapNotAnalyzedString(String field, XContentBuilder mapping) throws IOException {
    mapping.startObject(field);
    mapping.field("type", "string");
    mapping.field("index", "not_analyzed");
    mapping.endObject();
  }

  public static void mapAnalyzedString(String field, XContentBuilder mapping) throws IOException {
    mapping.startObject(field);
    mapping.field("fields", mapFields(field));
    mapping.field("type", "multi_field");
    mapping.endObject();
  }

  private static Map<String, Map<String, String>> mapFields(String field) {
    Map<String, String> analyzed = new HashMap<>();
    analyzed.put("type", "string");
    analyzed.put("index", "analyzed");
    analyzed.put("index_analyzer", "opal_index_analyzer");
    analyzed.put("search_analyzer", "opal_search_analyzer");

    Map<String, String> notAnalyzed = new HashMap<>();
    notAnalyzed.put("type", "string");
    notAnalyzed.put("index", "not_analyzed");

    Map<String, Map<String, String>> fields = new HashMap<>();
    fields.put(field + ANALYZED_FIELD_POSTFIX, analyzed);
    fields.put(field, notAnalyzed);

    return fields;
  }
}
