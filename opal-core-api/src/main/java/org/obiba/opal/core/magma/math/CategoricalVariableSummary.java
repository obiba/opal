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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.validation.constraints.NotNull;

import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.type.BooleanType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 *
 */
public class CategoricalVariableSummary extends AbstractVariableSummary implements Serializable {

  private static final long serialVersionUID = 203198842420473154L;

  private static final Logger log = LoggerFactory.getLogger(CategoricalVariableSummary.class);

  public static final String NULL_NAME = "N/A";

  private final org.apache.commons.math3.stat.Frequency frequencyDist = new org.apache.commons.math3.stat.Frequency();

  /**
   * Mode is the most frequent value
   */
  private String mode = NULL_NAME;

  private long n;

  private boolean distinct;

  private boolean empty = true;

  private final Collection<Frequency> frequencies = new ArrayList<Frequency>();

  private CategoricalVariableSummary(@NotNull Variable variable) {
    super(variable);
  }

  @Override
  public String getCacheKey(ValueTable table) {
    return CategoricalVariableSummaryFactory.getCacheKey(variable, table, distinct, getOffset(), getLimit());
  }

  @NotNull
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

  public boolean isEmpty() {
    return empty;
  }

  public static class Frequency implements Serializable {

    private static final long serialVersionUID = -2876592652764310324L;

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
  public static class Builder implements VariableSummaryBuilder<CategoricalVariableSummary> {

    private final CategoricalVariableSummary summary;

    @NotNull
    private final Variable variable;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@NotNull Variable variable) {
      this.variable = variable;
      summary = new CategoricalVariableSummary(variable);
    }

    public Builder addValue(@NotNull Value value) {
      if(addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.variable.getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    public Builder addTable(@NotNull ValueTable table, @NotNull ValueSource valueSource) {
      if(addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.variable.getName() +
            " because values where previously added with addValue().");
      }
      add(table, valueSource);
      addedTable = true;

      return this;
    }

    private void add(@NotNull ValueTable table, @NotNull ValueSource variableValueSource) {
      Assert.notNull(table, "ValueTable cannot be null");
      Assert.notNull(variableValueSource, "variableValueSource cannot be null");

      VectorSource vectorSource = variableValueSource.asVectorSource();
      if(vectorSource == null) return;
      for(Value value : vectorSource.getValues(summary.getVariableEntities(table))) {
        add(value);
      }
    }

    private void add(@NotNull Value value) {
      Assert.notNull(value, "Value cannot be null");
      if(summary.empty) summary.empty = false;
      if(value.isSequence()) {
        if(value.isNull()) {
          summary.frequencyDist.addValue(NULL_NAME);
        } else {
          //noinspection ConstantConditions
          for(Value v : value.asSequence().getValue()) {
            add(v);
          }
        }
      } else {
        summary.frequencyDist.addValue(value.isNull() ? NULL_NAME : value.toString());
      }
    }

    /**
     * Returns an iterator of frequencyDist names
     */
    private Iterator<String> freqNames(org.apache.commons.math3.stat.Frequency freq) {
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
      if(variable.getValueType().equals(BooleanType.get())) {
        return ImmutableList.<String>builder() //
            .add(BooleanType.get().trueValue().toString()) //
            .add(BooleanType.get().falseValue().toString()).build().iterator();
      }

      return Iterables.transform(variable.getCategories(), new Function<Category, String>() {

        @Override
        public String apply(Category from) {
          return from.getName();
        }

      }).iterator();
    }

    private void compute() {
      log.trace("Start compute categorical {}", summary.variable);
      long max = 0;
      Iterator<String> concat = summary.distinct //
          ? freqNames(summary.frequencyDist)  // category names, null values and distinct values
          : Iterators.concat(categoryNames(), ImmutableList.of(NULL_NAME).iterator()); // category names and null values

      // Iterate over all category names including or not distinct values.
      // The loop will also determine the mode of the distribution (most frequent value)
      while(concat.hasNext()) {
        String value = concat.next();
        long count = summary.frequencyDist.getCount(value);
        if(count > max) {
          max = count;
          summary.mode = value;
        }
        summary.frequencies
            .add(new Frequency(value, summary.frequencyDist.getCount(value), summary.frequencyDist.getPct(value)));
      }
      summary.n = summary.frequencyDist.getSumFreq();
    }

    public Builder distinct(boolean distinct) {
      summary.setDistinct(distinct);
      return this;
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    @Override
    @NotNull
    public CategoricalVariableSummary build() {
      compute();
      return summary;
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return variable;
    }

  }
}
