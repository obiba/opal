package org.obiba.opal.web.gwt.app.client.support.jsonschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.ui.SchemaUiContainer;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Panel;

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

  public static void buildUiIntoPanel(final JSONObject jsonSchema, Panel containerPanel) {
    JSONObject properties = getProperties(jsonSchema);
    List<String> required = getRequired(jsonSchema);

    Set<String> keys = properties.keySet();
    for(String key : keys) {
      JSONObject schema = getSchema(properties, key);
      if(schema != null) {
        containerPanel.add(new SchemaUiContainer(schema, key, required.indexOf(key) > -1));
      }
    }
  }

  public static boolean valueForStringSchemaIsValid(String value, JSONObject schema) {
    boolean valid = true;

    if (value != null) {
      JSONValue maxLength = schema.get("maxLength");
      if (maxLength != null && maxLength.isNumber() != null) {
        double maxLengthValue = maxLength.isNumber().doubleValue();
        if (maxLengthValue > 0) valid = value.length() <= maxLengthValue;
      }

      JSONValue minLength = schema.get("minLength");
      if (minLength != null && minLength.isNumber() != null) {
        double minLengthValue = minLength.isNumber().doubleValue();
        if (minLengthValue > 0) valid = valid && value.length() >= minLengthValue;
      }

      JSONValue pattern = schema.get("pattern");
      if (pattern != null && pattern.isString().stringValue() != null) {
        String patternValue = pattern.isString().stringValue();
        valid = valid && RegExp.compile(patternValue).test(value);
      }
    }

    return valid;
  }

  public static boolean valueForNumericSchemaIsValid(Number value, JSONObject schema) {
    boolean valid = true;

    if (value != null) {
      double doubleValue = value.doubleValue();

      JSONValue maximum = schema.get("maximum");
      if (maximum != null && maximum.isNumber() != null) {
        double maximumValue = maximum.isNumber().doubleValue();
        valid = doubleValue <= maximumValue;
      }

      JSONValue minimum = schema.get("minimum");
      if (minimum != null && minimum.isNumber() != null) {
        double minimumValue = minimum.isNumber().doubleValue();
        valid = valid && doubleValue >= minimumValue;
      }
    }

    return valid;
  }
}
