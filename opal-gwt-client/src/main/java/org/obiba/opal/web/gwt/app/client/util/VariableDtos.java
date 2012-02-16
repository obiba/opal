/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.util;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;

public class VariableDtos {

  /**
   * True if variable has at least one category defined.
   * @param variable
   * @return
   */
  public static boolean hasCategories(VariableDto variable) {
    return variable.getCategoriesArray() != null && variable.getCategoriesArray().length() > 0;
  }

  /**
   * Return false if variableDto contains at least one non-missing category, otherwise true
   * @param variable
   * @return
   */
  public static boolean allCategoriesMissing(VariableDto variable) {
    JsArray<CategoryDto> categoriesArray = variable.getCategoriesArray();
    if(categoriesArray == null) return true;
    for(int i = 0; i < categoriesArray.length(); i++) {
      if(!categoriesArray.get(i).getIsMissing()) return false;
    }
    return true;
  }

  /**
   * Get the script value if it exists (otherwise returns 'null' script).
   * @param variable
   * @return
   */
  public static String getScript(VariableDto variable) {
    AttributeDto scriptAttr = null;
    for(AttributeDto attr : JsArrays.toIterable(JsArrays.toSafeArray(variable.getAttributesArray()))) {
      if(attr.getName().equals("script")) {
        scriptAttr = attr;
        break;
      }
    }
    return scriptAttr != null ? scriptAttr.getValue() : "null";
  }

  /**
   * Set the script value in an attribute.
   * @param variable
   * @param script
   */
  public static void setScript(VariableDto variable, String script) {
    AttributeDto scriptAttr = getScriptAttribute(variable);
    scriptAttr.setValue(script);
  }

  /**
   * Get or create script attribute from variable.
   * @param variable
   * @return
   */
  public static AttributeDto getScriptAttribute(VariableDto variable) {
    return getAttribute(variable, "script");
  }

  /**
   * Get or create an attribute from the provided variable.
   * @param variable
   * @param name
   * @return
   */
  public static AttributeDto getAttribute(VariableDto variable, String name) {
    AttributeDto scriptAttr = null;
    // make sure attributes array is defined
    variable.setAttributesArray(JsArrays.toSafeArray(variable.getAttributesArray()));

    for(AttributeDto attr : JsArrays.toIterable(variable.getAttributesArray())) {
      if(attr.getName().equals(name)) {
        scriptAttr = attr;
        break;
      }
    }

    if(scriptAttr == null) {
      scriptAttr = AttributeDto.create();
      scriptAttr.setName(name);
      scriptAttr.setValue("null");
      variable.getAttributesArray().push(scriptAttr);
    }
    return scriptAttr;
  }

  /**
   * Set the attribute value of a variable (create attribute if it does not exist).
   * @param variable
   * @param name
   * @param value
   */
  public static void setAttribute(VariableDto variable, String name, String value) {
    AttributeDto attr = getAttribute(variable, name);
    attr.setValue(value);
  }

  public enum ValueType {
    TEXT, DECIMAL, INTEGER, BINARY, BOOLEAN, DATETIME, DATE, LOCALE;

    String label;

    private ValueType() {
      label = name().toLowerCase();
    }

    public String getLabel() {
      return label;
    }

    public boolean is(String value) {
      return label.equals(value.toLowerCase());
    }
  }
}
