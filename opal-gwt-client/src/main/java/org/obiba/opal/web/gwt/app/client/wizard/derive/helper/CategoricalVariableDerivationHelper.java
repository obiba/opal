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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.regexp.shared.RegExp;

/**
 *
 */
public class CategoricalVariableDerivationHelper extends DerivationHelper {

  public CategoricalVariableDerivationHelper(VariableDto originalVariable) {
    super(originalVariable);
  }

  protected void initializeValueMapEntries() {
    this.valueMapEntries = new ArrayList<ValueMapEntry>();
    int index = 1;
    for(CategoryDto cat : JsArrays.toIterable(originalVariable.getCategoriesArray())) {
      boolean missing = cat.hasIsMissing() ? cat.getIsMissing() : false;
      ValueMapEntry entry = new ValueMapEntry(cat.getName(), "", missing);

      if(RegExp.compile("^\\d+$").test(cat.getName())) {
        entry.setNewValue(cat.getName());
      } else if(RegExp.compile("^N$|^NO$|^NONE$|^NEVER$", "i").test(cat.getName())) {
        entry.setNewValue("0");
      } else if(RegExp.compile("^Y$|^YES$|^MALE$", "i").test(cat.getName())) {
        entry.setNewValue("1");
        index++;
      } else if(cat.getName().equalsIgnoreCase("FEMALE")) {
        entry.setNewValue("2");
        index++;
      } else {
        entry.setNewValue(Integer.toString(index++));
      }

      if(RegExp.compile("^DNK$|^DK-NA$|^PNA$|^REFUSED$|^NONE$|^NEVER$", "i").test(cat.getName())) {
        entry.setMissing(true);
      }

      valueMapEntries.add(entry);
    }
  }

  public VariableDto getDerivedVariable() {
    VariableDto derived = copyVariable(originalVariable);

    Map<String, CategoryDto> newValuesMap = new HashMap<String, CategoryDto>();

    StringBuilder scriptBuilder = new StringBuilder("$('" + originalVariable.getName() + "').map({");

    JsArray<CategoryDto> origCats = originalVariable.getCategoriesArray();

    for(int i = 0; i < origCats.length(); i++) {
      CategoryDto origCat = origCats.get(i);
      ValueMapEntry entry = getValueMapEntry(origCat.getName());

      // script
      scriptBuilder.append("\n  '").append(entry.getValue()).append("': ");
      if(!entry.getNewValue().isEmpty()) {
        scriptBuilder.append("'").append(entry.getNewValue()).append("'");
      } else {
        scriptBuilder.append("null");
      }
      if(i < origCats.length() - 1) {
        scriptBuilder.append(",");
      } else {
        scriptBuilder.append("\n");
      }

      // new category
      if(!entry.getNewValue().isEmpty()) {
        CategoryDto cat = newValuesMap.get(entry.getNewValue());
        if(cat == null) {
          cat = CategoryDto.create();
          cat.setName(entry.getNewValue());
          cat.setIsMissing(entry.isMissing());
          cat.setAttributesArray(copyAttributes(origCat.getAttributesArray()));
          newValuesMap.put(cat.getName(), cat);
        } else {
          // merge attributes
          mergeAttributes(origCat.getAttributesArray(), cat.getAttributesArray());
        }
      }
    }
    scriptBuilder.append("});");

    GWT.log(scriptBuilder.toString());

    // new categories
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto cat : newValuesMap.values()) {
      cats.push(cat);
    }
    derived.setCategoriesArray(cats);

    // set script
    setScript(derived, scriptBuilder.toString());

    return derived;
  }
}
