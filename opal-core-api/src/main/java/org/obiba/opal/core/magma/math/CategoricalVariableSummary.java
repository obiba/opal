/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.type.BooleanType;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 *
 */
public class CategoricalVariableSummary {

  public static final String NULL_NAME = "N/A";

  private final org.apache.commons.math.stat.Frequency frequencyDist = new org.apache.commons.math.stat.Frequency();

  @Nonnull
  private final Variable variable;

  /**
   * Mode is the most frequent value
   */
  private String mode = NULL_NAME;

  private long n;

  private boolean distinct;

  private final Collection<Frequency> frequencies = new ArrayList<Frequency>();

  private CategoricalVariableSummary(@Nonnull Variable variable) {
    this.variable = variable;
    Assert.notNull(variable, "Variable cannot be null");
  }

  private void add(@Nonnull ValueTable table, @Nonnull ValueSource variableValueSource) {
    Assert.notNull(variable, "ValueTable cannot be null");
    Assert.notNull(variableValueSource, "variableValueSource cannot be null");

    VectorSource vectorSource = variableValueSource.asVectorSource();
    if(vectorSource == null) return;
    for(Value value : vectorSource.getValues(Sets.newTreeSet(table.getVariableEntities()))) {
      add(value);
    }
  }

  private void add(@Nonnull Value value) {
    Assert.notNull(variable, "Value cannot be null");

    if(value.isSequence()) {
      if(value.isNull()) {
        frequencyDist.addValue(NULL_NAME);
      } else {
        //noinspection ConstantConditions
        for(Value v : value.asSequence().getValue()) {
          add(v);
        }
      }
    } else {
      frequencyDist.addValue(value.isNull() ? NULL_NAME : value.toString());
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void compute() {
    long max = 0;
    Iterator<String> concat = distinct //
        ? freqNames(frequencyDist)  // category names, null values and distinct values
        : Iterators.concat(categoryNames(), ImmutableList.of(NULL_NAME).iterator()); // category names and null values

    // Iterate over all category names including or not distinct values.
    // The loop will also determine the mode of the distribution (most frequent value)
    while(concat.hasNext()) {
      String value = concat.next();
      long count = frequencyDist.getCount(value);
      if(count > max) {
        max = count;
        mode = value;
      }
      frequencies.add(new Frequency(value, frequencyDist.getCount(value), frequencyDist.getPct(value)));
    }
    n = frequencyDist.getSumFreq();
  }

  /**
   * Returns an iterator of frequencyDist names
   */
  private Iterator<String> freqNames(org.apache.commons.math.stat.Frequency freq) {
    return Iterators.transform(freq.valuesIterator(), new Function<Comparable<?>, String>() {

      @Override
      public String apply(Comparable<?> input) {
        return input.toString();
      }
    });
  }

  /**
   * Returns an iterator of category names
   */
  private Iterator<String> categoryNames() {
    if(getVariable().getValueType().equals(BooleanType.get())) {
      return ImmutableList.<String>builder() //
          .add(BooleanType.get().trueValue().toString()) //
          .add(BooleanType.get().falseValue().toString()).build().iterator();
    }

    return Iterables.transform(getVariable().getCategories(), new Function<Category, String>() {

      @Override
      public String apply(Category from) {
        return from.getName();
      }

    }).iterator();
  }

  @Nonnull
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  public Iterable<Frequency> getFrequencies() {
    return ImmutableList.copyOf(frequencies);
  }

  public String getMode() {
    return mode;
  }

  public long getN() {
    return n;
  }

  public boolean isDistinct() {
    return distinct;
  }

  public void setDistinct(boolean distinct) {
    this.distinct = distinct;
  }

  public static class Frequency {

    private final String value;

    private final long freq;

    private final double pct;

    public Frequency(String value, long freq, double pct) {
      this.value = value;
      this.freq = freq;
      this.pct = pct;
    }

    public String getValue() {
      return value;
    }

    public long getFreq() {
      return freq;
    }

    public double getPct() {
      return pct;
    }
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder {

    private final CategoricalVariableSummary summary;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@Nonnull Variable variable) {
      summary = new CategoricalVariableSummary(variable);
    }

    public Builder addValue(@Nonnull Value value) {
      if(addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.getVariable().getName() +
            " because values where previously added from the whole table with addTable().");
      }
      summary.add(value);
      addedValue = true;
      return this;
    }

    public Builder addTable(@Nonnull ValueTable table, @Nonnull ValueSource variableValueSource) {
      if(addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.getVariable().getName() +
            " because values where previously added with addValue().");
      }
      summary.add(table, variableValueSource);
      addedTable = true;

      return this;
    }

    public Builder distinct(boolean distinct) {
      summary.setDistinct(distinct);
      return this;
    }

    @Nonnull
    public CategoricalVariableSummary build() {
      summary.compute();
      return summary;
    }

  }
}
