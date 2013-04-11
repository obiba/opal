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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;

public abstract class WizardPresenterWidget<V extends WizardView> extends PresenterWidget<V>
    implements WizardRequiredEvent.Handler {

  protected WizardPresenterWidget(EventBus eventBus, V view) {
    super(eventBus, view);
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
  }

  /**
   * When this method returns true, the dialog is hidden when {@code finish} is clicked.
   *
   * @return
   */
  protected boolean hideOnFinish() {
    return false;
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getView().addCancelClickHandler(new CancelClickHandler()));
    registerHandler(getView().addFinishClickHandler(new FinishClickHandler()));
    registerHandler(getView().addCloseClickHandler(new CloseClickHandler()));
  }

  /**
   * Called when the finish button is clicked.
   */
  protected void onFinish() {

  }

  /**
   * Called when the cancel button is clicked and after the wizard dialog has been closed.
   */
  protected void onCancel() {

  }

  /**
   * Called when the close button is clicked and after the wizard dialog has been closed.
   */
  protected void onClose() {

  }

  class CancelClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent e) {
      getView().hide();
      onCancel();
    }
  }

  class FinishClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent e) {
      if(hideOnFinish()) {
        getView().hide();
      }
      onFinish();
    }
  }

  class CloseClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent e) {
      if(!hideOnFinish()) {
        getView().hide();
      }
      onClose();
    }
  }
}
