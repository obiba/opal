/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.domain;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public interface HasUniqueProperties {

  List<String> getUniqueProperties();

  List<Object> getUniqueValues();

  default Map<String, Object> getProperties() {
    Map<String, Object> props = Maps.newHashMap();
    List<String> uniqueProps = getUniqueProperties();
    List<Object> uniqueValues = getUniqueValues();
    for (int i = 0; i < uniqueProps.size(); i++) {
      props.put(uniqueProps.get(i), uniqueValues.get(i));
    }
    return props;
  }
}
