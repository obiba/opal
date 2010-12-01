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

import net.customware.gwt.presenter.client.BasicPresenter;
import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

/**
 *
 */
public class WizardManager {
  //
  // Instance Variables
  //

  private final EventBus eventBus;

  @Inject
  private CreateViewStepPresenter createViewWizard;

  private BasicPresenter<?> lastBoundWizard;

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
        bindAndDisplayWizard(createViewWizard);
        break;
      default:
        throw new UnsupportedOperationException("wizard type not supported (" + event.getWizardType() + ")");
      }
    }

    private void bindAndDisplayWizard(BasicPresenter<?> wizard) {
      wizard.bind();
      lastBoundWizard = wizard;
      wizard.revealDisplay();
    }

    private void unbindLastWizard() {
      if(lastBoundWizard != null) {
        GWT.log("unbinding last wizard: " + this);
        lastBoundWizard.unbind();
        lastBoundWizard = null;
      }
    }
  }
}
