/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.collect.Lists;
import org.json.JSONObject;
import org.obiba.magma.AbstractVariableValueSource;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.DateType;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractRVariableValueSource extends AbstractVariableValueSource {

  protected Value getValue(List<Object> objValues) {
    if (objValues == null || objValues.size() == 0)
      return getVariable().isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getVariable().isRepeatable() ?
        getValueType().sequenceOf(Lists.newArrayList(objValues).stream().map(this::getSingleValue).collect(Collectors.toList())) :
        getSingleValue(objValues.get(0));
  }

  //
  // Private methods
  //

  private Value getSingleValue(Object objValue) {
    if (JSONObject.NULL.equals(objValue))
      return getValueType().nullValue();
    if (getVariable().getValueType().equals(DateType.get()))
      return getDateValue(objValue);
    if (getVariable().getValueType().equals(DateTimeType.get()))
      return getDateTimeValue(objValue);
    if (getVariable().getValueType().isNumeric())
      return getNumericValue(objValue);
    return getValueType().valueOf(objValue);
  }

  private Value getNumericValue(Object objValue) {
    if (objValue == null ||
        (objValue instanceof Double && ((Double) objValue).isNaN())) return getValueType().nullValue();
    return getValueType().valueOf(objValue);
  }

  /**
   * R dates are serialized as a number of days since epoch (1970-01-01).
   *
   * @param objValue
   * @return
   */
  private Value getDateValue(Object objValue) {
    if (objValue == null || "NaN".equals(objValue)) return getValueType().nullValue();
    try {
      switch (objValue) {
        case Float flt -> {
          if (flt.isNaN()) return getValueType().nullValue();
          Date value = new Date(flt.longValue() * 24 * 3600 * 1000);
          return getValueType().valueOf(value);
        }
        case Double dbl -> {
          if (dbl.isNaN()) return getValueType().nullValue();
          Date value = new Date(dbl.longValue() * 24 * 3600 * 1000);
          return getValueType().valueOf(value);
        }
        case Integer ii -> {
          Date value = new Date(ii.longValue() * 24 * 3600 * 1000);
          return getValueType().valueOf(value);
        }
        case Long lng -> {
          Date value = new Date(lng * 1000);
          return getValueType().valueOf(value);
        }
        default -> {
          return getValueType().valueOf(objValue);
        }
      }
    } catch (Exception e) {
      return getValueType().nullValue();
    }
  }

  private Value getDateTimeValue(Object objValue) {
    if (objValue == null || "NaN".equals(objValue)) return getValueType().nullValue();
    try {
      switch (objValue) {
        case Float flt -> {
          if (flt.isNaN()) return getValueType().nullValue();
          Date value = new Date(flt.longValue() * 1000);
          return getValueType().valueOf(value);
        }
        case Double dbl -> {
          if (dbl.isNaN()) return getValueType().nullValue();
          Date value = new Date(dbl.longValue() * 1000);
          return getValueType().valueOf(value);
        }
        case Integer ii -> {
          Date value = new Date(ii.longValue() * 1000);
          return getValueType().valueOf(value);
        }
        case Long lng -> {
          Date value = new Date(lng * 1000);
          return getValueType().valueOf(value);
        }
        default -> {
          return getValueType().valueOf(objValue);
        }
      }
    } catch (Exception e) {
      return getValueType().nullValue();
    }
  }

}
