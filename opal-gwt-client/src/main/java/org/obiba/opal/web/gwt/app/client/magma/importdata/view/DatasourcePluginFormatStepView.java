package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DatasourcePluginFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.SchemaUiContainer;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DatasourcePluginFormatStepView extends ViewImpl implements DatasourcePluginFormatStepPresenter.Display {

  @UiField
  FlowPanel containerPanel;

  private ModalUiHandlers uiHandlers;

  private String selectedPluginName;

  @Inject
  public DatasourcePluginFormatStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setUiHandlers(ModalUiHandlers uiHandlers) {
    this.uiHandlers = uiHandlers;
  }

  @Override
  public void setDatasourcePluginName(String name) {

    selectedPluginName = name;

    ResourceRequestBuilderFactory.<JavaScriptObject>newBuilder()
        .forResource(UriBuilders.DS_PLUGIN_SERVICE.create().build(name))
        .withCallback(new ResourceCallback<JavaScriptObject>() {

          @Override
          public void onResource(Response response, JavaScriptObject resource) {
            JSONObject jsonSchema = new JSONObject(resource);
            JsonSchemaGWT.buildUiIntoPanel(jsonSchema, containerPanel);
          }
        })
        .get().send();
  }

  @Override
  public boolean jsonSchemaValuesAreValid() {
    boolean isValid = true;
    Iterator<Widget> iterator = containerPanel.iterator();

    while(isValid && iterator.hasNext()) {
      Widget widget = iterator.next();

      if (widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        isValid = isValid && widgetAsSchemaUiContainer.isValid();
      }
    }

    return isValid;
  }

  @Override
  public JSONObject getCurrentValues() {
    JSONObject jsonObject = new JSONObject();

    for(Widget widget : containerPanel) {
      if(widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;

        Object value = widgetAsSchemaUiContainer.getValue();
        if (value != null) {
          jsonObject.put(widgetAsSchemaUiContainer.getKey(), JSONParser.parseStrict(value.toString()));
        }
      }
    }

    return jsonObject;
  }

  @Override
  public Map<HasType<ControlGroupType>, String> getErrors() {
    Map<HasType<ControlGroupType>, String> errors = new HashMap<>();

    for(Widget widget : containerPanel) {
      if (widget instanceof SchemaUiContainer) {
        SchemaUiContainer widgetAsSchemaUiContainer = (SchemaUiContainer) widget;
        if (!widgetAsSchemaUiContainer.isValid()) errors.put(widgetAsSchemaUiContainer, widgetAsSchemaUiContainer.getTitle());
      }
    }

    return errors;
  }

  public ModalUiHandlers getUiHandlers() {
    return uiHandlers;
  }

  @Override
  public String getSelectedPluginName() {
    return selectedPluginName;
  }

  interface Binder extends UiBinder<Widget, DatasourcePluginFormatStepView> {}
}
