/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ContinuousDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.Frequency;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.views.ListClause;
import org.obiba.magma.views.View;
import org.obiba.magma.views.ohs.ExcelVariablesClause;
import org.obiba.opal.web.model.Magma.DescriptiveStatsDto;
import org.obiba.opal.web.model.Magma.FrequencyDto;
import org.obiba.opal.web.model.Magma.ValueDto;
import org.obiba.opal.web.model.Magma.VariableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class VariableResource {

  private static final Logger log = LoggerFactory.getLogger(VariableResource.class);

  private final ValueTable valueTable;

  private final VariableValueSource vvs;

  public VariableResource(ValueTable valueTable, VariableValueSource vvs) {
    this.valueTable = valueTable;
    this.vvs = vvs;
  }

  @GET
  @Produces("application/xml")
  public Variable get() {
    return vvs.getVariable();
  }

  @PUT
  public Response write(VariableDto dto) throws IOException {
    log.info("writing to {}", valueTable.getDatasource().getName() + "." + valueTable.getName() + ":" + dto.getName());
    if(valueTable instanceof View) {
      View view = (View) valueTable;
      log.info("writing to view.");
      ListClause clause = view.getListClause();
      if(clause instanceof ExcelVariablesClause) {
        ExcelVariablesClause excelClause = (ExcelVariablesClause) clause;
        log.info("writing to ExcelVariablesClause {}", excelClause.getTable());
        ValueTableWriter writer = excelClause.getDatasource().createWriter(excelClause.getTable(), valueTable.getEntityType());
        VariableWriter vw = writer.writeVariables();
        vw.writeVariable(Dtos.fromDto(dto));
        vw.close();
        writer.close();
      }
      // This is a HUGE hack. We have to do this so that the list clause re-reads the variables.
      // This would not be required if we were writing through the View or the ListClause itself.
      Disposables.dispose(clause);
      Initialisables.initialise(clause);
    }
    return Response.ok().build();
  }

  @GET
  @Path("/values")
  public Collection<ValueDto> getValues(@QueryParam("limit") @DefaultValue("10") Integer limit) {
    ImmutableList.Builder<ValueDto> values = ImmutableList.builder();
    VectorSource vectorSource = vvs.asVectorSource();
    if(vectorSource != null) {
      int i = 0;
      for(Value value : vectorSource.getValues(Sets.newTreeSet(valueTable.getVariableEntities()))) {
        ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(vvs.getValueType().getName()).setIsSequence(value.isSequence());
        if(value.isNull() == false) {
          valueBuilder.setValue(value.toString());
        }
        values.add(valueBuilder.build());
        if(limit >= 0 && i++ >= limit) break;
      }
    }
    return values.build();
  }

  @GET
  @Path("/frequencies")
  public Collection<FrequencyDto> getDataTable() {
    VectorSource vectorSource = vvs.asVectorSource();
    if(vectorSource != null) {
      Frequency freq = new Frequency();
      for(Value value : vectorSource.getValues(Sets.newTreeSet(valueTable.getVariableEntities()))) {
        if(value.isNull() == false) {
          freq.addValue(value.toString());
        } else {
          freq.addValue("N/A");
        }
      }

      ImmutableList.Builder<FrequencyDto> dtos = ImmutableList.builder();
      for(Category c : vvs.getVariable().getCategories()) {
        dtos.add(FrequencyDto.newBuilder().setName(c.getName()).setValue((int) freq.getCount(c.getName())).setPct(freq.getPct(c.getName())).build());
      }
      dtos.add(FrequencyDto.newBuilder().setName("N/A").setValue((int) freq.getCount("N/A")).setPct(freq.getPct("N/A")).build());
      return dtos.build();

    }
    return ImmutableList.of();
  }

  @GET
  @Path("/univariate")
  // Can we find a better name for this resource?
  public DescriptiveStatsDto getUnivariateAnalysis(@QueryParam("d") @DefaultValue("normal") Distribution distribution, @QueryParam("p") Double[] percentiles) {
    VectorSource vectorSource = vvs.asVectorSource();
    if(vectorSource != null) {
      return distribution.calc(vectorSource.getValues(Sets.newTreeSet(valueTable.getVariableEntities())), percentiles).build();
    }
    throw new UnsupportedOperationException();
  }

  public static enum Distribution {
    normal {
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        if(ds.getStandardDeviation() > 0) {
          return new NormalDistributionImpl(ds.getMean(), ds.getStandardDeviation());
        }
        return null;
      }
    },
    lognormal {
      @Override
      public ContinuousDistribution getDistribution(DescriptiveStatistics ds) {
        return new NormalDistributionImpl(ds.getMean(), ds.getStandardDeviation());
      }
    };;
    abstract ContinuousDistribution getDistribution(DescriptiveStatistics ds);
    
    public DescriptiveStatsDto.Builder calc(Iterable<Value> values, Double[] percentiles) {
      DescriptiveStatistics ds = new DescriptiveStatistics();
      for(Value value : values) {
        if(value.isNull() == false) {
          ds.addValue(((Number) value.getValue()).doubleValue());
        }
      }
      ContinuousDistribution cd = getDistribution(ds);
      return percentiles(ds, cd, DescriptiveStatsDto.newBuilder().setMin(ds.getMin()).setMax(ds.getMax()).setN(ds.getN()).setMean(ds.getMean()).setSum(ds.getSum()).setSumsq(ds.getSumsq()).setStdDev(ds.getStandardDeviation()).setVariance(ds.getVariance()).setSkewness(ds.getSkewness()).setGeometricMean(ds.getGeometricMean()).setKurtosis(ds.getKurtosis()), percentiles);
    }

    private DescriptiveStatsDto.Builder percentiles(DescriptiveStatistics ds, ContinuousDistribution cd, DescriptiveStatsDto.Builder builder, Double[] percentiles) {
      if(percentiles != null) {
        for(Double p : percentiles) {
          builder.addPercentiles(ds.getPercentile(p));
          if(cd != null) {
            try {
              builder.addDistributionPercentiles(cd.inverseCumulativeProbability(p/100d));
            } catch(MathException e) {
              log.error("oops", e);
            }
          }
        }
      }
      return builder;
    }
  }

}
