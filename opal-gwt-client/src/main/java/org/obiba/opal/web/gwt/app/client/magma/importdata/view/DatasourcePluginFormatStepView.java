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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.ViewImpl;

public class DatasourcePluginFormatStepView extends ViewImpl implements DatasourcePluginFormatStepPresenter.Display {

  @UiField
  FlowPanel containerPanel;

  private ModalUiHandlers uiHandlers;

  private String selectedPluginName;

  private final EventBus eventBus;

  @Inject
  public DatasourcePluginFormatStepView(Binder uiBinder, EventBus eventBus) {
    initWidget(uiBinder.createAndBindUi(this));
    this.eventBus = eventBus;
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
            containerPanel.clear();

            JSONObject jsonSchema = new JSONObject(resource);
            JsonSchemaGWT.buildUiIntoPanel(jsonSchema, containerPanel, eventBus);
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
        jsonObject.put(widgetAsSchemaUiContainer.getKey(), widgetAsSchemaUiContainer.getJSONValue());
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
        String validationError = widgetAsSchemaUiContainer.validate();
        if (validationError.length() > 0) errors.put(widgetAsSchemaUiContainer, widgetAsSchemaUiContainer.getTitle() + ": " + validationError);
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
