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

import javax.ws.rs.GET;

import org.apache.commons.math.stat.Frequency;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.FrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

/**
 *
 */
public class CategoricalSummaryStatisticsResource extends AbstractSummaryStatisticsResource {

  /**
   * @param valueTable
   * @param variable
   * @param vectorSource
   */
  public CategoricalSummaryStatisticsResource(ValueTable valueTable, Variable variable, VectorSource vectorSource) {
    super(valueTable, variable, vectorSource);
  }

  @GET
  public SummaryStatisticsDto compute() {
    Frequency freq = new Frequency();
    for(Value value : getValues()) {
      if(value.isNull() == false) {
        if(value.isSequence()) {
          for(Value v : value.asSequence().getValue()) {
            freq.addValue(v.toString());
          }
        } else {
          freq.addValue(value.toString());
        }
      } else {
        freq.addValue("N/A");
      }
    }

    CategoricalSummaryDto.Builder builder = CategoricalSummaryDto.newBuilder();

    for(Category c : getVariable().getCategories()) {
      builder.addFrequencies(FrequencyDto.newBuilder().setValue(c.getName()).setFreq(freq.getCount(c.getName())).setPct(freq.getPct(c.getName())));
    }
    builder.addFrequencies(FrequencyDto.newBuilder().setValue("N/A").setFreq(freq.getCount("N/A")).setPct(freq.getPct("N/A")));
    return SummaryStatisticsDto.newBuilder().setResource(getVariable().getName()).setExtension(CategoricalSummaryDto.categorical, builder.build()).build();
  }

}
