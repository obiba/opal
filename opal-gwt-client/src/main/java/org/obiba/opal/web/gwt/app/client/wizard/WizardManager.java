/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class WizardManager {

  private final Map<WizardType, Provider<? extends Wizard>> wizardProviders;

  private Wizard lastBoundWizard;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public WizardManager(EventBus eventBus, Provider<CreateViewStepPresenter> createViewWizardProvider, Provider<VariablesImportPresenter> variablesImportWizardProvider, Provider<DataImportPresenter> dataImportWizardProvider, Provider<DataExportPresenter> dataExportWizardProvider, Provider<IdentifiersImportPresenter> identifiersImportWizardProvider, Provider<IdentifiersMapPresenter> identifiersMapWizardProvider, Provider<DataCopyPresenter> dataCopyWizardProvider, Provider<DeriveVariablePresenter> deriveVariableWizardProvider) {
    wizardProviders = new HashMap<WizardType, Provider<? extends Wizard>>();
    wizardProviders.put(WizardType.CREATE_VIEW, createViewWizardProvider);
    wizardProviders.put(WizardType.IMPORT_VARIABLES, variablesImportWizardProvider);
    wizardProviders.put(WizardType.IMPORT_DATA, dataImportWizardProvider);
    wizardProviders.put(WizardType.EXPORT_DATA, dataExportWizardProvider);
    wizardProviders.put(WizardType.IMPORT_IDENTIFIERS, identifiersImportWizardProvider);
    wizardProviders.put(WizardType.MAP_IDENTIFIERS, identifiersMapWizardProvider);
    wizardProviders.put(WizardType.COPY_DATA, dataCopyWizardProvider);
    wizardProviders.put(WizardType.DERIVE_CATEGORIZE_VARIABLE, deriveVariableWizardProvider);
    wizardProviders.put(WizardType.DERIVE_CUSTOM_VARIABLE, deriveVariableWizardProvider);
    eventBus.addHandler(WizardRequiredEvent.getType(), new WizardRequiredEventHandler());
  }

  class WizardRequiredEventHandler implements WizardRequiredEvent.Handler {

    @Override
    public void onWizardRequired(WizardRequiredEvent event) {
      unbindLastWizard();

      Provider<? extends Wizard> wizardProvider = wizardProviders.get(event.getWizardType());
      if(wizardProvider != null) {
        bindAndDisplayWizard(wizardProvider.get(), event);
      } else {
        throw new UnsupportedOperationException("wizard type not supported (" + event.getWizardType() + ")");
      }
    }

    private void bindAndDisplayWizard(Wizard wizard, WizardRequiredEvent event) {
      wizard.onWizardRequired(event);
      wizard.bind();
      lastBoundWizard = wizard;
      wizard.revealDisplay();
    }

    private void unbindLastWizard() {
      if(lastBoundWizard != null) {
        lastBoundWizard.unbind();
        lastBoundWizard = null;
      }
    }
  }
}
