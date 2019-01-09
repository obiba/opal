package org.obiba.opal.web.gwt.app.client.ui;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
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
import java.util.Set;

public class DynamicArrayTuples extends Composite implements TakesValue<JSONArray>, HasEnabled {

  private final String key;
  private final JSONArray items;
  private final boolean required;
  private final EventBus eventBus;

  private FlowPanel bodyContainer;

  private Map<FlowPanel, List<SchemaUiContainer>> itemsContainers;

  private Button addMoreButton;

  private List<ControlGroup> headerControlGroups;

  private boolean enabled;

  public DynamicArrayTuples(String key, JSONArray items, boolean required, EventBus eventBus) {
    this.enabled = true;
    this.key = key;
    this.items = items;
    this.required = required;
    this.eventBus = eventBus;

    itemsContainers = new HashMap<FlowPanel, List<SchemaUiContainer>>();
    headerControlGroups = new ArrayList<ControlGroup>();

    FlowPanel headerContainer = new FlowPanel();
    headerContainer.getElement().setAttribute("style", "display: grid; grid-template-columns: repeat(" + (items.size() < 1 ? 1 : items.size()) + ", 1fr) 50px;");
    header(headerContainer);
    headerVisibility(false);

    bodyContainer = new FlowPanel();

    FlowPanel root = new FlowPanel();
    root.add(headerContainer);
    root.add(bodyContainer);

    addMoreButton = createAddMoreButton(root);

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
    if (value != null) {
      int size = value.size();

      for (int i = 0; i < size; i++) {
        JSONValue jsonValue = value.get(i);
        if (jsonValue.isObject() != null) {
          body(jsonValue.isObject());
        }
      }
    }
  }

  @Override
  public JSONArray getValue() {
    JSONArray array = new JSONArray();
    Collection<List<SchemaUiContainer>> values = getItemsContainers().values();

    int i = 0;
    for (List<SchemaUiContainer> value : values) {
      JSONObject jsonObject = new JSONObject();

      boolean empty = false;

      for (SchemaUiContainer schemaUiContainer : value) {
        jsonObject.put(schemaUiContainer.getKey(), schemaUiContainer.getJSONValue());

        String validate = schemaUiContainer.validate();
        empty = empty || "required".equals(validate);
      }

      if (!empty)
        array.set(i++, jsonObject);
    }

    return array;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    addMoreButton.setVisible(enabled);
    addMoreButton.setVisible(enabled);

    Set<FlowPanel> flowPanels = getItemsContainers().keySet();

    for (FlowPanel flowPanel : flowPanels) {
      for (Widget widget: flowPanel) {
        if (widget instanceof ControlGroup) {
          ControlGroup controlGroup = (ControlGroup) widget;
          for (Widget control : controlGroup) {
            if (control instanceof HasEnabled) ((HasEnabled) control).setEnabled(enabled);
            if (control instanceof Button) control.setVisible(enabled);
          }
        }
      }
    }
  }

  public boolean isRequired() {
    return required;
  }

  private String getSchemaKey(JSONObject schema) {
    if (schema == null) {
      return null;
    }

    JSONValue schemaKey = schema.get("key");
    return schemaKey.isString() != null ? schemaKey.isString().stringValue() : null;
  }

  private boolean hasTitle(JSONObject schema) {
    if (schema == null) {
      return false;
    }

    return schema.containsKey("title");
  }

  private Button createAddMoreButton(FlowPanel root) {
    ControlGroup controlGroup = new ControlGroup();

    Button button = new Button("<i class='icon-plus'></i>");
    button.addStyleName("btn btn-info");

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        body(null);
        headerVisibility(!getItemsContainers().isEmpty());
      }
    });

    controlGroup.add(button);
    root.add(controlGroup);

    return button;
  }

  private void body(JSONObject initialValue) {
    int size = items.size();
    FlowPanel bodyItem = new FlowPanel();
    bodyItem.getElement().setAttribute("style", "display: grid; grid-template-columns: repeat(" + (size < 1 ? 1 : size) + ", 1fr) 50px;");

    List<SchemaUiContainer> widgetList = new ArrayList<SchemaUiContainer>();
    for (int i = 0; i < size; i++) {
      JSONValue jsonValue = items.get(i);
      JSONObject schema = jsonValue.isObject();

      schema.put("readOnly", JSONBoolean.getInstance(!enabled));

      String schemaKeyString = getSchemaKey(schema);

      SchemaUiContainer uiContainer = new SchemaUiContainer(schema, schemaKeyString, required, eventBus);

      if (initialValue != null && initialValue.containsKey(schemaKeyString)) {
        uiContainer.setJSONValue(initialValue.get(schemaKeyString));
      }

      bodyItem.add(uiContainer);
      widgetList.add(uiContainer);
    }

    ControlGroup controlGroup = new ControlGroup();
    Button button = new Button("<i class='icon-remove'></i>");
    button.getElement().addClassName("btn");

    button.setVisible(enabled);

    button.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Button source = (Button) event.getSource();
        Widget parent = source.getParent().getParent();

        if (parent instanceof FlowPanel) {
          getItemsContainers().remove(parent);
          parent.removeFromParent();
        }

        headerVisibility(!getItemsContainers().isEmpty());
      }
    });

    button.setEnabled(enabled);

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

      ControlGroup controlPanel = new ControlGroup();

      if (!hasTitle(schema)) {
        String schemaKeyString = getSchemaKey(schema);
        Label label = new Label();
        label.getElement().setAttribute("style", "font-weight: bold;");
        label.setText(schemaKeyString);
        controlPanel.add(label);
      }

      container.add(controlPanel);
      headerControlGroups.add(controlPanel);
    }

    container.add(new FlowPanel());
  }

  private void headerVisibility(boolean visible) {
    for (ControlGroup header : headerControlGroups) {
      header.setVisible(visible);
    }
  }
}
