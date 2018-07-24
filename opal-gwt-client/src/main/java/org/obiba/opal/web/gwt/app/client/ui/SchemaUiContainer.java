package org.obiba.opal.web.gwt.app.client.ui;

import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;

import com.github.gwtbootstrap.client.ui.ControlLabel;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class SchemaUiContainer extends com.github.gwtbootstrap.client.ui.ControlGroup {

  private JSONObject schema;
  private String key;

  public SchemaUiContainer(JSONObject schema, String key) {
    this.schema = schema;
    this.key = key;

    setUp();
  }

  public SchemaUiContainer(JSONObject schema, String key, String html) {
    super(html);
    this.schema = schema;
    this.key = key;

    setUp();
  }

  public void getValue() {

  }

  public JSONObject getSchema() {
    return schema;
  }

  public void setSchema(JSONObject schema) {
    this.schema = schema;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  private void setUp() {
    JSONValue title = schema.get("title");
    if(title != null && title.isString() != null) {
      add(new ControlLabel(title.isString().stringValue()));
    }

    // find out what to do with type
    add(buildInputWidget());

    JSONValue description = schema.get("description");
    if(description != null && description.isString() != null) {
      add(new ControlLabel(description.isString().stringValue()));
    }
  }

  private FocusWidget buildInputWidget() {
    String type = JsonSchemaGWT.getType(schema);
    String format = JsonSchemaGWT.getFormat(schema);

    JSONValue anEnum = schema.get("enum");
    boolean hasEnum = anEnum != null && anEnum.isArray() != null;
    // validation for enum, must create a ListBox for those, currently easy to implement for type == string

    // for now 3 cases: number, integer and string
    switch(type) {
      case "number": {
        DoubleBox input = new DoubleBox();
        input.setName(key);
        input.getElement().setAttribute("type", "number");
        input.getElement().setAttribute("step", "0.001");
        return input;
      }
      case "integer": {
        IntegerBox input = new IntegerBox();
        input.setName(key);
        input.getElement().setAttribute("type", "number");
        input.getElement().setAttribute("step", "1");
        return input;
      }
      default: {
        // most generic, must take into account that type can be one of the six primitive types ("null", "boolean", "object", "array", "number", or "string"), or "integer"
        if (hasEnum) {
          JSONArray enumArray = anEnum.isArray();

          ListBox listBox = new ListBox();
          listBox.setName(key);

          listBox.addItem("", HasDirection.Direction.DEFAULT, null);
          listBox.getElement().getFirstChildElement().setAttribute("disabled" ,"disabled" );
          for(int i = 0; i < enumArray.size(); i++) {
            listBox.addItem(enumArray.get(i).isString().stringValue());
          }

          return listBox;
        }

        TextBox input = new TextBox();
        input.setName(key);
        return input;
      }
    }
  }
}
