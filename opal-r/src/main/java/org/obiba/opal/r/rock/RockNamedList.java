/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONObject;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;

import java.util.*;

class RockNamedList implements RNamedList<RServerResult> {

  private final Map<String, RServerResult> map = Maps.newLinkedHashMap();

  public RockNamedList(JSONObject objectResult) {
    Iterator<String> keys = objectResult.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      Object value = objectResult.get(key);
      if (value instanceof JSONArray) {
        map.put(key, new RockResult((JSONArray) value));
      } else if (value instanceof JSONObject) {
        map.put(key, new RockResult((JSONObject) value));
      } else {
        map.put(key, new RockResult(value));
      }
    }
  }

  @Override
  public List<String> getNames() {
    return Lists.newArrayList(map.keySet());
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object o) {
    return map.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return map.containsValue(o);
  }

  @Override
  public RServerResult get(Object o) {
    return map.get(o);
  }

  @Override
  public Set<String> keySet() {
    return map.keySet();
  }

  @Override
  public Collection<RServerResult> values() {
    return map.values();
  }

  @Override
  public Set<Entry<String, RServerResult>> entrySet() {
    return map.entrySet();
  }
}
