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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.math.stat.IntervalFrequency;
import org.obiba.magma.type.IntegerType;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 */
public class ContinuousVariableSummary extends AbstractVariableSummary implements Serializable {

  private static final long serialVersionUID = -8679001175321206239L;

  public static final int DEFAULT_INTERVALS = 10;

  private static final ImmutableList<Double> DEFAULT_PERCENTILES = ImmutableList
      .of(0.05d, 0.5d, 5d, 10d, 15d, 20d, 25d, 30d, 35d, 40d, 45d, 50d, 55d, 60d, 65d, 70d, 75d, 80d, 85d, 90d, 95d,
          99.5d, 99.95d);

  @Nonnull
  private final Distribution distribution;

  @Nonnull
  private List<Double> defaultPercentiles = DEFAULT_PERCENTILES;

  private int intervals = DEFAULT_INTERVALS;

  // Holds missing categories
  // (the case of continuous variables that have "special" values such as 8888 or 9999 that indicate a missing value)
  @Nonnull
  private final Set<Value> missing = Sets.newHashSet();

  @Nonnull
  private final DescriptiveStatistics descriptiveStats = new DescriptiveStatistics();

  @Nonnull
  private final List<Double> percentiles = Lists.newArrayList();

  @Nonnull
  private final Collection<Double> distributionPercentiles = Lists.newArrayList();

  @Nonnull
  private final Collection<IntervalFrequency.Interval> intervalFrequencies = Lists.newArrayList();

  @Override
  public String getCacheKey(ValueTable table) {
    return ContinuousVariableSummaryFactory
        .getCacheKey(variable, table, distribution, percentiles, intervals, getOffset(), getLimit());
  }

  private ContinuousVariableSummary(@Nonnull Variable variable, @Nonnull Distribution distribution) {
    super(variable);

    Assert.notNull(distribution, "Distribution cannot be null");
    Assert.isTrue(variable.getValueType().isNumeric(), "Continuous variables must be numeric");

    this.distribution = distribution;

    if(variable.hasCategories()) {
      for(Category c : variable.getCategories()) {
        if(c.isMissing()) {
          missing.add(variable.getValueType().valueOf(c.getName()));
        }
      }
    }
  }

  @Nonnull
  public Distribution getDistribution() {
    return distribution;
  }

  public int getIntervals() {
    return intervals;
  }

  @Nonnull
  public DescriptiveStatistics getDescriptiveStats() {
    return descriptiveStats;
  }

  @Nonnull
  public List<Double> getPercentiles() {
    return percentiles;
  }

  @Nonnull
  public Collection<Double> getDistributionPercentiles() {
    return distributionPercentiles;
  }

  @Nonnull
  public Collection<IntervalFrequency.Interval> getIntervalFrequencies() {
    return intervalFrequencies;
  }

  @Nonnull
  public List<Double> getDefaultPercentiles() {
    return defaultPercentiles;
  }

  public enum Distribution {
    normal {
      @Nullable
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        return ds.getStandardDeviation() > 0
            ? new NormalDistributionImpl(ds.getMean(), ds.getStandardDeviation())
            : null;
      }
    },
    exponential {
      @Nonnull
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        return new ExponentialDistributionImpl(ds.getMean());
      }
    };

    @Nullable
    abstract ContinuousDistribution getDistribution(DescriptiveStatistics ds);

  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  public static class Builder implements VariableSummaryBuilder<ContinuousVariableSummary> {

    private final ContinuousVariableSummary summary;

    @Nonnull
    private final Variable variable;

    private boolean addedTable;

    private boolean addedValue;

    public Builder(@Nonnull Variable variable, @Nonnull Distribution distribution) {
      this.variable = variable;
      summary = new ContinuousVariableSummary(variable, distribution);
    }

    public Builder intervals(int intervals) {
      summary.intervals = intervals;
      return this;
    }

    public Builder defaultPercentiles(@Nullable List<Double> defaultPercentiles) {
      summary.defaultPercentiles = defaultPercentiles == null || defaultPercentiles.isEmpty()
          ? DEFAULT_PERCENTILES
          : defaultPercentiles;
      return this;
    }

    public Builder filter(Integer offset, Integer limit) {
      summary.setOffset(offset);
      summary.setLimit(limit);
      return this;
    }

    public Builder addValue(@Nonnull Value value) {
      if(addedTable) {
        throw new IllegalStateException("Cannot add value for variable " + summary.getVariable().getName() +
            " because values where previously added from the whole table with addTable().");
      }
      add(value);
      addedValue = true;
      return this;
    }

    public Builder addTable(@Nonnull ValueTable table, @Nonnull ValueSource variableValueSource) {
      if(addedValue) {
        throw new IllegalStateException("Cannot add table for variable " + summary.getVariable().getName() +
            " because values where previously added with addValue().");
      }
      add(table, variableValueSource);
      addedTable = true;

      return this;
    }

    private void add(@Nonnull ValueTable table, @Nonnull ValueSource variableValueSource) {
      Assert.notNull(variable, "ValueTable cannot be null");
      Assert.notNull(variableValueSource, "VariableValueSource cannot be null");

      VectorSource vectorSource = variableValueSource.asVectorSource();
      if(vectorSource == null) return;
      for(Value value : vectorSource.getValues(summary.getVariableEntities(table))) {
        add(value);
      }
    }

    @SuppressWarnings("ConstantConditions")
    private void add(@Nonnull Value value) {
      Assert.notNull(variable, "Value cannot be null");
      if(!value.isNull() && !summary.missing.contains(value)) {
        if(value.isSequence()) {
          for(Value v : value.asSequence().getValue()) {
            add(v);
          }
        } else {
          summary.descriptiveStats.addValue(((Number) value.getValue()).doubleValue());
        }
      }
    }

    @SuppressWarnings("MagicNumber")
    private void compute() {
      double variance = summary.descriptiveStats.getVariance();
      if(Double.isNaN(variance) || Double.isInfinite(variance) || variance <= 0) return;

      IntervalFrequency intervalFrequency = new IntervalFrequency(summary.descriptiveStats.getMin(),
          summary.descriptiveStats.getMax(), summary.intervals,
          summary.getVariable().getValueType() == IntegerType.get());
      for(double d : summary.descriptiveStats.getSortedValues()) {
        intervalFrequency.add(d);
      }

      for(IntervalFrequency.Interval interval : intervalFrequency.intervals()) {
        summary.intervalFrequencies.add(interval);
      }

      ContinuousDistribution cd = summary.distribution.getDistribution(summary.descriptiveStats);
      for(Double p : summary.defaultPercentiles) {
        summary.percentiles.add(summary.descriptiveStats.getPercentile(p));
        try {
          if(cd != null) summary.distributionPercentiles.add(cd.inverseCumulativeProbability(p / 100d));
        } catch(MathException ignored) {
        }
      }
    }

    @Override
    @Nonnull
    public ContinuousVariableSummary build() {
      compute();
      return summary;
    }
  }

}