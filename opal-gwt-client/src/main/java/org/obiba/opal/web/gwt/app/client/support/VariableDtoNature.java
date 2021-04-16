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
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

/**
 * Determines the nature of a variable by inspecting its {@code ValueType} and any associated {@code Category} instance.
 */
public enum VariableDtoNature {
  /**
   * A categorical variable: its value can take one of the predefined {@code Category}.
   */
  CATEGORICAL,

  /**
   * A continuous variable: its value can take any value of it's {@code ValueType}. Some values may have a particular
   * meaning: they indicate a missing value. These are defined as missing {@code Category} instances.
   */
  CONTINUOUS,

  /**
   * A temporal variable: it's value is a date or time.
   */
  TEMPORAL,

  /**
   * A geo variable: it's value is a point, line or polygon.
   */
  GEO,

  /**
   * A bnary variable.
   */
  BINARY,

  /**
   * None of the above. Variables with {@code LocaleType} will be of this nature.
   */
  UNDETERMINED;

  public static VariableDtoNature getNature(VariableDto variable) {

    if(JsArrays.toSafeArray(variable.getCategoriesArray()).length() > 0) {
      return areAllCategoriesMissing(variable) ? CONTINUOUS : CATEGORICAL;
    }
    if("integer".equals(variable.getValueType()) || "decimal".equals(variable.getValueType())) {
      return CONTINUOUS;
    }
    if("date".equals(variable.getValueType()) || "datetime".equals(variable.getValueType())) {
      return TEMPORAL;
    }
    if("boolean".equals(variable.getValueType())) {
      return CATEGORICAL;
    }
    if("polygon".equals(variable.getValueType()) || "linestring".equals(variable.getValueType()) ||
        "point".equals(variable.getValueType())) {
      return GEO;
    }
    if("binary".equals(variable.getValueType())) {
      return BINARY;
    }
    return UNDETERMINED;
  }

  private static boolean areAllCategoriesMissing(VariableDto variable) {
    for(CategoryDto categoryDto : JsArrays.toIterable(variable.getCategoriesArray())) {
      if(!categoryDto.getIsMissing()) {
        return false;
      }
    }
    return true;
  }

}
