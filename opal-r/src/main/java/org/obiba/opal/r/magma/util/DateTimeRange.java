package org.obiba.opal.r.magma.util;

import com.google.common.collect.Lists;
import org.obiba.magma.Value;
import org.obiba.magma.type.DateTimeType;

import java.text.SimpleDateFormat;
import java.util.List;

public class DateTimeRange implements Range {
  private static final SimpleDateFormat NO_TIMEZONE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private List<String> naValues;
  private List<Value> naRange;

  public DateTimeRange(List<String> missingCats) {
    this.naValues = Lists.newArrayList();
    this.naRange = Lists.newArrayList();
    missingCats.forEach((name) -> {
      Value val = makeDate(name);
      if (val != null && !val.isNull()) {
        naValues.add(name);
        naRange.add(val);
      }
    });
    naValues = naValues.stream().sorted().toList();
    naRange = naRange.stream().sorted().toList();
  }

  @Override
  public boolean hasRange() {
    return !naRange.isEmpty();
  }

  @Override
  public String getRangeMin() {
    return String.format("'%s'", NO_TIMEZONE.format(naRange.getFirst().getValue()));
  }

  @Override
  public String getRangeMax() {
    return String.format("'%s'", NO_TIMEZONE.format(naRange.getLast().getValue()));
  }

  @Override
  public List<String> getMissingCats() {
    return naValues;
  }

  @Override
  public String toString() {
    return "na_values=" + naValues;
  }

  private Value makeDate(String valStr) {
    try {
      return DateTimeType.get().valueOf(valStr);
    } catch (Exception e) {
      return null;
    }
  }
}
