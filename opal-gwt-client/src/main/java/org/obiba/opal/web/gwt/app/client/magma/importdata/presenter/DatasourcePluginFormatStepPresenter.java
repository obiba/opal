/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.Map;

import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.json.client.JSONObject;
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
    ImportConfig importConfig = new ImportConfig();
    importConfig.setFormat(ImportConfig.ImportFormat.FROM_PLUGIN);
    importConfig.setPluginName(getView().getSelectedPluginName());
    importConfig.setPluginImportConfig(getView().getCurrentValues());
    return importConfig;
  }

  @Override
  public boolean validate() {
    return getView().jsonSchemaValuesAreValid();
  }

  public Map<HasType<ControlGroupType>, String> getErrors() {
    return getView().getErrors();
  }

  public interface Display extends View, WizardStepDisplay, HasUiHandlers<ModalUiHandlers> {

    void setDatasourcePluginName(String name);

    boolean jsonSchemaValuesAreValid();

    JSONObject getCurrentValues();

    Map<HasType<ControlGroupType>, String> getErrors();

    String getSelectedPluginName();
  }
}
