package org.obiba.opal.r.magma.util;

import org.obiba.magma.Value;
import org.obiba.magma.type.DateType;

import java.util.List;
import java.util.Objects;

public class DateRange implements Range {
  private final List<String> missingCats;

  private final List<Value> naValues;

  public DateRange(List<String> missingCats) {
    this.missingCats = missingCats;
    this.naValues = missingCats.stream().map(this::makeDate).filter(Objects::nonNull).filter(val -> !val.isNull()).sorted().toList();
  }

  @Override
  public boolean hasRange() {
    return !naValues.isEmpty();
  }

  @Override
  public String getRangeMin() {
    return naValues.getFirst().toString();
  }

  @Override
  public String getRangeMax() {
    return naValues.getLast().toString();
  }

  @Override
  public List<String> getMissingCats() {
    return missingCats;
  }

  @Override
  public String toString() {
    return "na_values=" + naValues;
  }

  private Value makeDate(String valStr) {
    try {
      return DateType.get().valueOf(valStr);
    } catch (Exception e) {
      return null;
    }
  }
}
