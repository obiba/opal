package org.obiba.opal.core.domain;

import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;

/**
 * Determines the nature of a variable by inspecting its {@code ValueType} and any associated {@code Category} instance.
 */
public enum VariableNature {

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
   * A binary variable: it's a binary file
   */
  BINARY,

  /**
   * None of the above. Variables with {@code LocaleType} will be of this nature.
   */
  UNDETERMINED;

  public static VariableNature getNature(Variable variable) {
    if(variable.hasCategories()) {
      return variable.areAllCategoriesMissing() && variable.getValueType().isNumeric() ? CONTINUOUS : CATEGORICAL;
    }
    if(variable.getValueType().isNumeric()) {
      return CONTINUOUS;
    }
    if(variable.getValueType().isDateTime()) {
      return TEMPORAL;
    }
    if(variable.getValueType().equals(BooleanType.get())) {
      return CATEGORICAL;
    }
    if(variable.getValueType().isGeo()) {
      return GEO;
    }
    if(variable.getValueType().isBinary()) {
      return BINARY;
    }

    return UNDETERMINED;
  }

}
