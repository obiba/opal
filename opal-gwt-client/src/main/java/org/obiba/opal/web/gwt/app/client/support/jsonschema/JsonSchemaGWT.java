package org.obiba.opal.web.gwt.app.client.support.jsonschema;

import java.util.Iterator;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.FlowPanel;

public class JsonSchemaGWT {

  private static boolean typeIsValueBox(String type) {
    return type.equals("string") || type.equals("number") || type.equals("integer");
  }

  private static JSONObject getSchema(final JSONObject rootSchema, String key) {
    final JSONValue schema = rootSchema.get(key);
    return schema != null && schema.isObject() != null ? schema.isObject() : null;
  }

  private static JSONObject getSchemaProperties(final JSONObject jsonSchema) {
    JSONValue properties = jsonSchema.get("properties");
    return properties != null && properties.isObject() != null ? properties.isObject() : null;
  }

  private static String getSchemaType(final JSONObject schema) {
    final JSONValue type = schema.get("type");
    return type != null && type.isString() != null ? type.isString().stringValue() : "string";
  }

  public static FlowPanel createUI(final JSONObject jsonSchema, FlowPanel containerPanel) {
    JSONObject properties = getSchemaProperties(jsonSchema);

    if (properties != null) {
      Set<String> keys = properties.keySet();
      for(Iterator<String> i = keys.iterator(); i.hasNext(); ) {
        String key = i.next();

        JSONObject schema = getSchema(properties, key);
        if (schema != null) {
          ControlGroup controlGroup = new ControlGroup();

          JSONValue title = schema.get("title");
          if(title != null && title.isString() != null) {
            controlGroup.add(new ControlLabel(title.isString().stringValue()));
          }

          TextBox input = new TextBox();
          input.setId(key);
          controlGroup.add(input);

          containerPanel.add(controlGroup);
        }
      }
    }

    return containerPanel;
  }
}
