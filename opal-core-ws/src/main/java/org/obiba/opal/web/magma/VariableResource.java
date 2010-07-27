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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.web.model.Magma.DescriptiveStatsDto;
import org.obiba.opal.web.model.Magma.FrequencyDto;

import com.google.common.collect.Maps;

public class VariableResource {

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

  @GET
  @Path("/values.json")
  @Produces("application/json")
  public Response getValues() {
    Map<String, Object> response = new HashMap<String, Object>();

    List<Object> values = new LinkedList<Object>();
    for(ValueSet vs : valueTable.getValueSets()) {
      values.add(vvs.getValue(vs).getValue());
    }
    response.put(vvs.getVariable().getName(), values);

    return Response.ok(new JSONObject(response).toString()).build();
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
