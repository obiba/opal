/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;


import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.opal.OpalMap;

import java.util.List;

public class JsOpalMap {

  private final OpalMap opalMap;

  private final List<String> keys;

  public JsOpalMap(OpalMap opalMap) {
    this.opalMap = opalMap;
    this.keys = JsArrays.toList(opalMap.getKeysArray());
  }

  public List<String> getKeys() {
    return keys;
  }

  public String getKey(int idx) {
    if (idx>=0 && idx<size()) return keys.get(idx);
    return null;
  }

  public String getValue(String key) {
    if (keys.contains(key)) return opalMap.getValues(keys.indexOf(key));
    return null;
  }

  public boolean hasValue(String key) {
    return keys.contains(key);
  }

  public String getValue(int idx) {
    if (idx>=0 && idx<size()) return opalMap.getValues(idx);
    return null;
  }

  public int size() {
    return keys.size();
  }

  public boolean isEmpty() {
    return keys.isEmpty();
  }
}
