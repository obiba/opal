/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rserve;

import com.google.common.collect.Maps;
import org.obiba.opal.spi.r.RNamedList;
import org.obiba.opal.spi.r.RServerResult;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class RserveNamedList implements RNamedList<RServerResult> {

  private final Map<String, RServerResult> namedList = Maps.newLinkedHashMap();

  public RserveNamedList(REXP rexp) throws REXPMismatchException {
    if (rexp != null && rexp.isList()) {
      RList list = rexp.asList();
      if (list.isNamed()) {
        for (int i = 0; i < list.names.size(); i++) {
          String key = list.names.get(i).toString();
          REXP value = list.at(key);
          namedList.put(key, new RserveResult(value));
        }
      }
    }
  }

  @Override
  public int size() {
    return namedList.size();
  }

  @Override
  public boolean isEmpty() {
    return namedList.isEmpty();
  }

  @Override
  public boolean containsKey(Object o) {
    return namedList.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return namedList.containsValue(o);
  }

  @Override
  public RServerResult get(Object o) {
    return namedList.get(o);
  }

  @Override
  public Set<String> keySet() {
    return namedList.keySet();
  }

  @Override
  public Collection<RServerResult> values() {
    return namedList.values();
  }

  @Override
  public Set<Entry<String, RServerResult>> entrySet() {
    return namedList.entrySet();
  }
}
