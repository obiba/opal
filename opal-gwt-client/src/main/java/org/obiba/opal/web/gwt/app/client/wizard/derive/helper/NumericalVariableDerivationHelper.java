/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.math.CategoricalSummaryDto;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.gwt.core.client.JsArray;

public class NumericalVariableDerivationHelper<N extends Number & Comparable<N>> extends DerivationHelper {

  private Map<ValueMapEntry, Range<N>> entryRangeMap;

  public NumericalVariableDerivationHelper(VariableDto originalVariable, VariableDto destination) {
    super(originalVariable, destination);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    valueMapEntries = new ArrayList<ValueMapEntry>();
    entryRangeMap = new HashMap<ValueMapEntry, Range<N>>();

    addMissingCategoriesMapping();

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  private void addMissingCategoriesMapping() {
    // distinct values
    for(CategoryDto category : JsArrays.toIterable(JsArrays.toSafeArray(originalVariable.getCategoriesArray()))) {
      if(category.getIsMissing()) {
        valueMapEntries.add(ValueMapEntry.fromCategory(category).missing().build());
      }
    }
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  public double addDistinctValues(CategoricalSummaryDto categoricalSummaryDto) {
    List<FrequencyDto> frequenciesList = getFrequenciesList(categoricalSummaryDto);
    double maxFreq = 0;
    for(int i = 0; i < frequenciesList.size(); i++) {
      FrequencyDto frequencyDto = frequenciesList.get(i);
      double freq = frequencyDto.getFreq();
      String value = frequencyDto.getValue();
      if(!value.equals(NA)) {
        ValueMapEntry entry;
        ValueMapEntry existingEntry = getValueMapEntryWithValue(value);
        if(existingEntry == null) {
          entry = ValueMapEntry.fromDistinct(value).newValue(Integer.toString(i + 1)).build();
          addValueMapEntry(entry);
        } else {
          entry = existingEntry;
        }
        entry.setCount(freq);
      } else {
        setEmptiesFrequency(freq);
      }
      if(freq > maxFreq) {
        maxFreq = freq;
      }
    }

    return maxFreq;
  }

  private List<FrequencyDto> getFrequenciesList(CategoricalSummaryDto categoricalSummaryDto) {
    JsArray<FrequencyDto> frequenciesArray = categoricalSummaryDto.getFrequenciesArray();
    // sort frequency dtos by value
    List<FrequencyDto> frequenciesList = JsArrays.toList(frequenciesArray);

    Collections.sort(frequenciesList, new Comparator<FrequencyDto>() {

      @Override
      public int compare(FrequencyDto o1, FrequencyDto o2) {
        if(o1.getValue().equals(NA)) return -1;
        try {
          return Double.valueOf(o1.getValue()).compareTo(Double.valueOf(o2.getValue()));
        } catch(Exception e) {
          return -1;
        }
      }
    });
    return frequenciesList;
  }

  private void setEmptiesFrequency(double emptiesFrequency) {
    ValueMapEntry entry = getEmptiesValueMapEntry();
    if(entry != null) {
      entry.setCount(emptiesFrequency);
    }
  }

  public void addValueMapEntry(N value, String newValue) {
    addValueMapEntry(value, value, newValue);
  }

  public void addValueMapEntry(@Nullable N lower, @Nullable N upper, String newValue) {
    if(lower == null && upper == null) return;

    ValueMapEntry entry = null;
    String nv = newValue == null ? "" : newValue;

    if(lower != null && lower.equals(upper)) {
      entry = ValueMapEntry.fromDistinct(lower).newValue(nv).build();
    } else {
      Range<N> range = buildRange(lower, upper);
      entry = ValueMapEntry.fromRange(range).newValue(nv).build();
      entryRangeMap.put(entry, range);
    }

    addValueMapEntry(entry);
  }

  private void addValueMapEntry(ValueMapEntry entry) {
    valueMapEntries.add(0, entry);
    Collections.sort(valueMapEntries, new NumericValueMapEntryComparator());
  }

  public boolean isRangeOverlap(@Nullable N lower, @Nullable N upper) {
    return isRangeOverlap(buildRange(lower, upper));
  }

  public boolean isRangeOverlap(Range<N> range) {
    for(ValueMapEntry e : valueMapEntries) {
      Range<N> r = entryRangeMap.get(e);
      if(r != null && r.isConnected(range) && !r.intersection(range).isEmpty()) {
        // range overlap
        return true;
      }
    }
    return false;
  }

  @Override
  protected DerivedVariableGenerator getDerivedVariableGenerator() {
    return new DerivedNumericalVariableGenerator<N>(originalVariable, valueMapEntries, entryRangeMap);
  }

  public static <N extends Number & Comparable<N>> Range<N> buildRange(N lower, N upper) {
    if(lower == null) {
      return Ranges.lessThan(upper);
    }
    if(upper == null) {
      return Ranges.atLeast(lower);
    }
    if(lower.equals(upper)) {
      return Ranges.closed(lower, upper);
    }
    return Ranges.closedOpen(lower, upper);
  }

  private final class NumericValueMapEntryComparator implements Comparator<ValueMapEntry> {
    @Override
    public int compare(ValueMapEntry o1, ValueMapEntry o2) {
      switch(o1.getType()) {
        case RANGE:
          return o2.getType() == ValueMapEntryType.RANGE ? compareRanges(o1, o2) : -1;
        case CATEGORY_NAME:
        case DISTINCT_VALUE:
          if(o2.getType() == ValueMapEntryType.CATEGORY_NAME || o2.getType() == ValueMapEntryType.DISTINCT_VALUE) {
            return compareDistincts(o1, o2);
          }
          return o2.getType() == ValueMapEntryType.EMPTY_VALUES ? -1 : 1;
        case EMPTY_VALUES:
          return o2.getType() == ValueMapEntryType.OTHER_VALUES ? -1 : 1;
        case OTHER_VALUES:
          return 1;
      }
      return 0;
    }

    private int compareRanges(ValueMapEntry o1, ValueMapEntry o2) {
      Range<N> r1 = entryRangeMap.get(o1);
      Range<N> r2 = entryRangeMap.get(o2);
      if(!r1.hasLowerBound()) return -1;
      if(!r2.hasLowerBound()) return 1;

      return r1.lowerEndpoint().compareTo(r2.lowerEndpoint());
    }

    private int compareDistincts(ValueMapEntry o1, ValueMapEntry o2) {
      return new Double(o1.getValue()).compareTo(new Double(o2.getValue()));
    }
  }

}