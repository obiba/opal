package org.obiba.opal.web.gwt.app.client.support.jsonschema;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class JsonSchemaGWT {

  public static List<String> getRequired(final JSONObject jsonSchema) {
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

  public static String getType(final JSONObject schema) {
    final JSONValue type = schema.get("type");
    return type != null && type.isString() != null ? type.isString().stringValue() : "string";
  }

  public static void buildUiIntoPanel(final JSONObject jsonSchema, Panel containerPanel) {
    JSONObject properties = getProperties(jsonSchema);

    Set<String> keys = properties.keySet();
    for(String key : keys) {
      JSONObject schema = getSchema(properties, key);
      if(schema != null) {
        containerPanel.add(buildControlGroup(schema, key));
      }
    }
  }

  private static JSONObject getProperties(final JSONObject jsonSchema) {
    JSONValue properties = jsonSchema.get("properties");
    return properties != null && properties.isObject() != null ? properties.isObject() : new JSONObject();
  }

  private static JSONObject getSchema(final JSONObject rootSchema, String key) {
    final JSONValue schema = rootSchema.get(key);
    return schema != null && schema.isObject() != null ? schema.isObject() : null;
  }

  private static ControlGroup buildControlGroup(final JSONObject schema, String key) {
    ControlGroup controlGroup = new ControlGroup();

    JSONValue title = schema.get("title");
    if(title != null && title.isString() != null) {
      controlGroup.add(new ControlLabel(title.isString().stringValue()));
    }

    // find out what to do with type
    controlGroup.add(buildInputWidget(schema, key));

    JSONValue description = schema.get("description");
    if(description != null && description.isString() != null) {
      controlGroup.add(new ControlLabel(description.isString().stringValue()));
    }

    return controlGroup;
  }

  private static Widget buildInputWidget(final JSONObject schema, String key) {
    String type = getType(schema);

    JSONValue anEnum = schema.get("enum");
    if (anEnum != null && anEnum.isArray() != null) {
      // validation for enum, must create a ListBox for those, currently easy to implement for type == string
    }

    // most generic, must take into account that type can be one of the six primitive types ("null", "boolean", "object", "array", "number", or "string"), or "integer"
    TextBox input = new TextBox();
    input.setId(key);

    return input;
  }
}
