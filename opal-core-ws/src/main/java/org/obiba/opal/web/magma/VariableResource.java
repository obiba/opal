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
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
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
import com.google.common.collect.Maps;

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
    int i = 0;
    ImmutableList.Builder<ValueDto> values = ImmutableList.builder();
    for(ValueSet valueSet : valueTable.getValueSets()) {
      Value value = vvs.getValue(valueSet);
      ValueDto.Builder valueBuilder = ValueDto.newBuilder().setValueType(vvs.getValueType().getName()).setIsSequence(value.isSequence());
      if(value.isNull() == false) {
        valueBuilder.setValue(value.toString());
      }
      values.add(valueBuilder.build());
      if(i++ == limit) break;
    }
    return values.build();
  }

  @GET
  @Path("/frequencies.json")
  public Collection<FrequencyDto> getDataTable() {
    Map<String, FrequencyDto> frequencies = Maps.newLinkedHashMap();
    for(Category c : vvs.getVariable().getCategories()) {
      frequencies.put(c.getName(), FrequencyDto.newBuilder().setName(c.getName()).setValue(0).build());
    }
    frequencies.put("N/A", FrequencyDto.newBuilder().setName("N/A").setValue(0).build());
    for(ValueSet vs : valueTable.getValueSets()) {
      Value value = vvs.getValue(vs);
      if(value.isNull()) {
        count("N/A", frequencies);
      } else {
        count(value.toString(), frequencies);
      }
    }
    return frequencies.values();
  }

  @GET
  @Path("/univariate")
  // Can we find a better name for this resource?
  public DescriptiveStatsDto getUnivariateAnalysis() {
    DescriptiveStatistics ds = new DescriptiveStatistics();
    for(ValueSet vs : valueTable.getValueSets()) {
      Value value = vvs.getValue(vs);
      if(value.isNull() == false) {
        ds.addValue(((Number) value.getValue()).doubleValue());
      }
    }
    return DescriptiveStatsDto.newBuilder().setMin(ds.getMin()).setMax(ds.getMax()).setN(ds.getN()).setMean(ds.getMean()).setSum(ds.getSum()).setSumsq(ds.getSumsq()).setStdDev(ds.getStandardDeviation()).setVariance(ds.getVariance()).setSkewness(ds.getSkewness()).setGeometricMean(ds.getGeometricMean()).setKurtosis(ds.getKurtosis()).build();
  }

  private void count(String key, Map<String, FrequencyDto> frequencies) {
    FrequencyDto value = frequencies.get(key);
    if(value == null) {
      value = FrequencyDto.newBuilder().setName(key).setValue(1).build();
    } else {
      // Need to derive a new FrequencyDTO because they are immutable
      value = value.toBuilder().setValue(value.getValue() + 1).build();
    }
    frequencies.put(key, value);
  }
}
