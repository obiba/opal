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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.view.VariableViewHelper;
import org.obiba.opal.web.gwt.app.client.util.VariableDtos;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry;
import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public abstract class DerivedVariableGenerator {

  protected final VariableDto originalVariable;

  protected final List<ValueMapEntry> valueMapEntries;

  protected StringBuilder scriptBuilder;

  protected final Map<String, CategoryDto> newCategoriesMap = new LinkedHashMap<String, CategoryDto>();

  private boolean categoryValuesAppended;

  private boolean distinctValuesAppended;

  public DerivedVariableGenerator(VariableDto originalVariable, List<ValueMapEntry> valueMapEntries) {
    this.originalVariable = originalVariable;
    this.valueMapEntries = valueMapEntries;
  }

  public VariableDto generate(@Nullable VariableDto destination) {
    VariableDto derived = destination == null ? copyVariable(originalVariable) : destination;

    scriptBuilder = new StringBuilder();
    newCategoriesMap.clear();

    // don't overwrite value type if destination variable exists
    if(destination == null || derived.getValueType() == null) {
      derived.setValueType("text");
    }

    generateScript();

    // set script in derived variable
    VariableDtos.setScript(derived, scriptBuilder.toString());

    // new categories if destination does not already define them
    JsArray<CategoryDto> cats = destination == null ? null : destination.getCategoriesArray();
    if(cats == null || cats.length() == 0) {
      cats = JsArrays.create();
      for(CategoryDto cat : newCategoriesMap.values()) {
        cats.push(cat);
      }
      derived.setCategoriesArray(cats);
    }

    return derived;
  }

  protected abstract void generateScript();

  protected void appendCategoryValueMapEntries() {
    if(originalVariable.getCategoriesArray() == null) return;
    int nbCategories = originalVariable.getCategoriesArray().length();
    for(int i = 0; i < nbCategories; i++) {
      CategoryDto origCat = originalVariable.getCategoriesArray().get(i);
      ValueMapEntry entry = getValueMapEntry(origCat.getName());

      if(entry.isType(ValueMapEntryType.CATEGORY_NAME)) {
        // script
        scriptBuilder.append("\n    '").append(normalize(entry.getValue())).append("': ");
        appendNewValue(entry);
        if(i < nbCategories - 1) scriptBuilder.append(",");

        // new category
        addNewCategory(origCat, entry);
      }
    }
  }

  protected void appendDistinctValueMapEntries() {
    boolean first = true;
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getType() == ValueMapEntryType.DISTINCT_VALUE) {
        if(first && !categoryValuesAppended) {
          first = false;
        } else {
          scriptBuilder.append(",");
        }
        scriptBuilder.append("\n    '").append(normalize(entry.getValue())).append("': ");
        appendNewValue(entry);

        // new category
        addNewCategory(entry);
      }
    }
  }

  protected ValueMapEntry appendValueMapEntry(String value) {
    ValueMapEntry entry = getValueMapEntry(value);
    if(entry != null) {
      scriptBuilder.append("\n    '").append(normalize(value)).append("': ");
      appendNewValue(entry);
    }
    return entry;
  }

  private String normalize(String text) {
    return text == null ? text : text.replace("'", "\\'");
  }

  protected ValueMapEntry getValueMapEntry(String value) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.getValue().equals(value)) {
        return entry;
      }
    }
    return null;
  }

  protected ValueMapEntry getOtherValuesMapEntry() {
    return getMapEntry(ValueMapEntryType.OTHER_VALUES);
  }

  protected ValueMapEntry getEmptyValuesMapEntry() {
    return getMapEntry(ValueMapEntryType.EMPTY_VALUES);
  }

  protected ValueMapEntry getMapEntry(ValueMapEntryType type) {
    for(ValueMapEntry entry : valueMapEntries) {
      if(entry.isType(type)) return entry;
    }
    return null;
  }

  protected void addNewCategory(CategoryDto origCat, ValueMapEntry entry) {
    if(!entry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategoriesMap.get(entry.getNewValue());
      JsArray<AttributeDto> origAttrs = origCat.getAttributesArray();
      if(origAttrs == null || origAttrs.length() == 0) {
        origAttrs = newAttributes(newLabelAttribute(entry));
      }
      if(cat == null) {
        cat = newCategory(entry);
        cat.setAttributesArray(copyAttributes(origAttrs));
        newCategoriesMap.put(cat.getName(), cat);
      } else {
        // merge attributes
        mergeAttributes(origAttrs, cat.getAttributesArray());
      }
      categoryValuesAppended = true;
    }
  }

  protected void addNewCategory(ValueMapEntry entry) {
    if(!entry.getNewValue().isEmpty()) {
      CategoryDto cat = newCategory(entry);
      cat.setAttributesArray(newAttributes(newLabelAttribute(entry)));
      if(newCategoriesMap.containsKey(cat.getName())) {
        // merge attributes
        mergeAttributes(cat.getAttributesArray(), newCategoriesMap.get(cat.getName()).getAttributesArray());
      } else {
        newCategoriesMap.put(entry.getNewValue(), cat);
      }
      distinctValuesAppended = true;
    }
  }

  protected void appendNewValue(ValueMapEntry entry) {
    String value = entry.getNewValue();
    if(value != null && !value.isEmpty()) {
      scriptBuilder.append("'").append(normalize(value)).append("'");
    } else {
      scriptBuilder.append("null");
    }
  }

  protected void appendSpecialValuesEntry(ValueMapEntry entry) {
    if(entry == null) {
      scriptBuilder.append(",\n  null");
    } else {
      scriptBuilder.append(",\n  ");
      appendNewValue(entry);

      if(!entry.getNewValue().isEmpty()) {
        CategoryDto cat = newCategory(entry);
        cat.setAttributesArray(newAttributes(newLabelAttribute(entry)));
        if(newCategoriesMap.containsKey(cat.getName())) {
          // merge attributes
          mergeAttributes(cat.getAttributesArray(), newCategoriesMap.get(cat.getName()).getAttributesArray());
        } else {
          newCategoriesMap.put(entry.getNewValue(), cat);
        }
      }
    }
  }

  public static VariableDto copyVariable(@Nonnull VariableDto variable) {
    return copyVariable(variable, false);
  }

  public static VariableDto copyVariable(VariableDto variable, boolean withCategories) {
    VariableDto derived = VariableDto.create();
    derived.setName(variable.getName());
    derived.setValueType(variable.getValueType());
    derived.setEntityType(variable.getEntityType());
    derived.setIsRepeatable(variable.getIsRepeatable());
    derived.setIndex(variable.getIndex());
    derived.setOccurrenceGroup(variable.getOccurrenceGroup());
    derived.setUnit(variable.getUnit());

    // set attributes
    derived.setAttributesArray(copyAttributes(variable.getAttributesArray()));
    if(!Strings.isNullOrEmpty(variable.getLink())) {
      VariableDtos.setDerivedFrom(derived, variable);
    }

    // set categories
    if(withCategories) {
      derived.setCategoriesArray(copyCategories(derived.getCategoriesArray()));
    }

    return derived;
  }

  public static AttributeDto newLabelAttribute(ValueMapEntry entry) {
    if(entry.getLabel() == null || entry.getLabel().isEmpty()) return null;

    AttributeDto labelDto = AttributeDto.create();
    labelDto.setName("label");
    labelDto.setLocale(VariableViewHelper.getCurrentLanguage());
    labelDto.setValue(entry.getLabel());
    return labelDto;
  }

  public static JsArray<AttributeDto> newAttributes(AttributeDto... attrs) {
    JsArray<AttributeDto> nattrs = JsArrays.create();
    if(attrs != null) {
      for(AttributeDto attr : attrs) {
        nattrs.push(attr);
      }
    }
    return nattrs;
  }

  public static CategoryDto newCategory(ValueMapEntry entry) {
    CategoryDto cat = CategoryDto.create();
    cat.setName(entry.getNewValue());
    cat.setIsMissing(entry.isMissing());
    return cat;
  }

  protected static JsArray<CategoryDto> copyCategories(JsArray<CategoryDto> origCats) {
    JsArray<CategoryDto> cats = JsArrays.create();
    for(CategoryDto origCat : JsArrays.toIterable(JsArrays.toSafeArray(origCats))) {
      cats.push(copyCategory(origCat));
    }
    return cats;
  }

  protected static CategoryDto copyCategory(CategoryDto origCat) {
    CategoryDto cat = CategoryDto.create();
    cat.setName(origCat.getName());
    cat.setIsMissing(origCat.getIsMissing());
    cat.setAttributesArray(copyAttributes(origCat.getAttributesArray()));
    return cat;
  }

  public static JsArray<AttributeDto> copyAttributes(JsArray<AttributeDto> origAttrs) {
    JsArray<AttributeDto> attrs = JsArrays.create();
    for(AttributeDto origAttr : JsArrays.toIterable(JsArrays.toSafeArray(origAttrs))) {
      attrs.push(copyAttribute(origAttr));
    }
    return attrs;
  }

  public static void mergeAttributes(JsArray<AttributeDto> origAttrs, JsArray<AttributeDto> attrs) {
    if(origAttrs == null || origAttrs.length() == 0) return;

    for(AttributeDto origAttr : JsArrays.toIterable(origAttrs)) {
      boolean found = false;
      for(AttributeDto attr : JsArrays.toIterable(attrs)) {
        if(attr.getName().equals(origAttr.getName()) && origAttr.hasValue()) {
          String newValue = mergeAttributeValues(origAttr, attr);
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

  private static String mergeAttributeValues(AttributeDto origAttr, AttributeDto attr) {
    String newValue = origAttr.getValue();
    if(attr.hasValue()) {
      // do not append the same value several times
      boolean appended = false;
      for(String value : attr.getValue().split(" | ")) {
        if(value.compareTo(origAttr.getValue()) == 0) {
          appended = true;
        }
      }
      if(appended == false) {
        newValue = attr.getValue() + " | " + origAttr.getValue();
      }
    }
    return newValue;
  }

  private static AttributeDto copyAttribute(AttributeDto origAttr) {
    AttributeDto attr = AttributeDto.create();
    attr.setNamespace(origAttr.getNamespace());
    attr.setName(origAttr.getName());
    attr.setLocale(origAttr.getLocale());
    attr.setValue(origAttr.getValue());
    return attr;
  }

  public boolean isCategoryValuesAppended() {
    return categoryValuesAppended;
  }

  public boolean isDistinctValuesAppended() {
    return distinctValuesAppended;
  }
}
