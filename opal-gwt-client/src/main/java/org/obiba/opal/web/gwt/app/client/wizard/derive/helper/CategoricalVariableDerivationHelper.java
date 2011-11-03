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
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;

/**
 *
 */
public class CategoricalVariableDerivationHelper extends DerivationHelper {

  private static final String MISSING_REGEXP = "^DNK$|^DK-NA$|^PNA$|^REFUSED$";

  private static final String NONE_REGEXP = "^NONE$|^NEVER$";

  private static final String YES_REGEXP = "^Y$|^YES$";

  private static final String NO_REGEXP = "^N$|^NO$";

  private static final String MALE_REGEXP = "^MALE$";

  private static final String FEMALE_REGEXP = "^FEMALE$";

  public CategoricalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
  }

  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    List<ValueMapEntry> missingValueMapEntries = new ArrayList<ValueMapEntry>();
    int index = 1;

    // recode non-missing values and identify missing values
    for(CategoryDto cat : JsArrays.toIterable(originalVariable.getCategoriesArray())) {
      ValueMapEntry entry = new ValueMapEntry(ValueMapEntryType.CATEGORY_NAME, cat.getName());

      if(estimateIsMissing(cat)) {
        missingValueMapEntries.add(entry);
        entry.setMissing(true);
      } else {
        valueMapEntries.add(entry);

        if(RegExp.compile("^\\d+$").test(cat.getName())) {
          entry.setNewValue(cat.getName());
        } else if(RegExp.compile(NO_REGEXP + "|" + NONE_REGEXP, "i").test(cat.getName())) {
          entry.setNewValue("0");
        } else if(RegExp.compile(YES_REGEXP + "|" + MALE_REGEXP, "i").test(cat.getName())) {
          entry.setNewValue("1");
          index++;
        } else if(RegExp.compile(FEMALE_REGEXP, "i").test(cat.getName())) {
          entry.setNewValue("2");
          index++;
        } else {
          entry.setNewValue(Integer.toString(index++));
        }
      }
    }

    // recode missing values
    if(missingValueMapEntries.size() > 0) {
      int missIndex = 10 - missingValueMapEntries.size();
      int factor = 1;
      while(missIndex * factor < index + 1) {
        factor = factor * 10 + 1;
      }
      for(ValueMapEntry entry : missingValueMapEntries) {
        entry.setNewValue(Integer.toString(missIndex * factor));
        missIndex++;
      }
      valueMapEntries.addAll(missingValueMapEntries);
    }

    valueMapEntries.add(new ValueMapEntry(ValueMapEntryType.EMPTY_VALUES, translations.emptyValuesLabel(), "", true));
    valueMapEntries.add(new ValueMapEntry(ValueMapEntryType.OTHER_VALUES, translations.otherValuesLabel()));
  }

  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);

    Map<String, CategoryDto> newCategoriesMap = new HashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "').map({");

    JsArray<CategoryDto> origCats = originalVariable.getCategoriesArray();

    for(int i = 0; i < origCats.length(); i++) {
      CategoryDto origCat = origCats.get(i);
      ValueMapEntry entry = getValueMapEntry(origCat.getName());

      if(entry.isType(ValueMapEntryType.CATEGORY_NAME, ValueMapEntryType.DISTINCT_VALUE)) {
        // script
        scriptBuilder.append("\n    '").append(entry.getValue()).append("': ");
        appendValue(scriptBuilder, entry.getNewValue());

        if(i < origCats.length() - 1) {
          scriptBuilder.append(",");
        } else {
          scriptBuilder.append("\n");
        }

        // new category
        if(!entry.getNewValue().isEmpty()) {
          CategoryDto cat = newCategoriesMap.get(entry.getNewValue());
          if(cat == null) {
            cat = CategoryDto.create();
            cat.setName(entry.getNewValue());
            cat.setIsMissing(entry.isMissing());
            cat.setAttributesArray(copyAttributes(origCat.getAttributesArray()));
            newCategoriesMap.put(cat.getName(), cat);
          } else {
            // merge attributes
            mergeAttributes(origCat.getAttributesArray(), cat.getAttributesArray());
          }
        }
      }
    }
    scriptBuilder.append("  }");
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getOtherValuesMapEntry());
    appendSpecialValuesEntry(scriptBuilder, newCategoriesMap, getEmptyValuesMapEntry());
    scriptBuilder.append(");");

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newCategoriesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    // set script in derived variable
    setScript(derived, scriptBuilder.toString());

    return derived;
  }

  private void appendSpecialValuesEntry(StringBuilder scriptBuilder, Map<String, CategoryDto> newCategoriesMap, ValueMapEntry entry) {
    if(entry == null) return;

    scriptBuilder.append(",\n  ");
    appendValue(scriptBuilder, entry.getNewValue());

    if(!entry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategoriesMap.get(entry.getNewValue());
      if(cat == null) {
        cat = CategoryDto.create();
        cat.setName(entry.getNewValue());
        cat.setIsMissing(entry.isMissing());

        AttributeDto labelDto = AttributeDto.create();
        labelDto.setName("label");
        // TODO set the translation locale
        labelDto.setLocale("en");
        labelDto.setValue(entry.getValue());
        JsArray<AttributeDto> attrs = JsArrays.create();
        attrs.push(labelDto);
        cat.setAttributesArray(attrs);

        newCategoriesMap.put(cat.getName(), cat);
      }
    }
  }

  private void appendValue(StringBuilder scriptBuilder, String value) {
    if(value == null) return;

    if(!value.isEmpty()) {
      // if(RegExp.compile("^\\d+$").test(entry.getNewValue())) {
      // scriptBuilder.append(entry.getNewValue());
      // } else {
      scriptBuilder.append("'").append(value).append("'");
      // }
    } else {
      scriptBuilder.append("null");
    }
  }

  private boolean estimateIsMissing(CategoryDto cat) {
    boolean missing = false;

    if(estimateIsMissing(cat.getName())) {
      missing = true;
    } else if(cat.hasIsMissing()) {
      missing = cat.getIsMissing();
    }

    return missing;
  }

  private boolean estimateIsMissing(String value) {
    if(value == null || value.isEmpty()) return true;
    return RegExp.compile(MISSING_REGEXP, "i").test(value);
  }
}
