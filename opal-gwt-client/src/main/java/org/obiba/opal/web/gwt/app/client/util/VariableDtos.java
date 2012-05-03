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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;

public class VariableDtos {

  public static final String OPAL_NAMESPACE = "opal";

  public static final String SCRIPT_ATTRIBUTE = "script";
  public static final String DERIVED_FROM_ATTRIBUTE = "derivedFrom";

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
    AttributeDto scriptAttr = getAttribute(variable, null, SCRIPT_ATTRIBUTE);
    return scriptAttr != null ? scriptAttr.getValue() : "null";
  }

  /**
   * Set the script value in an attribute.
   * @param variable
   * @param script
   */
  public static void setScript(VariableDto variable, String script) {
    AttributeDto attr = getAttribute(variable, null, SCRIPT_ATTRIBUTE);
    if(attr == null) {
      createAttribute(variable, null, SCRIPT_ATTRIBUTE, script);
    } else {
      attr.setValue(script);
    }
  }

  public static
  @Nullable
  String getDerivedFrom(VariableDto variable) {
    AttributeDto attribute = getAttribute(variable, OPAL_NAMESPACE, DERIVED_FROM_ATTRIBUTE);
    return attribute == null ? null : attribute.getValue();
  }

  public static void setDerivedFrom(VariableDto variable, String derivedFrom) {
    AttributeDto attribute = getAttribute(variable, OPAL_NAMESPACE, DERIVED_FROM_ATTRIBUTE);
    if(attribute == null) {
      createAttribute(variable, OPAL_NAMESPACE, DERIVED_FROM_ATTRIBUTE, derivedFrom);
    } else {
      attribute.setValue(derivedFrom);
    }
  }

  public static void setDerivedFrom(VariableDto variable, VariableDto derivedFrom) {
    setDerivedFrom(variable, derivedFrom.getLink());
  }

  /**
   * Get an attribute from the provided variable.
   * @param variable
   * @param name
   * @return
   */
  public static
  @Nullable
  AttributeDto getAttribute(VariableDto variable, String name) {
    return getAttribute(variable, null, name);
  }

  /**
   * Get an attribute from the provided variable.
   * @param variable
   * @param namespace
   * @param name
   * @return
   */
  public static
  @Nullable
  AttributeDto getAttribute(VariableDto variable, @Nullable String namespace, String name) {
    // make sure attributes array is defined
    variable.setAttributesArray(JsArrays.toSafeArray(variable.getAttributesArray()));
    if(namespace == null) namespace = "";
    for(AttributeDto attr : JsArrays.toIterable(variable.getAttributesArray())) {
      if(attr.getNamespace().equals(namespace) && attr.getName().equals(name)) {
        return attr;
      }
    }
    return null;
  }

  public static AttributeDto createAttribute(VariableDto variable, @Nullable String namespace, String name,
      String value) {
    AttributeDto attribute = AttributeDto.create();
    attribute.setNamespace(namespace);
    attribute.setName(name);
    attribute.setValue(value);

    JsArray<AttributeDto> attributes = JsArrays.toSafeArray(variable.getAttributesArray());
    attributes.push(attribute);
    variable.setAttributesArray(attributes);

    return attribute;
  }

  /**
   * Set the attribute value of a variable (create attribute if it does not exist).
   * @param variable
   * @param name
   * @param value
   */
  public static void setAttribute(VariableDto variable, String name, String value) {
    setAttribute(variable, null, name, value);
  }

  /**
   * Set the attribute value of a variable (create attribute if it does not exist).
   * @param variable
   * @param namespace
   * @param name
   * @param value
   */
  public static void setAttribute(VariableDto variable, String namespace, String name, String value) {
    AttributeDto attr = getAttribute(variable, namespace, name);
    if(attr == null) {
      createAttribute(variable, namespace, name, value);
    } else {
      attr.setValue(value);
    }
  }

  public enum ValueType {

    TEXT, DECIMAL, INTEGER, BINARY, BOOLEAN, DATETIME, DATE, LOCALE;

    private String label;

    ValueType() {
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
