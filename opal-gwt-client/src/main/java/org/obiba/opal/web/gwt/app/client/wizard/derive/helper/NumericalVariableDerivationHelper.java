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
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

public class NumericalVariableDerivationHelper<N extends Number & Comparable<N>> extends DerivationHelper {

  private Map<ValueMapEntry, Range<N>> entryRangeMap;

  public NumericalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    this.entryRangeMap = new HashMap<ValueMapEntry, Range<N>>();

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

  public void addValueMapEntry(N value, String newValue) {
    addValueMapEntry(value, value, newValue);
  }

  public void addValueMapEntry(N lower, N upper, String newValue) {
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

    valueMapEntries.add(0, entry);
    Collections.sort(valueMapEntries, new NumericValueMapEntryComparator());
  }

  public boolean isRangeOverlap(N lower, N upper) {
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
    } else if(upper == null) {
      return Ranges.atLeast(lower);
    } else if(lower.equals(upper)) {
      return Ranges.closed(lower, upper);
    } else {
      return Ranges.closedOpen(lower, upper);
    }
  }

  private final class NumericValueMapEntryComparator implements Comparator<ValueMapEntry> {
    @Override
    public int compare(ValueMapEntry o1, ValueMapEntry o2) {
      switch(o1.getType()) {
      case RANGE:
        return o2.getType().equals(ValueMapEntryType.RANGE) ? compareRanges(o1, o2) : -1;
      case CATEGORY_NAME:
      case DISTINCT_VALUE:
        if(o2.getType().equals(ValueMapEntryType.CATEGORY_NAME) || o2.getType().equals(ValueMapEntryType.DISTINCT_VALUE)) return compareDistincts(o1, o2);
        return o2.getType().equals(ValueMapEntryType.EMPTY_VALUES) ? -1 : 1;
      case EMPTY_VALUES:
        return o2.getType().equals(ValueMapEntryType.OTHER_VALUES) ? -1 : 1;
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