package org.obiba.opal.r.magma.util;

import java.util.List;

public class DoubleRange extends NumberRange {

  public DoubleRange(List<String> cats, List<String> missingCats) {
    super(cats, missingCats);
  }

  public double getMin() {
    return naRange.get(0).doubleValue();
  }

  public double getMax() {
    return naRange.get(naRange.size() - 1).doubleValue();
  }

  @Override
  protected Number makeNumber(String valStr) {
    try {
      return Double.parseDouble(valStr);
    } catch(NumberFormatException e) {
      return null;
    }
  }
}
