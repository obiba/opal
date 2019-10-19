/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.Set;
import javax.validation.constraints.NotNull;

import com.google.gwt.user.client.ui.*;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.HelpBlock;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.TakesValue;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.markdown.client.Markdown;

public class SchemaUiContainer extends ControlGroup {

  private final EventBus eventBus;

  private JSONObject schema;
  private final String key;

  private final boolean required;

  private final String type;
  private final String format;

  public SchemaUiContainer(JSONObject schema, String key, boolean required, EventBus eventBus) {
    this.schema = schema;
    this.key = key;
    this.required = required;
    this.eventBus = eventBus;
    type = JsonSchemaGWT.getType(schema);

    JSONValue formatValue = schema.get("format");
    format = formatValue != null && formatValue.isString() != null ? formatValue.isString().stringValue() : "";

    setUp();
  }

  public Object getValue() {
    Iterator<Widget> iterator = getChildren().iterator();
    Object value = null;
    boolean found = false;

    while(!found || iterator.hasNext()) {
      Widget widget = iterator.next();

      if(widget instanceof TakesValue || widget instanceof OpalListBox) {
        found = true;
        value = widget instanceof TakesValue ? ((TakesValue) widget).getValue() : ((OpalListBox) widget).getSelectedValue();
      }
    }

    return value;
  }

  public JSONValue getJSONValue() {
    Object value = getValue();

    if(value == null) return null;
    if(type.equals("array")) {

      if (schema.get("items").isArray() != null) {
        return (JSONArray) getValue();
      }

      HashSet set = (HashSet) value;
      JSONArray jsonArray = new JSONArray();

      int i = 0;
      for(Object item : set) {
        jsonArray.set(i++, new JSONString(item.toString()));
      }

      return jsonArray;
    } else {
      return new JSONString(value.toString());
    }
  }

  public void setJSONValue(JSONValue value) {
    Iterator<Widget> iterator = getChildren().iterator();
    boolean found = false;

    while(value != null && (!found && iterator.hasNext())) {
      Widget widget = iterator.next();

      if(widget instanceof TakesValue) {
        TakesValue editableWidget = (TakesValue) widget;

        if ("string".equals(type)) {
          editableWidget.setValue(ensureStringValue(value));
        } else if ("integer".equals(type)) {
          Integer aInt = ensureIntegerValue(value);
          editableWidget.setValue(aInt);
        } else if ("number".equals(type)) {
          Double aDouble = ensureNumberValue(value);
          editableWidget.setValue(aDouble);
        } else if ("array".equals(type)) {
          if (schema.get("items").isArray() != null) {
            editableWidget.setValue(value.isArray());
          } else {
            Set<String> set = new HashSet<>();
            JSONArray array = value.isArray();
            if (array != null) {
              int size = array.size();
              for (int i = 0; i < size; i++) {
                set.add(ensureStringValue(array.get(i)));
              }
            }

            editableWidget.setValue(set);
          }

        }

        found = true;
      } else if (widget instanceof OpalListBox) {
        int valueIndex = ((OpalListBox) widget).getValueIndex(ensureStringValue(value));
        if (valueIndex > -1) {
          ((OpalListBox) widget).setSelectedIndex(valueIndex);
        }

        found = true;
      }
    }
  }

  private String ensureStringValue(JSONValue value) {
    return value != null && value.isString() != null ? value.isString().stringValue() : "";
  }

  private Double ensureNumberValue(JSONValue value) {
    if (value != null && value.isNumber() != null)
      return value.isNumber().doubleValue();

    String nbStr = ensureStringValue(value);
    try {
      return Double.parseDouble(nbStr);
    } catch (Exception e) {
      return null;
    }
  }

  private Integer ensureIntegerValue(JSONValue value) {
    Double aDouble = ensureNumberValue(value);
    return aDouble == null ? null : aDouble.intValue();
  }

  public boolean isValid() {
    return validate().length() == 0;
  }

  public String validate() {
    Object value = getValue();
    switch(type) {
      case "string": {
        return validateString(value);
      }
      case "integer":
      case "number": {
        return validateNumber(value);
      }
      case "array": {
        return validateArray(value);
      }
    }

    return "";
  }

