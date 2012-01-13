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
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public abstract class WizardPresenterWidget<V extends PopupView> extends PresenterWidget<V> implements WizardRequiredEvent.Handler {

  protected WizardPresenterWidget(EventBus eventBus, V view) {
    super(eventBus, view);
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
  }

}
