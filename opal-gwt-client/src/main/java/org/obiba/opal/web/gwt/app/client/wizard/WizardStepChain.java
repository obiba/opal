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
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.WidgetProvider;
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

  private HandlerRegistration nextHandlerRegistration;

  private HandlerRegistration previousHandlerRegistration;

  private HandlerRegistration cancelHandlerRegistration;

  private HandlerRegistration closeHandlerRegistration;

  private HandlerRegistration finishHandlerRegistration;

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
    wizard.setCancelEnabled(true);
    wizard.setProgress(false);
    wizard.setCloseVisible(false);
    current = first;
    // forward reset request
    current.reset();
    apply();
  }

  private void apply() {
    wizard.setNextEnabled(current.hasNext() && !current.canFinish());
    wizard.setPreviousEnabled(current.hasPrevious());
    wizard.setFinishEnabled(current.canFinish());
    wizard.setCloseVisible(!current.hasNext());
    Widget help = current.getHelp();
    wizard.setHelpEnabled(help != null);
    if(help != null) {
      wizard.setHelpTooltip(help);
    }
  }

  public HandlerRegistration getNextHandlerRegistration() {
    return nextHandlerRegistration;
  }

  public HandlerRegistration getPreviousHandlerRegistration() {
    return previousHandlerRegistration;
  }

  public HandlerRegistration getFinishHandlerRegistration() {
    return finishHandlerRegistration;
  }

  public HandlerRegistration getCancelHandlerRegistration() {
    return cancelHandlerRegistration;
  }

  public HandlerRegistration getCloseHandlerRegistration() {
    return closeHandlerRegistration;
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

    public Builder help(WidgetProvider provider) {
      currentStepCtrl.setHelpProvider(provider);
      return this;
    }

    public Builder onValidate(ValidationHandler validator) {
      currentStepCtrl.setValidator(validator);
      return this;
    }

    public Builder onReset(ResetHandler handler) {
      currentStepCtrl.setReset(handler);
      return this;
    }

    public Builder onNext(ClickHandler handler) {
      registration = chain.wizard.addNextClickHandler(handler);
      chain.nextHandlerRegistration = registration;
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
      chain.previousHandlerRegistration = registration;
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
      chain.finishHandlerRegistration = registration;
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

    public Builder onClose() {
      return onClose(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          if(chain.current.validate()) chain.wizard.hide();
        }
      });
    }

    public Builder onClose(ClickHandler handler) {
      registration = chain.wizard.addCloseClickHandler(handler);
      chain.closeHandlerRegistration = registration;
      return this;
    }

    public Builder onCancel(ClickHandler handler) {
      registration = chain.wizard.addCancelClickHandler(handler);
      chain.cancelHandlerRegistration = registration;
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
