/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.ExponentialDistributionImpl;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.math.stat.IntervalFrequency;
import org.obiba.magma.math.stat.IntervalFrequency.Interval;
import org.obiba.magma.type.IntegerType;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.DescriptiveStatsDto;
import org.obiba.opal.web.model.Math.IntervalFrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class ContinuousSummaryStatisticsResource extends AbstractSummaryStatisticsResource {

  // Holds missing categories (the case of continuous variables that have "special" values such as 8888 or 9999 that
  // indicate a missing value)
  private final Set<Value> missing = Sets.newHashSet();

  public ContinuousSummaryStatisticsResource(ValueTable valueTable, Variable variable, VectorSource vectorSource) {
    super(valueTable, variable, vectorSource);
    if(variable.getValueType().isNumeric() == false)
      throw new IllegalArgumentException("continuous variables must be numeric");

    if(variable.hasCategories()) {
      for(Category c : variable.getCategories()) {
        if(c.isMissing()) {
          missing.add(variable.getValueType().valueOf(c.getName()));
        }
      }
    }
  }

  @GET
  @POST
  public Response compute(@QueryParam("d") @DefaultValue("normal") Distribution distribution,
      @QueryParam("p") List<Double> percentiles, @QueryParam("intervals") @DefaultValue("10") int intervals) {
    List<Double> percentilesOrDefault = null;
    if(percentiles != null && !percentiles.isEmpty()) {
      percentilesOrDefault = percentiles;
    } else { // default
      percentilesOrDefault = ImmutableList
          .<Double>of(0.05d, 0.5d, 5d, 10d, 15d, 20d, 25d, 30d, 35d, 40d, 45d, 50d, 55d, 60d, 65d, 70d, 75d, 80d, 85d,
              90d, 95d, 99.5d, 99.95d);
    }

    SummaryStatisticsDto entity = SummaryStatisticsDto.newBuilder()//
        .setResource(getVariable().getName())//
        .setExtension(ContinuousSummaryDto.continuous,
            distribution.calc(getVariable().getValueType(), missing, getValues(), percentilesOrDefault, intervals)
                .build()).build();

    return TimestampedResponses.ok(getValueTable(), entity).build();
  }

  public enum Distribution {
    normal {
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        if(ds.getStandardDeviation() > 0) {
          return new NormalDistributionImpl(ds.getMean(), ds.getStandardDeviation());
        }
        return null;
      }
    },
    exponential {
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        return new ExponentialDistributionImpl(ds.getMean());
      }
    };

    abstract ContinuousDistribution getDistribution(DescriptiveStatistics ds);

    public ContinuousSummaryDto.Builder calc(ValueType type, Set<Value> missing, Iterable<Value> values,
        List<Double> percentiles, int intervals) {
      DescriptiveStatistics ds = new DescriptiveStatistics();
      for(Value value : values) {
        addValue(ds, missing, value);
      }

      DescriptiveStatsDto.Builder builder = DescriptiveStatsDto.newBuilder().setMin(ds.getMin()).setMax(ds.getMax())
          .setN(ds.getN()).setMean(ds.getMean()).setSum(ds.getSum()).setSumsq(ds.getSumsq())
          .setStdDev(ds.getStandardDeviation()).setVariance(ds.getVariance()).setSkewness(ds.getSkewness())
          .setGeometricMean(ds.getGeometricMean()).setKurtosis(ds.getKurtosis()).setMedian(ds.apply(new Median()));
      ContinuousSummaryDto.Builder continuous = ContinuousSummaryDto.newBuilder();
      if(ds.getVariance() > 0) {
        IntervalFrequency bf = new IntervalFrequency(ds.getMin(), ds.getMax(), intervals, type == IntegerType.get());
        for(double d : ds.getSortedValues()) {
          bf.add(d);
        }

        for(Interval interval : bf.intervals()) {
          continuous.addIntervalFrequency(
              IntervalFrequencyDto.newBuilder().setLower(interval.getLower()).setUpper(interval.getUpper())
                  .setFreq(interval.getFreq()).setDensity(interval.getDensity())
                  .setDensityPct(interval.getDensityPct()));
        }

        ContinuousDistribution cd = getDistribution(ds);
        for(Double p : percentiles) {
          builder.addPercentiles(ds.getPercentile(p));
          try {
            continuous.addDistributionPercentiles(cd.inverseCumulativeProbability(p / 100d));
          } catch(MathException e) {
          }
        }
      }
      return continuous.setSummary(builder);
    }

    private void addValue(DescriptiveStatistics ds, Set<Value> missing, Value value) {
      if(value.isNull() == false && missing.contains(value) == false) {
        if(value.isSequence()) {
          for(Value v : value.asSequence().getValue()) {
            addValue(ds, missing, v);
          }
        } else {
          ds.addValue(((Number) value.getValue()).doubleValue());
        }
      }
    }

  }

}
