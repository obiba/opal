/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Some utility methods for handling JSON objects.
 */
public class JSONUtils {

  public static Map<String, Object> toMap(JSONObject jsonobj)  throws JSONException {
    Map<String, Object> map = new HashMap<>();
    Iterator keys = jsonobj.keys();
    while(keys.hasNext()) {
      String key = keys.next().toString();
      Object value = jsonobj.get(key);
      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      } else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      map.put(key, value);
    }   return map;
  }

  public static List<Object> toList(JSONArray array) throws JSONException {
    List<Object> list = new ArrayList<>();
    for(int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof JSONArray) {
        value = toList((JSONArray) value);
      }
      else if (value instanceof JSONObject) {
        value = toMap((JSONObject) value);
      }
      list.add(value);
    }   return list;
  }
}
