package org.obiba.opal.web.gwt.app.client.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;

public class SchemaUiContainer extends com.github.gwtbootstrap.client.ui.ControlGroup {

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

      if (widget instanceof TakesValue || widget instanceof ListBox) {
        found = true;
        value = widget instanceof TakesValue ? ((TakesValue) widget).getValue() : ((ListBox) widget).getSelectedValue();
      }
    }

    return value;
  }

  public JSONValue getJSONValue() {
    Object value = getValue();

    if (value == null) return null;
    if (type.equals("array")) {
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

  public boolean isValid() {
    // create a schema validator? is this useful if the validation is done through the browser (HTML5)?
    boolean valid = true;
    Object value = getValue();

    switch(type) {
      case "string": {
        if (required) {
          valid = value != null && ((String) value).trim().length() > 0;
        }

        JSONValue fileFormats = schema.get("fileFormats");
        if(format.equals("file") && (fileFormats != null && fileFormats.isArray() != null)) {
          valid = valid && validateFileFormat(value, fileFormats.isArray());
        } else {
          valid = valid &&
              JsonSchemaGWT.valueForStringSchemaIsValid(value instanceof String ? (String) value : null, schema);
        }

        break;
      }
      case "integer":
      case "number": {
        if (required) {
          valid = value != null;
        }

        valid = valid &&
            JsonSchemaGWT.valueForNumericSchemaIsValid(value instanceof Number ? (Number) value : null, schema);
        break;
      }
      case "array": {
        if (required) {
          valid = value != null && ((HashSet) value).size() > 0;
        }

        valid = valid &&
            JsonSchemaGWT.valueForArraySchemaIsValid(value instanceof HashSet ? (HashSet) value : null, schema);
        break;
      }
    }

    return valid;
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

    if (widget != null) {
      JSONValue title = schema.get("title");
      if(title != null && title.isString() != null) {
        String titleStringValue = title.isString().stringValue();
        add(new ControlLabel(titleStringValue));
        setTitle(titleStringValue);
      }

      // find out what to do with type
      if (required) widget.getElement().setAttribute("required", "required");
      add(widget);

      JSONValue description = schema.get("description");
      if(description != null && description.isString() != null) {
        add(new ControlLabel(description.isString().stringValue()));
      }
    }
  }

  private Widget buildInputWidget() {
    JSONValue aDefault = schema.get("default");

    // for now 3 cases: number, integer and string
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
      case "array": {
        return createWidgetForArrayWithEnumItems();
      }
    }

    return null;
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
    IntegerBox input = new IntegerBox();
    input.setName(key);
    input.getElement().setAttribute("type", "number");
    input.getElement().setAttribute("step", "1");
    setNumericSchemaValidations(input);

    if(aDefault != null && aDefault.isNumber() != null) {
      double defaultDoubleValue = aDefault.isNumber().doubleValue();
      input.setValue(Double.valueOf(defaultDoubleValue).intValue());
    }

    return input;
  }

  private Widget createWidgetForString(final JSONValue aDefault) {

    List<String> enumItems = JsonSchemaGWT.getEnum(schema);
    boolean hasEnum = enumItems.size() > 0;

    if (format.equals("file")) {
      return new FileSelection(eventBus);
    }

    if (hasEnum) {
      return createWidgetForStringWithEnum(enumItems);
    }

    TextBox input = format.equals("password") ? new PasswordTextBox() : new TextBox();
    input.setName(key);
    setStringSchemaValidations(input);

    if (aDefault != null && aDefault.isString() != null) {
      String defaultStringValue = aDefault.isString().stringValue();
      input.setValue(defaultStringValue);
    }

    return input;
  }

  private Widget createWidgetForStringWithEnum(@NotNull final List<String> enumItems) {
    if(format.equals("radio")) {
      return new DynamicRadioGroup(key, enumItems);
    }

    ListBox listBox = new ListBox();
    listBox.setName(key);

    for(String item : enumItems) {
      listBox.addItem(item);
    }

    return listBox;
  }

  private Widget createWidgetForArrayWithEnumItems() {
    JSONValue itemsSchema = schema.get("items");
    JSONObject items = itemsSchema != null && itemsSchema.isObject() != null
        ? itemsSchema.isObject()
        : new JSONObject();

    return new DynamicCheckboxGroup(key, JsonSchemaGWT.getEnum(items));
  }

  private boolean validateFileFormat(Object value, JSONArray fileFormatsArray) {
    boolean formatIsvalid = false;
    int i = 0;
    while (!formatIsvalid && i < fileFormatsArray.size()) {
      String fileFormat = fileFormatsArray.get(i).isString().stringValue();
      formatIsvalid = fileFormat.equals(value);
      i++;
    }

    return formatIsvalid;
  }

  private void setStringSchemaValidations(Widget widget) {
    JSONValue maxLength = schema.get("maxLength");
    if (maxLength != null && maxLength.isNumber() != null) {
      if (maxLength.isNumber().doubleValue() > 0) widget.getElement().setAttribute("maxlength", maxLength.isNumber().toString());
    }

    JSONValue minLength = schema.get("minLength");
    if (minLength != null && minLength.isNumber() != null) {
      if (minLength.isNumber().doubleValue() > 0) widget.getElement().setAttribute("minlength", minLength.isNumber().toString());
    }

    JSONValue pattern = schema.get("pattern");
    if (pattern != null && pattern.isString() != null) {
      widget.getElement().setAttribute("pattern", pattern.isString().stringValue());
    }
  }

  private void setNumericSchemaValidations(Widget widget) {
    JSONValue maximum = schema.get("maximum");
    if (maximum != null && maximum.isNumber() != null) {
      widget.getElement().setAttribute("max", maximum.isNumber().toString());
    }

    JSONValue minimum = schema.get("minimum");
    if (minimum != null && minimum.isNumber() != null) {
      widget.getElement().setAttribute("min", minimum.isNumber().toString());
    }
  }
}