  private boolean validateFileFormat(Object value, JSONArray fileFormatsArray) {
    String stringValue = value == null ? "" : (String) value;
    if(stringValue.trim().length() == 0) return true;

    boolean formatIsvalid = false;
    int i = 0;
    while(!formatIsvalid && i < fileFormatsArray.size()) {
      String fileFormat = fileFormatsArray.get(i).isString().stringValue();
      formatIsvalid = stringValue.endsWith(fileFormat);
      i++;
    }

    return formatIsvalid;
  }

  private String validateString(Object value) {
    if(required && (value == null || (value instanceof String && ((String) value).trim().length() == 0))) {
      return "required";
    }

    JSONValue fileFormats = schema.get("fileFormats");
    if(format.equals("file") && (fileFormats != null && fileFormats.isArray() != null)) {
      if(!validateFileFormat(value, fileFormats.isArray())) return "fileFormats";
    }

    return JsonSchemaGWT.valueForStringSchemaIsValid(value instanceof String ? (String) value : null, schema);
  }

  private String validateNumber(Object value) {
    if(required && value == null) {
      return "required";
    }

    return JsonSchemaGWT.valueForNumericSchemaIsValid((Number) value, schema);
  }

  private String validateArray(Object value) {
    int size = 0;
    if (schema.get("items").isArray() == null) {
      size = (value instanceof HashSet ? (HashSet) value : new HashSet()).size();
    } else {
      size = (value instanceof JSONArray ? (JSONArray) value : new JSONArray()).size();
    }

    if(required && (value == null || size == 0)) {
      return "required";
    }

    return JsonSchemaGWT.valueForArraySchemaIsValid(value instanceof HashSet ? (HashSet) value
        : value instanceof JSONArray ? JsonSchemaGWT.jsonArrayOfObjectsToList((JSONArray) value)
            : new HashSet(), schema);
  }

  public JSONObject getSchema() {
    return schema;
  }

  public String getKey() {
    return key;
  }

  public boolean isRequired() {
    return required;
  }

  public String getType() {
    return type;
  }

  private void setUp() {
    Widget widget = buildInputWidget();

    if(widget != null) {
      JSONValue title = schema.get("title");
      if(title != null && title.isString() != null) {
        String titleStringValue = title.isString().stringValue();
        add(new ControlLabel(SimpleHtmlSanitizer.sanitizeHtml(titleStringValue).asString()));
        setTitle(titleStringValue);
      }

      if(required) widget.getElement().setAttribute("required", "required");

      JSONValue readOnly = schema.get("readOnly");
      if (readOnly != null && readOnly.isBoolean() != null && readOnly.isBoolean().booleanValue()) {
        ((HasEnabled) widget).setEnabled(false);
      }

      add(widget);

      JSONValue description = schema.get("description");
      if(description != null && description.isString() != null) {
        String descriptionStringValue = description.isString().stringValue();
        HelpBlock helpBlock = new HelpBlock();
        helpBlock.setHTML(Markdown.parseNoStyle(descriptionStringValue));
        add(helpBlock);
      }
    }
  }

  private Widget buildInputWidget() {
    JSONValue aDefault = schema.get("default");

    switch(type) {
      case "number": {
        return createWidgetForNumber(aDefault);
      }
      case "integer": {
        return createWidgetForInteger(aDefault);
      }
      case "string": {
        return createWidgetForString(aDefault);
      }
      case "boolean": {
        return createWidgetForBoolean(aDefault);
      }
      case "array": {
        return createWidgetForArray();
      }
    }

    return null;
  }

  private Widget createWidgetForBoolean(final JSONValue aDefault) {
    CheckBox checkBox = new CheckBox();

    if (aDefault != null && aDefault.isBoolean() != null) {
      checkBox.setValue(aDefault.isBoolean().booleanValue());
    }

    return checkBox;
  }

  private Widget createWidgetForNumber(final JSONValue aDefault) {
    DoubleBox input = new DoubleBox();
    input.setName(key);
    input.getElement().setAttribute("type", "number");
    input.getElement().setAttribute("step", "0.001");
    setNumericSchemaValidations(input);

    if(aDefault != null && aDefault.isNumber() != null) {
      double defaultDoubleValue = aDefault.isNumber().doubleValue();
      input.setValue(defaultDoubleValue);
    }

    return input;
  }

