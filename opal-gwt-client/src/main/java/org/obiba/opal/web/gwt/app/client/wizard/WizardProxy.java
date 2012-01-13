/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard;

import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public abstract class WizardProxy<W extends WizardPresenterWidget<?>> {

  protected WizardProxy(final EventBus eventBus, WizardType type, final Provider<W> wizardProvider) {
    eventBus.addHandler(type, new WizardRequiredEvent.Handler() {

      @Override
      public void onWizardRequired(WizardRequiredEvent event) {
        W w = wizardProvider.get();
        w.onWizardRequired(event);
        RevealRootPopupContentEvent.fire(eventBus, w);
      }
    });
  }
}
