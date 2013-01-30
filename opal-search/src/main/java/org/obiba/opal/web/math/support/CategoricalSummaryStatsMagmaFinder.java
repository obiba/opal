/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math.support;

import java.util.Iterator;

import org.apache.commons.math.stat.Frequency;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.BooleanType;
import org.obiba.opal.web.finder.AbstractMagmaFinder;
import org.obiba.opal.web.finder.FinderResult;
import org.obiba.opal.web.model.Math;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 *
 */
public class CategoricalSummaryStatsMagmaFinder extends
    AbstractMagmaFinder<CategoricalSummaryStatsQuery, FinderResult<Math.CategoricalSummaryDto>> {

  private static final String NULL_NAME = "N/A";

  @Override
  public void executeQuery(CategoricalSummaryStatsQuery query,
      FinderResult<Math.CategoricalSummaryDto> result) {

    Frequency freq = computeFrequencyDistribution(query);
    Math.CategoricalSummaryDto.Builder builder = Math.CategoricalSummaryDto.newBuilder();
    long max = 0;
    // Mode is the most frequent value
    String mode = NULL_NAME;
    Iterator<String> concat;
    if(query.isDistinct()) {
      // category names, null values and distinct values
      concat = freqNames(freq);
    } else {
      // category names and null values
      concat = Iterators.concat(categoryNames(query.getVariable()), ImmutableList.of(NULL_NAME).iterator());
    }
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
          Math.FrequencyDto.newBuilder().setValue(value).setFreq(freq.getCount(value)).setPct(freq.getPct(value)));
    }
    builder.setMode(mode).setN(freq.getSumFreq());
    result.setValue(builder.build());
  }

  private Frequency computeFrequencyDistribution(CategoricalSummaryStatsQuery query) {
    Frequency freq = new Frequency();
    for(Value value : getValues(query)) {
      addValue(freq, value);
    }
    return freq;
  }

  private Iterable<Value> getValues(CategoricalSummaryStatsQuery query) {
    return query.getVectorSource().getValues(Sets.newTreeSet(query.getValueTable().getVariableEntities()));
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
   * Returns an iterator of category names
   */
  private Iterator<String> categoryNames(Variable variable) {
    if(variable.getValueType().equals(BooleanType.get())) {
      return ImmutableList.<String>builder()//
          .add(BooleanType.get().trueValue().toString())//
          .add(BooleanType.get().falseValue().toString()).build().iterator();
    }

    return Iterables.transform(variable.getCategories(), new Function<Category, String>() {

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