  private Widget createWidgetForInteger(final JSONValue aDefault) {
    HtmlIntegerBox input = new HtmlIntegerBox();
    input.setName(key);
    setNumericSchemaValidations(input);

    if(aDefault != null && aDefault.isNumber() != null) {
      double defaultDoubleValue = aDefault.isNumber().doubleValue();
      input.setValue(Double.valueOf(defaultDoubleValue).intValue());
    }

    return input;
  }

  private Widget createWidgetForString(final JSONValue aDefault) {

    List<JSONObject> enumItems = JsonSchemaGWT.getEnum(schema);
    boolean hasEnum = enumItems.size() > 0;

    if(("file".equals(format) || "folder".equals(format)) && eventBus != null) {
      return new FileSelection(eventBus, format);
    }

    if (format.equals("textarea")) {
      TextArea textArea = new TextArea();

      JSONValue rowsValue = schema.get("rows");
      if (rowsValue != null && rowsValue.isNumber() != null) {
        int numberOfRows = Double.valueOf(rowsValue.isNumber().doubleValue()).intValue();
        textArea.setVisibleLines(numberOfRows > 2 ? numberOfRows : 2);
      } else {
        textArea.setVisibleLines(2);
      }

      textArea.setName(key);
      setStringSchemaValidations(textArea);
      setDefaultStringValue(textArea, aDefault);

      return textArea;
    }

    if(hasEnum) {
      return createWidgetForStringWithEnum(enumItems);
    }

    TextBox input = format.equals("password") ? new PasswordTextBox() : new TextBox();
    input.setName(key);
    setStringSchemaValidations(input);

    setDefaultStringValue(input, aDefault);

    return input;
  }

  private void setDefaultStringValue(final HasValue<String> widget, final JSONValue aDefault) {
    if(aDefault != null && aDefault.isString() != null) {
      String defaultStringValue = aDefault.isString().stringValue();
      widget.setValue(defaultStringValue);
    }
  }

  private Widget createWidgetForStringWithEnum(@NotNull final List<JSONObject> enumItems) {
    if(format.equals("radio")) {
      return new DynamicRadioGroup(key, enumItems);
    }

    OpalListBox listBox = new OpalListBox();
    listBox.setName(key);

    for(JSONObject item : enumItems) {
      String key = item.get("key").isString().stringValue();
      String title = item.containsKey("title") ? item.get("title").isString().stringValue() : key;
      listBox.addItem(title, key);
    }

    return listBox;
  }

  private Widget createWidgetForArray() {
    JSONValue itemsSchema = schema.get("items");

    if (itemsSchema != null) {
      if (itemsSchema.isArray() != null) {
        return new DynamicArrayTuples(key, itemsSchema.isArray(), required, eventBus);
      } else {
        JSONObject items = itemsSchema.isObject() != null ? itemsSchema.isObject() : new JSONObject();

        List<JSONObject> enumItems = JsonSchemaGWT.getEnum(items);

        if (enumItems.size() == 0) {
          String type = JsonSchemaGWT.getType(items);
          return new DynamicArrayItems(key, type);
        }

        return new DynamicCheckboxGroup(key, enumItems);
      }
    }

    return new DynamicArrayItems(key, "string");
  }

  private void setStringSchemaValidations(Widget widget) {
    JSONValue maxLength = schema.get("maxLength");
    if(maxLength != null && maxLength.isNumber() != null) {
      if(maxLength.isNumber().doubleValue() > 0)
        widget.getElement().setAttribute("maxlength", maxLength.isNumber().toString());
    }

    JSONValue minLength = schema.get("minLength");
    if(minLength != null && minLength.isNumber() != null) {
      if(minLength.isNumber().doubleValue() > 0)
        widget.getElement().setAttribute("minlength", minLength.isNumber().toString());
    }

    JSONValue pattern = schema.get("pattern");
    if(pattern != null && pattern.isString() != null) {
      widget.getElement().setAttribute("pattern", pattern.isString().stringValue());
    }
  }

  private void setNumericSchemaValidations(Widget widget) {
    JSONValue maximum = schema.get("maximum");
    if(maximum != null && maximum.isNumber() != null) {
      widget.getElement().setAttribute("max", maximum.isNumber().toString());
    }

    JSONValue minimum = schema.get("minimum");
    if(minimum != null && minimum.isNumber() != null) {
      widget.getElement().setAttribute("min", minimum.isNumber().toString());
    }
  }
}
