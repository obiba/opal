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

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 *
 */
public class CategoricalSummaryStatisticsResource extends AbstractSummaryStatisticsResource {

  final static String NULL_NAME = "N/A";

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
    Frequency freq = computeFrequencyDistribution();
    CategoricalSummaryDto.Builder builder = CategoricalSummaryDto.newBuilder();
    long max = 0;
    // Mode is the most frequent value
    String mode = NULL_NAME;
    // Iterate over all category names with an additional one for the null values. The loop will also determine the mode
    // of the distribution (most frequent value)
    for(String value : Iterables.concat(categoryNames(), ImmutableList.of(NULL_NAME))) {
      long count = freq.getCount(value);
      if(count > max) {
        max = count;
        mode = value;
      }
      builder.addFrequencies(FrequencyDto.newBuilder().setValue(value).setFreq(freq.getCount(value)).setPct(freq.getPct(value)));
    }
    builder.setMode(mode);
    return SummaryStatisticsDto.newBuilder().setResource(getVariable().getName()).setExtension(CategoricalSummaryDto.categorical, builder.build()).build();
  }

  private Frequency computeFrequencyDistribution() {
    Frequency freq = new Frequency();
    for(Value value : getValues()) {
      addValue(freq, value);
    }
    return freq;
  }

  private void addValue(Frequency freq, Value value) {
    if(value.isSequence()) {
      if(value.isNull()) {
        freq.addValue(NULL_NAME);
      } else {
        for(Value v : value.asSequence().getValue()) {
          addValue(freq, v);
        }
      }
    } else {
      if(value.isNull() == false) {
        freq.addValue(value.toString());
      } else {
        freq.addValue(NULL_NAME);
      }
    }
  }

  /**
   * Returns an iterable of category names
   */
  private Iterable<String> categoryNames() {
    return Iterables.transform(getVariable().getCategories(), new Function<Category, String>() {

      @Override
      public String apply(Category from) {
        return from.getName();
      }

    });
  }

}
