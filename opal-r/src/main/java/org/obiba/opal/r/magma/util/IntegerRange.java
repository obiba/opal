package org.obiba.opal.r.magma.util;

import java.util.List;

public class IntegerRange extends NumberRange {

  public IntegerRange(List<String> cats, List<String> missingCats) {
    super(cats, missingCats);
  }

  public int getMin() {
    return naRange.get(0).intValue();
  }

  public int getMax() {
    return naRange.get(naRange.size() - 1).intValue();
  }

  @Override
  protected Number makeNumber(String valStr) {
    try {
      return Integer.parseInt(valStr);
    } catch(NumberFormatException e) {
      return null;
    }
  }
}