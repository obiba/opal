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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.gwt.core.client.JsArray;

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
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
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

    valueMapEntries.add(valueMapEntries.size() - 2, entry);
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
  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);
    derived.setValueType("text");

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "')");
    Map<String, CategoryDto> newCategoriesMap = new LinkedHashMap<String, CategoryDto>();

    appendGroupMethod(scriptBuilder);
    appendMapMethod(newCategoriesMap, scriptBuilder);

    setScript(derived, scriptBuilder.toString());

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newCategoriesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    return derived;
  }

  private void appendGroupMethod(StringBuilder scriptBuilder) {
    // group method
    // ImmutableSortedSet.Builder<Range<N>> ranges = ImmutableSortedSet.naturalOrder();
    List<Range<N>> ranges = new ArrayList<Range<N>>();
    List<ValueMapEntry> outliers = new ArrayList<ValueMapEntry>();
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType().equals(ValueMapEntryType.DISTINCT_VALUE)) {
        outliers.add(entry);
      } else if(entry.getType().equals(ValueMapEntryType.RANGE)) {
        ranges.add(entryRangeMap.get(entry));
      }
    }

    if(ranges.size() == 0) return;

    scriptBuilder.append(".group(");

    appendBounds(scriptBuilder, ranges);
    appendOutliers(scriptBuilder, outliers);

    scriptBuilder.append(")");
  }

  private void appendBounds(StringBuilder scriptBuilder, List<Range<N>> ranges) {
    scriptBuilder.append("[");
    boolean first = true;
    for(Range<N> range : ranges) {
      if(range.hasLowerBound()) {
        if(first) {
          first = false;
        } else {
          scriptBuilder.append(", ");
        }
        scriptBuilder.append(range.lowerEndpoint());
      }
    }
    scriptBuilder.append("]");
  }

  private void appendOutliers(StringBuilder scriptBuilder, List<ValueMapEntry> outliers) {
    if(outliers.size() == 0) return;
    scriptBuilder.append(", [");
    boolean first = true;
    for(ValueMapEntry entry : outliers) {
      if(first) {
        first = false;
      } else {
        scriptBuilder.append(", ");
      }
      scriptBuilder.append(entry.getValue());
    }
    scriptBuilder.append("]");
  }

  private void appendMapMethod(Map<String, CategoryDto> newCategoriesMap, StringBuilder scriptBuilder) {
    scriptBuilder.append(".map({");
    boolean first = true;
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType().equals(ValueMapEntryType.DISTINCT_VALUE) || entry.getType().equals(ValueMapEntryType.RANGE)) {
        if(first) {
          first = false;
        } else {
          scriptBuilder.append(", ");
        }
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendNewValue(scriptBuilder, entry);
        addNewCategory(newCategoriesMap, entry);
      }
    }
    scriptBuilder.append("\n  }");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(")");
  }

  public Range<N> buildRange(N lower, N upper) {
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

}