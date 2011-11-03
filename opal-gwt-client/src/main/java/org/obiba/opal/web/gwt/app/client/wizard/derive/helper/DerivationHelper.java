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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public abstract class DerivationHelper {

  protected Translations translations = GWT.create(Translations.class);

  protected List<ValueMapEntry> valueMapEntries;

  protected VariableDto originalVariable;

  public DerivationHelper(VariableDto originalVariable) {
    super();
    this.originalVariable = originalVariable;
    initializeValueMapEntries();
  }

  protected abstract void initializeValueMapEntries();

  public abstract VariableDto getDerivedVariable();

  public List<ValueMapEntry> getValueMapEntries() {
    return valueMapEntries;
  }

  public VariableDto copyVariable(VariableDto variable) {
    return copyVariable(variable, false);
  }

  public VariableDto copyVariable(VariableDto variable, boolean withCategories) {
    VariableDto derived = VariableDto.create();
    derived.setName(variable.getName());
    derived.setValueType(variable.getValueType());
    derived.setEntityType(variable.getEntityType());
    derived.setIsRepeatable(variable.getIsRepeatable());

    // set attributes
    derived.setAttributesArray(copyAttributes(variable.getAttributesArray()));

    if(withCategories) {
      derived.setCategoriesArray(copyCategories(derived.getCategoriesArray()));
    }

    return derived;
  }

  protected void setScript(VariableDto derived, String script) {
    AttributeDto scriptAttr = getScriptAttribute(derived);
    scriptAttr.setValue(script);
  }

  public String getScript(VariableDto variable) {
    return getScriptAttribute(variable).getValue();
  }

  private AttributeDto getScriptAttribute(VariableDto derived) {
    AttributeDto scriptAttr = null;
    for(AttributeDto attr : JsArrays.toIterable(derived.getAttributesArray())) {
      if(attr.getName().equals("script")) {
        scriptAttr = attr;
        break;
      }
    }
    if(scriptAttr == null) {
      scriptAttr = AttributeDto.create();
      scriptAttr.setName("script");
      scriptAttr.setValue("null");
      derived.getAttributesArray().push(scriptAttr);
    }
    return scriptAttr;
  }

  protected ValueMapEntry getValueMapEntry(String value) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(value)) {
        return entry;
      }
      // GWT.log(entry.getValue() + "," + entry.getNewValue() + "," + entry.isMissing());
    }
    return null;
  }

  protected ValueMapEntry getOtherValuesMapEntry() {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.isType(ValueMapEntryType.OTHER_VALUES)) {
        return entry;
      }
    }
    return null;
  }

  protected ValueMapEntry getEmptyValuesMapEntry() {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.isType(ValueMapEntryType.EMPTY_VALUES)) {
        return entry;
      }
    }
    return null;
  }

  protected JsArray<CategoryDto> copyCategories(JsArray<CategoryDto> origCats) {
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto origCat : JsArrays.toIterable(JsArrays.toSafeArray(origCats))) {
      cats.push(copyCategory(origCat));
    }
    return cats;
  }

  protected CategoryDto copyCategory(CategoryDto origCat) {
    CategoryDto cat = CategoryDto.create();
    cat.setName(origCat.getName());
    cat.setIsMissing(origCat.getIsMissing());
    cat.setAttributesArray(copyAttributes(origCat.getAttributesArray()));
    return cat;
  }

  protected JsArray<AttributeDto> copyAttributes(JsArray<AttributeDto> origAttrs) {
    JsArray<AttributeDto> attrs = JsArrays.create();
    for(AttributeDto origAttr : JsArrays.toIterable(JsArrays.toSafeArray(origAttrs))) {
      attrs.push(copyAttribute(origAttr));
    }
    return attrs;
  }

  protected void mergeAttributes(JsArray<AttributeDto> origAttrs, JsArray<AttributeDto> attrs) {
    if(origAttrs == null || origAttrs.length() == 0) return;

    debug("origAttrs", origAttrs);
    debug("attrs", attrs);

    for(AttributeDto origAttr : JsArrays.toIterable(origAttrs)) {
      boolean found = false;
      for(AttributeDto attr : JsArrays.toIterable(attrs)) {
        if(attr.getName().equals(origAttr.getName()) && origAttr.hasValue()) {
          String newValue = attr.hasValue() ? attr.getValue() + " | " + origAttr.getValue() : origAttr.getValue();
          if((attr.hasLocale() && origAttr.hasLocale() && attr.getLocale().equals(origAttr.getLocale())) //
              || (!attr.hasLocale() && !origAttr.hasLocale())) {
            attr.setValue(newValue);
            found = true;
            break;
          }
        }
      }
      if(!found) {
        attrs.push(copyAttribute(origAttr));
      }
    }
  }

  private AttributeDto copyAttribute(AttributeDto origAttr) {
    AttributeDto attr = AttributeDto.create();
    attr.setName(origAttr.getName());
    attr.setLocale(origAttr.getLocale());
    attr.setValue(origAttr.getValue());
    return attr;
  }

  private void debug(String message, JsArray<AttributeDto> attrs) {
    for(int i = 0; i < attrs.length(); i++) {
      AttributeDto attr = attrs.get(i);
      GWT.log(message + "[" + (i + 1) + "]=" + attr.getName() + ", " + attr.getLocale() + ", " + attr.getValue());
    }
  }

}
