package org.obiba.opal.r.magma.util;

import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class DateTimeRange implements Range {
  private static final SimpleDateFormat NO_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private final List<String> missingCats;

  private final List<Value> naValues;

  public DateTimeRange(List<String> missingCats) {
    this.missingCats = missingCats;
    this.naValues = missingCats.stream().map(this::makeDate).filter(Objects::nonNull).filter(val -> !val.isNull()).sorted().toList();
  }

  @Override
  public boolean hasRange() {
    return !naValues.isEmpty();
  }

  @Override
  public String getRangeMin() {
    return NO_TIMEZONE.format(naValues.getFirst().getValue());
  }

  @Override
  public String getRangeMax() {
    return NO_TIMEZONE.format(naValues.getLast().getValue());
  }

  @Override
  public List<String> getMissingCats() {
    return missingCats;
  }


  @Override
  public String toString() {
    return "na_values=" + naValues.stream().map(val -> NO_TIMEZONE.format(val.getValue())).toList();
  }

  private Value makeDate(String valStr) {
    try {
      return DateTimeType.get().valueOf(valStr);
    } catch (Exception e) {
      return null;
    }
  }
}
