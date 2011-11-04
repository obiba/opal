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
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;

public class TemporalVariableDerivationHelper extends DerivationHelper {

  public enum GroupMethod {
    DAY_OF_WEEK("dayOfWeek"), //
    DAY_OF_MONTH("dayOfMonth"), //
    DAY_OF_YEAR("dayOfYear"), //
    WEEK_OF_MONTH("weekOfMonth"), //
    WEEK_OF_YEAR("weekOfYear"), //
    MONTH("month"), //
    QUARTER("quarter"), //
    SEMESTER("semester"), //
    YEAR("year");

    private final String method;

    GroupMethod(String method) {
      this.method = method;
    }

    public static GroupMethod fromString(String text) {
      for(GroupMethod g : GroupMethod.values()) {
        if(g.method.equalsIgnoreCase(text)) {
          return g;
        }
      }
      return null;
    }

    @Override
    public String toString() {
      return method;
    }
  }

  private final GroupMethod groupMethod;

  public TemporalVariableDerivationHelper(VariableDto originalVariable, String groupMethod) {
    super(originalVariable);
    this.groupMethod = GroupMethod.fromString(groupMethod);
    initializeValueMapEntries();
  }

  @Override
  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    if(groupMethod == null) return;

    switch(groupMethod) {
    case DAY_OF_WEEK:
      addDayOfWeekEntries();
      break;
    case DAY_OF_MONTH:
      addDayOfMonthEntries();
      break;
    case DAY_OF_YEAR:
      addDayOfYearEntries();
      break;
    }

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
    valueMapEntries.add(ValueMapEntry.createOthers(translations.otherValuesLabel()).build());
  }

  private void addDayOfWeekEntries() {
    valueMapEntries.add(ValueMapEntry.fromDistinct("1").label("Sunday").newValue("1").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("2").label("Monday").newValue("2").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("3").label("Tuesday").newValue("3").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("4").label("Wednesday").newValue("4").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("5").label("Thursday").newValue("5").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("6").label("Friday").newValue("6").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct("7").label("Saturday").newValue("7").build());
  }

  private void addDayOfMonthEntries() {
    for(int i = 1; i < 32; i++) {
      String str = Integer.toString(i);
      valueMapEntries.add(ValueMapEntry.fromDistinct(str).newValue(str).build());
    }
  }

  private void addDayOfYearEntries() {
    for(int i = 1; i < 366; i++) {
      String str = Integer.toString(i);
      valueMapEntries.add(ValueMapEntry.fromDistinct(str).newValue(str).build());
    }
  }

  @Override
  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable, true);
    derived.setValueType("text");

    Map<String, CategoryDto> newCategoriesMap = new HashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "')");
    scriptBuilder.append(".").append(groupMethod).append("()");
    scriptBuilder.append(".map({");

    boolean first = true;
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType().equals(ValueMapEntryType.DISTINCT_VALUE)) {
        if(first) {
          first = false;
        } else {
          scriptBuilder.append(",");
        }
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendNewValue(scriptBuilder, entry);

        CategoryDto cat = newCategory(entry);
        cat.setAttributesArray(newAttributes(newLabelAttribute(entry)));
        newCategoriesMap.put(entry.getNewValue(), cat);
      }
    }

    scriptBuilder.append("\n").append("  }");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(");");

    setScript(derived, scriptBuilder.toString());

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newCategoriesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    return derived;
  }

  public void setMethod(String method) {
    // TODO Auto-generated method stub

  }

}