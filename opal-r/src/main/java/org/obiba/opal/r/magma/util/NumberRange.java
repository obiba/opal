/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma.util;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class NumberRange implements Range {
  private final List<String> missingCats;

  protected List<Number> naValues;
  protected List<Number> naRange;

  public NumberRange(List<String> cats, List<String> missingCats) {
    this.missingCats = missingCats;
    this.naValues = missingCats.stream().map(this::makeNumber).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    this.naRange = Lists.newArrayList();

    // find longest range
    List<Number> nums = cats.stream().map(this::makeNumber).filter(Objects::nonNull).sorted().toList();
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

  @Override
  public boolean hasRange() {
    return !naRange.isEmpty();
  }

  @Override
  public String getRangeMin() {
    return hasRange() ? naRange.getFirst().toString() : null;
  }

  @Override
  public String getRangeMax() {
    return hasRange() ? naRange.getLast().toString() : null;
  }

  @Override
  public List<String> getMissingCats() {
    return missingCats;
  }

  protected abstract Number makeNumber(String valStr);

  @Override
  public String toString() {
    return "na_range=" + naRange + " na_values=" + naValues;
  }
}