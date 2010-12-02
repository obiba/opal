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

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;

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
  private Provider<CreateViewStepPresenter> createViewWizard;

  @Inject
  private Provider<DataImportPresenter> dataImportWizard;

  private Wizard lastBoundWizard;

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
    eventBus.addHandler(WizardRequiredEvent.getType(), new WizardRequiredEventHandler());
  }

  //
  // Inner Classes / Interfaces
  //

  class WizardRequiredEventHandler implements WizardRequiredEvent.Handler {

    @Override
    public void onWizardRequired(WizardRequiredEvent event) {
      unbindLastWizard();

      switch(event.getWizardType()) {
      case CREATE_VIEW:
        bindAndDisplayWizard(createViewWizard.get(), event);
        break;
      case IMPORT_DATA:
        bindAndDisplayWizard(dataImportWizard.get(), event);
        break;
      default:
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
