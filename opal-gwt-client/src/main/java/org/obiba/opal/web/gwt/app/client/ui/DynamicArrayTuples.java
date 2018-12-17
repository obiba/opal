package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicArrayTuples extends Composite implements TakesValue<JSONArray>, HasEnabled {

  private final String key;
  private final JSONArray items;
  private final EventBus eventBus;

  private FlowPanel bodyContainer;

  private Map<FlowPanel, List<SchemaUiContainer>> itemsContainers;

  public DynamicArrayTuples(String key, JSONArray items, EventBus eventBus) {
    this.key = key;
    this.items = items;
    this.eventBus = eventBus;

    itemsContainers = new HashMap<FlowPanel, List<SchemaUiContainer>>();

    FlowPanel headerContainer = new FlowPanel();
    headerContainer.getElement().setAttribute("style", "display: grid; grid-template-columns: repeat(" + (items.size() < 1 ? 1 : items.size()) + ", 1fr) 50px;");
    header(headerContainer);

    bodyContainer = new FlowPanel();

    FlowPanel root = new FlowPanel();
    root.add(headerContainer);
    root.add(bodyContainer);
    createAddMoreButton(root);

    initWidget(root);
  }

  public String getKey() {
    return key;
  }

  public String getFormKey() {
    return key.endsWith("[]") ? key : key + "[]";
  }

  public Map<FlowPanel, List<SchemaUiContainer>> getItemsContainers() {
    return itemsContainers;
  }

  @Override
  public void setValue(JSONArray value) {

  }

  @Override
  public JSONArray getValue() {
    JSONArray array = new JSONArray();

    Collection<List<SchemaUiContainer>> values = getItemsContainers().values();

    int i = 0;
    for (List<SchemaUiContainer> value: values) {
      JSONObject jsonObject = new JSONObject();

      for (SchemaUiContainer schemaUiContainer : value) {
        jsonObject.put(schemaUiContainer.getKey(), schemaUiContainer.getJSONValue());
      }

      array.set(i++, jsonObject);
    }

    return array;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void setEnabled(boolean enabled) {

  }

  private String getSchemaKey(JSONObject schema) {
    if (schema == null) {
      return null;
    }

    JSONValue schemaKey = schema.get("key");
    return schemaKey.isString() != null ? schemaKey.isString().stringValue() : null;
  }

  private void createAddMoreButton(FlowPanel root) {
    ControlGroup controlGroup = new ControlGroup();

    Button button = new Button("<i style='font-weight: 900;'>&plus;</i>");
    button.addStyleName("btn btn-info");

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        body();
      }
    });

    controlGroup.add(button);
    root.add(controlGroup);
  }

  private void body() {
    int size = items.size();
    FlowPanel bodyItem = new FlowPanel();
    bodyItem.getElement().setAttribute("style", "display: grid; grid-template-columns: repeat(" + (size < 1 ? 1 : size) + ", 1fr) 50px;");

    List<SchemaUiContainer> widgetList = new ArrayList<SchemaUiContainer>();
    for (int i = 0; i < size; i++) {
      JSONValue jsonValue = items.get(i);
      JSONObject schema = jsonValue.isObject();

      String schemaKeyString = getSchemaKey(schema);

      SchemaUiContainer uiContainer = new SchemaUiContainer(schema, schemaKeyString, false, eventBus);

      bodyItem.add(uiContainer);
      widgetList.add(uiContainer);
    }

    ControlGroup controlGroup = new ControlGroup();
    Button button = new Button("<i class='icon-trash'></i>");
    button.getElement().addClassName("btn btn-danger");
    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Button source = (Button) event.getSource();
        Widget parent = source.getParent().getParent();

        getItemsContainers().remove(parent);
        parent.removeFromParent();
      }
    });

    controlGroup.add(button);
    bodyItem.add(controlGroup);

    bodyContainer.add(bodyItem);

    itemsContainers.put(bodyItem, widgetList);
  }

  private void header(FlowPanel container) {
    int size = items.size();

    for (int i = 0; i < size; i++) {
      JSONValue jsonValue = items.get(i);

      JSONObject schema = jsonValue.isObject();

      String schemaKeyString = getSchemaKey(schema);

      ControlGroup controlPanel = new ControlGroup();
      Label label = new Label();
      label.setText(schemaKeyString);

      controlPanel.add(label);

      container.add(controlPanel);
    }

    container.add(new FlowPanel());
  }
}
