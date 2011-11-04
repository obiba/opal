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
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class BooleanVariableDerivationHelper extends DerivationHelper {

  public BooleanVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
  }

  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();

    valueMapEntries.add(ValueMapEntry.fromDistinct(Boolean.TRUE.toString()).label(translations.trueLabel()).newValue("1").build());
    valueMapEntries.add(ValueMapEntry.fromDistinct(Boolean.FALSE.toString()).label(translations.falseLabel()).newValue("0").build());

    valueMapEntries.add(ValueMapEntry.createEmpties(translations.emptyValuesLabel()).build());
  }

  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);
    derived.setValueType("text");

    Map<String, CategoryDto> newCategoriesMap = new HashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "').map({");

    ValueMapEntry trueEntry = appendValueEntry(scriptBuilder, Boolean.TRUE.toString());
    scriptBuilder.append(",");
    if(!trueEntry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategory(trueEntry);
      cat.setAttributesArray(newAttributes(newLabelAttribute(trueEntry)));
      newCategoriesMap.put(cat.getName(), cat);
    }

    ValueMapEntry falseEntry = appendValueEntry(scriptBuilder, Boolean.FALSE.toString());
    scriptBuilder.append("\n").append("  }");
    if(!falseEntry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategory(falseEntry);
      cat.setAttributesArray(newAttributes(newLabelAttribute(falseEntry)));
      newCategoriesMap.put(cat.getName(), cat);
    }

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

}
