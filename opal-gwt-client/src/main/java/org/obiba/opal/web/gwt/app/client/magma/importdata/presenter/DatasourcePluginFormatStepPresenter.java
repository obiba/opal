package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.HashMap;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class DatasourcePluginFormatStepPresenter extends PresenterWidget<DatasourcePluginFormatStepPresenter.Display>
    implements DataImportPresenter.DataConfigFormatStepPresenter {

  @Inject
  public DatasourcePluginFormatStepPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
  }

  @Override
  public ImportConfig getImportConfig() {
    return null;
  }

  @Override
  public boolean validate() {
    return getView().jsonSchemaValuesAreValid();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return new HashMap<>();
  }

  public interface Display extends View, WizardStepDisplay, HasUiHandlers<ModalUiHandlers> {

    void setDatasourcePluginName(String name);

    boolean jsonSchemaValuesAreValid();

    Map<String, Object> getCurrentValues();
  }
}
