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

public class NumericalVariableDerivationHelper extends DerivationHelper {

  private Map<ValueMapEntry, Range<?>> entryRangeMap;

  public NumericalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    this.entryRangeMap = new HashMap<ValueMapEntry, Range<?>>();
    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  public boolean addValueMapEntry(Number value, String newValue) {
    if(value != null && newValue != null && !newValue.trim().equals("")) {
      if(!hasValueMapEntryWithValue(value.toString())) {
        valueMapEntries.add(valueMapEntries.size() - 2, ValueMapEntry.fromDistinct(value.toString()).newValue(newValue).build());
        return true;
      }
    }
    return false;
  }

  public boolean addValueMapEntry(Number lower, Number upper, String newValue) {
    if(lower != null && lower.equals(upper)) {
      return addValueMapEntry(lower, newValue);
    } else if((lower != null || upper != null)) {
      Range<?> range = buildNumberRange(lower, upper);
      ValueMapEntry entry = ValueMapEntry.fromRange(lower, upper).label(range.toString()).newValue(newValue == null ? "" : newValue).build();
      if(!hasValueMapEntryWithValue(entry.getValue())) {
        entryRangeMap.put(entry, range);
        valueMapEntries.add(valueMapEntries.size() - 2, entry);
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
    List<Range<?>> ranges = new ArrayList<Range<?>>();
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

  private void appendBounds(StringBuilder scriptBuilder, List<Range<?>> ranges) {
    scriptBuilder.append("[");
    boolean first = true;
    for(Range<?> range : ranges) {
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
    scriptBuilder.append("}");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(")");
  }

  private boolean hasValueMapEntryWithValue(String value) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(value)) {
        return true;
      }
    }
    return false;
  }

  public Range<?> buildNumberRange(Number lower, Number upper) {
    if(lower instanceof Long) {
      return buildRange((Long) lower, (Long) upper);
    } else {
      return buildRange((Double) lower, (Double) upper);
    }
  }

  public Range<?> buildRange(Long lower, Long upper) {
    if(lower == null) {
      return Ranges.lessThan(upper);
    } else if(upper == null) {
      return Ranges.atLeast(lower);
    } else {
      return Ranges.closedOpen(lower, upper);
    }
  }

  public Range<?> buildRange(Double lower, Double upper) {
    if(lower == null) {
      return Ranges.lessThan(upper);
    } else if(upper == null) {
      return Ranges.atLeast(lower);
    } else {
      return Ranges.closedOpen(lower, upper);
    }
  }
}