package org.obiba.opal.r.magma.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class NumberRange {
  private final List<String> cats;
  private final List<String> missingCats;

  protected List<Number> naValues;
  protected List<Number> naRange;

  public NumberRange(List<String> cats, List<String> missingCats) {
    this.cats = cats;
    this.missingCats = missingCats;
    this.naValues = missingCats.stream().map(this::makeNumber).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    this.naRange = Lists.newArrayList();

    // find longuest range
    List<Number> nums = cats.stream().map(this::makeNumber).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    List<Number> tmpRange = Lists.newArrayList();

    for (Number value : nums) {
      if (naValues.contains(value)) { // is a missing
        tmpRange.add(value);
        // end range if last value
        if (nums.indexOf(value) == nums.size() - 1 && tmpRange.size() > naRange.size())
          naRange = Lists.newArrayList(tmpRange);
      } else if (!tmpRange.isEmpty()) { // end range if next value is not a missing
        if (tmpRange.size() > naRange.size()) naRange = Lists.newArrayList(tmpRange);
        tmpRange.clear();
      }
    }

    for (Number value : naRange) {
      naValues.remove(value);
    }

    List<String> toRemove = Lists.newArrayList();
    for (String cat : missingCats) {
      if (!naValues.contains(makeNumber(cat))) {
        toRemove.add(cat);
      }
    }
    missingCats.removeAll(toRemove);
  }

  public boolean hasRange() {
    return !naRange.isEmpty();
  }

  public Number getRangeMin() {
    return hasRange() ? naRange.get(0) : null;
  }

  public Number getRangeMax() {
    return hasRange() ? naRange.get(naRange.size() - 1) : null;
  }

  protected abstract Number makeNumber(String valStr);

  @Override
  public String toString() {
    return "na_range=" + naRange + " na_values=" + naValues;
  }
}