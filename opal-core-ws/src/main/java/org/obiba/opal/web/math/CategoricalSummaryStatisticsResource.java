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

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.math.stat.Frequency;
import org.jboss.resteasy.annotations.cache.Cache;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Math.CategoricalSummaryDto;
import org.obiba.opal.web.model.Math.FrequencyDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

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
  @POST
  @Cache(isPrivate = true, mustRevalidate = true, maxAge = 0)
  public Response compute(@QueryParam("distinct") boolean distinct) {
    Frequency freq = computeFrequencyDistribution();
    CategoricalSummaryDto.Builder builder = CategoricalSummaryDto.newBuilder();
    long max = 0;
    // Mode is the most frequent value
    String mode = NULL_NAME;
    Iterator<String> concat = distinct //
        ? freqNames(freq)  // category names, null values and distinct values
        : Iterators.concat(categoryNames(), ImmutableList.of(NULL_NAME).iterator()); // category names and null values

    // Iterate over all category names including or not distinct values. The loop will also determine the mode
    // of the distribution (most frequent value)
    while(concat.hasNext()) {
      String value = concat.next();
      long count = freq.getCount(value);
      if(count > max) {
        max = count;
        mode = value;
      }
      builder.addFrequencies(
          FrequencyDto.newBuilder().setValue(value).setFreq(freq.getCount(value)).setPct(freq.getPct(value)));
    }
    builder.setMode(mode).setN(freq.getSumFreq());
    return TimestampedResponses.ok(getValueTable(),
        SummaryStatisticsDto.newBuilder().setResource(getVariable().getName())
            .setExtension(CategoricalSummaryDto.categorical, builder.build()).build()).build();
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
        //noinspection ConstantConditions
        for(Value v : value.asSequence().getValue()) {
          addValue(freq, v);
        }
      }
    } else {
      freq.addValue(value.isNull() ? NULL_NAME : value.toString());
    }
  }

  /**
   * Returns an iterator of category names
   */
  private Iterator<String> categoryNames() {
    if(getVariable().getValueType().equals(BooleanType.get())) {
      return ImmutableList.<String>builder()//
          .add(BooleanType.get().trueValue().toString())//
          .add(BooleanType.get().falseValue().toString()).build().iterator();
    }

    return Iterables.transform(getVariable().getCategories(), new Function<Category, String>() {

      @Override
      public String apply(Category from) {
        return from.getName();
      }

    }).iterator();
  }

  /**
   * Returns an iterator of frequency names
   */
  private Iterator<String> freqNames(Frequency freq) {
    return Iterators.transform(freq.valuesIterator(), new Function<Comparable<?>, String>() {

      @Override
      public String apply(Comparable<?> input) {
        return input.toString();
      }
    });
  }

}
