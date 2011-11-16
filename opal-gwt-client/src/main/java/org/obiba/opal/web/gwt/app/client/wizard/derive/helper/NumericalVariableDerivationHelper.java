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
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

public class NumericalVariableDerivationHelper extends DerivationHelper {

  private Map<ValueMapEntry, Range<Long>> entryRangeMap;

  public NumericalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    this.entryRangeMap = new HashMap<ValueMapEntry, Range<Long>>();
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  public boolean addValueMapEntry(Long value, String newValue) {
    if(value != null && newValue != null && !newValue.trim().equals("")) {
      if(!hasValueMapEntryWithValue(value.toString())) {
        valueMapEntries.add(ValueMapEntry.fromDistinct(value.toString()).newValue(newValue).build());
        return true;
      }
    }
    return false;
  }

  public boolean addValueMapEntry(Long lower, Long upper, String newValue) {
    if(lower != null && lower.equals(upper)) {
      return addValueMapEntry(lower, newValue);
    } else if((lower != null || upper != null) && newValue != null && !newValue.trim().equals("")) {
      Range<Long> range = buildRange(lower, upper);
      ValueMapEntry entry = ValueMapEntry.fromRange(lower, upper).label(range.toString()).newValue(newValue).build();
      if(!hasValueMapEntryWithValue(entry.getValue())) {
        entryRangeMap.put(entry, range);
        valueMapEntries.add(entry);
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
    List<Range<Long>> ranges = new ArrayList<Range<Long>>();
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

  private void appendBounds(StringBuilder scriptBuilder, List<Range<Long>> ranges) {
    scriptBuilder.append("[");
    boolean first = true;
    for(Range<Long> range : ranges) {
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
        scriptBuilder.append("'").append(entry.getValue()).append("': ");
        appendNewValue(scriptBuilder, entry);
        addNewCategory(newCategoriesMap, entry);
      }
    }
    scriptBuilder.append("})");
  }

  private boolean hasValueMapEntryWithValue(String value) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  public Range<Long> buildRange(String value) {
    if(value.startsWith("-")) {
      Long upper = Long.parseLong(value.substring(1));
      GWT.log("(*, " + upper + "]");
      return Ranges.lessThan(upper);
    } else if(value.endsWith("+")) {
      Long lower = Long.parseLong(value.substring(0, value.length() - 1));
      GWT.log("[" + lower + ", *)");
      return Ranges.atLeast(lower);
    } else {
      String[] vals = value.split("-");
      Long lower = Long.parseLong(vals[0]);
      Long upper = Long.parseLong(vals[1]);
      GWT.log("[" + lower + ", " + upper + "]");
      return Ranges.closedOpen(lower, upper);
    }
  }

  public Range<Long> buildRange(Long lower, Long upper) {
    if(lower == null) {
      return Ranges.lessThan(upper);
    } else if(upper == null) {
      return Ranges.atLeast(lower);
    } else {
      return Ranges.closedOpen(lower, upper);
    }
  }
}