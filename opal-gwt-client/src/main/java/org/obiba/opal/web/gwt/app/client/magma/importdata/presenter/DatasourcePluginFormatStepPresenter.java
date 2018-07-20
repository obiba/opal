package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;

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
    return false;
  }

  public interface Display extends View, WizardStepDisplay, HasUiHandlers<ModalUiHandlers> {

    void setDatasourcePluginName(String name);
  }
}
