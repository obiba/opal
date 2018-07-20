package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DatasourcePluginFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.support.jsonschema.JsonSchemaGWT;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class DatasourcePluginFormatStepView extends ViewImpl implements DatasourcePluginFormatStepPresenter.Display {

  @UiField
  FlowPanel containerPanel;

  @Inject
  public DatasourcePluginFormatStepView(Binder uiBinder) {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void setUiHandlers(ModalUiHandlers uiHandlers) {

  }

  @Override
  public void setDatasourcePluginName(String name) {

    ResourceRequestBuilderFactory.<JavaScriptObject>newBuilder()
        .forResource(UriBuilders.DS_PLUGIN_SERVICE.create().build(name))
        .withCallback(new ResourceCallback<JavaScriptObject>() {

          @Override
          public void onResource(Response response, JavaScriptObject resource) {
            GWT.log(resource.toString());
            JsonSchemaGWT.createUI(new JSONObject(resource), containerPanel);
          }
        })
        .get().send();
  }

  interface Binder extends UiBinder<Widget, DatasourcePluginFormatStepView> {}
}
