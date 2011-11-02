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
      ValueMapEntry map = new ValueMapEntry(cat.getName(), Integer.toString(index++), missing);
      valueMapEntries.add(map);
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
