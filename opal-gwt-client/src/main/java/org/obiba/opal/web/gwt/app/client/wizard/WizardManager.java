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

import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.CreateDatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 *
 */
public class WizardManager {
  //
  // Instance Variables
  //

  private final EventBus eventBus;

  @Inject
  private Provider<CreateDatasourcePresenter> createDatasourceWizardProvider;

  @Inject
  private Provider<CreateViewStepPresenter> createViewWizardProvider;

  @Inject
  private Provider<VariablesImportPresenter> variablesImportWizardProvider;

  @Inject
  private Provider<DataImportPresenter> dataImportWizardProvider;

  @Inject
  private Provider<DataExportPresenter> dataExportWizardProvider;

  private Map<WizardType, Provider<? extends Wizard>> wizardProviders;

  private Wizard lastBoundWizard;

  private HandlerRegistration handlerRegistration;

  //
  // Constructors
  //

  @Inject
  public WizardManager(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  //
  // Methods
  //

  public void bind() {
    wizardProviders = new HashMap<WizardType, Provider<? extends Wizard>>();
    wizardProviders.put(WizardType.CREATE_DATASOURCE, createDatasourceWizardProvider);
    wizardProviders.put(WizardType.CREATE_VIEW, createViewWizardProvider);
    wizardProviders.put(WizardType.IMPORT_VARIABLES, variablesImportWizardProvider);
    wizardProviders.put(WizardType.IMPORT_DATA, dataImportWizardProvider);
    wizardProviders.put(WizardType.EXPORT_DATA, dataExportWizardProvider);

    handlerRegistration = eventBus.addHandler(WizardRequiredEvent.getType(), new WizardRequiredEventHandler());
  }

  public void unbind() {
    // TODO: This method is currently never being called (maybe it is not needed at all?). Note that
    // WizardManager is a singleton whose bind() method is called by GwtApp.
    if(handlerRegistration != null) {
      handlerRegistration.removeHandler();
    }
  }

  //
  // Inner Classes / Interfaces
  //

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
