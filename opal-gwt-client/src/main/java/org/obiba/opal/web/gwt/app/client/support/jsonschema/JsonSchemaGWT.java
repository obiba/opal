/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support.jsonschema;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.ui.SchemaUiContainer;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Panel;
import com.google.web.bindery.event.shared.EventBus;

public abstract class JsonSchemaGWT {

  private static JSONObject getProperties(final JSONObject jsonSchema) {
    JSONValue properties = jsonSchema.get("properties");
    return properties != null && properties.isObject() != null ? properties.isObject() : new JSONObject();
  }

  private static JSONObject getSchema(final JSONObject rootSchema, String key) {
    final JSONValue schema = rootSchema.get(key);
    return schema != null && schema.isObject() != null ? schema.isObject() : null;
  }

  private static List<String> getRequired(final JSONObject jsonSchema) {
    JSONValue required = jsonSchema.get("required");
    JSONArray jsonArray = required != null && required.isArray() != null ? required.isArray() : new JSONArray();
    List<String> list = new ArrayList<>();

    for(int i = 0; i < jsonArray.size(); i++) {
      JSONString key = jsonArray.get(i).isString();
      if (key != null) {
        list.add(key.stringValue());
      }
    }

    return list;
  }

  public static String getFormat(final JSONObject schema) {
    final JSONValue format = schema.get("format");
    return format != null && format.isString() != null ? format.isString().stringValue() : null;
  }

  public static String getType(final JSONObject schema) {
    final JSONValue type = schema.get("type");
    return type != null && type.isString() != null ? type.isString().stringValue() : "string";
  }

  public static List<String> getEnum(final JSONObject schema) {
    List<String> result = new ArrayList<>();

    JSONValue anEnum = schema.get("enum");
    if (anEnum != null && anEnum.isArray() != null) {
      JSONArray enumArray = anEnum.isArray();

      for(int i = 0; i < enumArray.size(); i++) {
        result.add(enumArray.get(i).isString().stringValue());
      }
    }

    return result;
  }

  public static void buildUiIntoPanel(final JSONObject jsonSchema, final JSONObject initialValues, Panel containerPanel, EventBus eventBus) {
    String rootSchemaType = getType(jsonSchema);
    List<String> required = getRequired(jsonSchema);

    JSONObject valueObject = initialValues != null ? initialValues : new JSONObject();

    JSONValue readOnly = jsonSchema.get("readOnly");
    boolean wholeSchemaIsReadOnly = (readOnly != null && readOnly.isBoolean() != null) && readOnly.isBoolean().booleanValue();

    if ("object".equals(rootSchemaType)) {
      JSONObject properties = getProperties(jsonSchema);

      Set<String> keys = properties.keySet();
      for(String key : keys) {
        JSONObject schema = getSchema(properties, key);
        if(schema != null) {
          addToPanel(schema, key, valueObject, wholeSchemaIsReadOnly, required, containerPanel, eventBus);
        }
      }
    } else {
      JSONValue itemsValue = jsonSchema.get("items");
      if (itemsValue != null && itemsValue.isArray() != null) {
        JSONArray items = itemsValue.isArray();

        for(int i = 0; i < items.size(); i++) {
          JSONValue schemaValue = items.get(i);
          if (schemaValue != null && schemaValue.isObject() != null) {
            JSONObject schema = schemaValue.isObject();
            JSONValue keyValue = schema.get("key");
            String key = keyValue != null && keyValue.isString() != null ? keyValue.isString().stringValue() : "key" + i;

            addToPanel(schema, key, valueObject, wholeSchemaIsReadOnly, required, containerPanel, eventBus);
          }
        }
      }
    }
  }

  public static Map<HasType<ControlGroupType>, String> validate(final Panel containerPanel) {
    Map<HasType<ControlGroupType>, String> errors = new HashMap<>();

    for(Widget widget : containerPanel) {
      if (widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        String validationError = widgetAsSchemaUiContainer.validate();

        if (validationError.length() > 0) {
          errors.put(widgetAsSchemaUiContainer, widgetAsSchemaUiContainer.getTitle() + ": " + validationError);
        }
      }
    }

    return errors;
  }

  public static JSONObject getModel(final Panel containerPanel) {
    JSONObject jsonObject = new JSONObject();

    for(Widget widget : containerPanel) {
      if(widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        jsonObject.put(widgetAsSchemaUiContainer.getKey(), widgetAsSchemaUiContainer.getJSONValue());
      }
    }

    return jsonObject;
  }

  public static String valueForStringSchemaIsValid(String value, JSONObject schema) {
    String error = "";

    if (value != null) {
      JSONValue maxLength = schema.get("maxLength");
      if (maxLength != null && maxLength.isNumber() != null) {
        double maxLengthValue = maxLength.isNumber().doubleValue();
        if (maxLengthValue > 0 && value.length() > maxLengthValue) return "maxLength";
      }

      JSONValue minLength = schema.get("minLength");
      if (minLength != null && minLength.isNumber() != null) {
        double minLengthValue = minLength.isNumber().doubleValue();
        if (minLengthValue > 0 && value.length() < minLengthValue) return "minLength";
      }

      JSONValue pattern = schema.get("pattern");
      if (pattern != null && pattern.isString().stringValue() != null) {
        String patternValue = pattern.isString().stringValue();
        if (!RegExp.compile(patternValue).test(value)) return "pattern";
      }
    }

    return error;
  }

  public static String valueForNumericSchemaIsValid(Number value, JSONObject schema) {
    String error = "";

    if (value != null) {
      double doubleValue = value.doubleValue();

      JSONValue maximum = schema.get("maximum");
      if (maximum != null && maximum.isNumber() != null) {
        double maximumValue = maximum.isNumber().doubleValue();
        if (doubleValue > maximumValue) return "maximum";
      }

      JSONValue minimum = schema.get("minimum");
      if (minimum != null && minimum.isNumber() != null) {
        double minimumValue = minimum.isNumber().doubleValue();
        if (doubleValue < minimumValue) return "minimum";
      }
    }

    return error;
  }

  public static String valueForArraySchemaIsValid(Collection value, JSONObject schema) {
    String error = "";

    if (value != null) {
      JSONValue maxItems = schema.get("maxItems");
      if (maxItems != null && maxItems.isNumber() != null) {
        double maxItemsValue = maxItems.isNumber().doubleValue();
        if (value.size() > maxItemsValue) return "maxItems";
      }

      JSONValue minItems = schema.get("minItems");
      if (minItems != null && minItems.isNumber() != null) {
        double minItemsValue = minItems.isNumber().doubleValue();
        if (value.size() < minItemsValue) return "minItems";
      }
    }

    return error;
  }

  public static List<JSONObject> jsonArrayOfObjectsToList(JSONArray array) {
    List<JSONObject> objects = new ArrayList<JSONObject>();

    for (int i = 0; i < array.size(); i++) {
      JSONValue jsonValue = array.get(i);

      if (jsonValue.isObject() != null)
        objects.add(jsonValue.isObject());
    }

    return objects;
  }

  private static void addToPanel(final JSONObject schema, String key, final JSONObject initialValue, boolean wholeSchemaIsReadOnly, List<String> required, Panel containerPanel,  EventBus eventBus) {
    schema.put("readOnly", JSONBoolean.getInstance(wholeSchemaIsReadOnly));
    SchemaUiContainer uiContainer = new SchemaUiContainer(schema, key,required.indexOf(key) > -1, eventBus);

    uiContainer.setJSONValue(initialValue.get(key));
    containerPanel.add(uiContainer);
  }
}
