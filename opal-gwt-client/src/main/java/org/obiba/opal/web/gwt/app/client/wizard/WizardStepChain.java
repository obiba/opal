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

import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class WizardStepChain {

  private WizardDialogBox wizard;

  private WizardStepController first;

  private WizardStepController current;

  private WizardStepChain() {
  }

  public WizardStepController onNext() {
    current = current.onNext();
    apply();
    return current;
  }

  public WizardStepController onPrevious() {
    current = current.onPrevious();
    apply();
    return current;
  }

  public void reset() {
    current = first;
    // forward reset request
    current.reset();
    apply();
    wizard.setCancelEnabled(true);
  }

  private void apply() {
    wizard.setNextEnabled(current.hasNext());
    wizard.setPreviousEnabled(current.hasPrevious());
    wizard.setFinishEnabled(current.canFinish() || !current.hasNext());
    Widget help = current.getHelp();
    wizard.setHelpEnabled(help != null);
    if(help != null) {
      wizard.setHelpTooltip(help);
    }
  }

  public static class Builder {

    private WizardStepChain chain;

    private WizardStepControllerImpl currentStepCtrl;

    private HandlerRegistration registration;

    private Builder(WizardDialogBox wizard) {
      chain = new WizardStepChain();
      chain.wizard = wizard;
    }

    public static Builder create(WizardDialogBox wizard) {
      return create(wizard, true);
    }

    public static Builder create(WizardDialogBox wizard, boolean glass) {
      wizard.setGlassEnabled(glass);
      return new Builder(wizard);
    }

    public Builder append(WizardStep step, Widget help) {
      WizardStepControllerImpl stepCtrl = new WizardStepControllerImpl(step, help);
      if(currentStepCtrl != null) {
        currentStepCtrl.setNext(stepCtrl);
        stepCtrl.setPrevious(currentStepCtrl);
      } else {
        chain.first = stepCtrl;
      }
      currentStepCtrl = stepCtrl;
      return this;
    }

    public Builder append(WizardStep step) {
      return append(step, null);
    }

    public Builder title(String text) {
      currentStepCtrl.getStep().setStepTitle(text);
      return this;
    }

    public Builder canFinish() {
      currentStepCtrl.setCanFinish(true);
      return this;
    }

    public Builder onValidate(ValidationHandler validator) {
      currentStepCtrl.setValidator(validator);
      return this;
    }

    public Builder onReset(WizardStepResetHandler handler) {
      currentStepCtrl.setReset(handler);
      return this;
    }

    public Builder onNext(ClickHandler handler) {
      registration = chain.wizard.addNextClickHandler(handler);
      return this;
    }

    public Builder onNext() {
      return onNext(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          chain.wizard.setProgress(true);
          chain.onNext();
          chain.wizard.setProgress(false);
        }
      });
    }

    public Builder onPrevious(ClickHandler handler) {
      registration = chain.wizard.addPreviousClickHandler(handler);
      return this;
    }

    public Builder onPrevious() {
      return onPrevious(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          chain.wizard.setProgress(true);
          chain.onPrevious();
          chain.wizard.setProgress(false);
        }
      });
    }

    public Builder onFinish(ClickHandler handler) {
      registration = chain.wizard.addFinishClickHandler(handler);
      return this;
    }

    public Builder onFinish() {
      return onFinish(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          if(chain.current.validate()) chain.wizard.hide();
        }
      });
    }

    public Builder onCancel(ClickHandler handler) {
      registration = chain.wizard.addCancelClickHandler(handler);
      return this;
    }

    public Builder onCancel() {
      return onCancel(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          chain.wizard.hide();
        }
      });
    }

    public HandlerRegistration getRegistrationHandler() {
      return registration;
    }

    public WizardStepChain build() {
      return chain;
    }

  }

}
