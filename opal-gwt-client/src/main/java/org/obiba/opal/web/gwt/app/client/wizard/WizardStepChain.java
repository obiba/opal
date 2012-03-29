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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.WidgetProvider;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

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
    wizard.setNextEnabled(current.hasNext() && !current.isFinish());
    wizard.setPreviousEnabled(current.hasPrevious());
    wizard.setFinishEnabled(current.isFinish());
    wizard.setCloseVisible(current.isConclusion());
    Widget help = current.getHelp();
    wizard.setHelpEnabled(help != null);
    if(help != null) {
      wizard.setHelpTooltip(help);
    }
  }

  public boolean isValid() {
    return this.current.validate();
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

    private DefaultWizardStepController currentStepCtrl;

    private HandlerRegistration registration;

    private Builder(WizardDialogBox wizard) {
      chain = new WizardStepChain();
      chain.wizard = wizard;
    }

    /**
     * Create a step chain for the wizard.
     *
     * @param wizard
     * @return
     */
    public static Builder create(WizardDialogBox wizard) {
      return create(wizard, true);
    }

    /**
     * Create a step chain for the wizard, with optional glass rendering.
     *
     * @param wizard
     * @param glass
     * @return
     */
    public static Builder create(WizardDialogBox wizard, boolean glass) {
      wizard.setGlassEnabled(glass);
      return new Builder(wizard);
    }

    /**
     * Append a step, without a help widget.
     *
     * @param step
     * @return
     */
    public Builder append(WizardStep step) {
      return append(step, null);
    }

    /**
     * Append a step with its help widget.
     *
     * @param step
     * @param help
     * @return
     */
    public Builder append(WizardStep step, Widget help) {
      return append(new DefaultWizardStepController(step, help));
    }

    public Builder append(WizardStep step, Widget help, Skippable skippable) {
      return append(new DefaultWizardStepController(step, help, skippable));
    }

    public Builder append(DefaultWizardStepController stepCtrl) {
      if(currentStepCtrl != null) {
        if(currentStepCtrl.isConclusion()) throw new IllegalArgumentException("Cannot have a step after conclusion.");
        currentStepCtrl.setNext(stepCtrl);
        stepCtrl.setPrevious(currentStepCtrl);
      } else {
        chain.first = stepCtrl;
      }
      currentStepCtrl = stepCtrl;
      return this;
    }

    /**
     * Set the title of the last appended step.
     *
     * @param text
     * @return
     */
    public Builder title(String text) {
      currentStepCtrl.getStep().setStepTitle(text);
      return this;
    }

    /**
     * Set a provider of help for the last appended step.
     *
     * @param provider
     * @return
     */
    public Builder help(WidgetProvider provider) {
      currentStepCtrl.setHelpProvider(provider);
      return this;
    }

    /**
     * Set if the last appended step is a conclusion: when entering this step the navigation buttons
     * (next/previous/finish) will be hidden and close/cancel will be available.
     *
     * @return
     */
    public Builder conclusion() {
      currentStepCtrl.setConclusion(true);
      return this;
    }

    /**
     * Callback that validates the current step before switching to the next step.
     *
     * @param validator
     * @return
     */
    public Builder onValidate(ValidationHandler validator) {
      currentStepCtrl.setValidator(validator);
      return this;
    }

    public Builder onStepIn(StepInHandler handler) {
      currentStepCtrl.setStepInHandler(handler);
      return this;
    }

    /**
     * Callback to ask for the step to reset its display.
     *
     * @param handler
     * @return
     */
    public Builder onReset(ResetHandler handler) {
      currentStepCtrl.setReset(handler);
      return this;
    }

    /**
     * Set a specific handler to be called when next is clicked.
     *
     * @param handler
     * @return
     */
    public Builder onNext(ClickHandler handler) {
      registration = chain.wizard.addNextClickHandler(handler);
      chain.nextHandlerRegistration = registration;
      return this;
    }

    /**
     * Set a default handler to be called when next is clicked: current step is validated and next step is requested.
     *
     * @return
     */
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

    /**
     * Set a specific handler to be called when previous is clicked.
     *
     * @param handler
     * @return
     */
    public Builder onPrevious(ClickHandler handler) {
      registration = chain.wizard.addPreviousClickHandler(handler);
      chain.previousHandlerRegistration = registration;
      return this;
    }

    /**
     * Set a default handler to be called when previous is clicked: previous step is requested.
     *
     * @return
     */
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

    /**
     * Set a specific handler to be called when finish is clicked.
     *
     * @param handler
     * @return
     */
    public Builder onFinish(ClickHandler handler) {
      registration = chain.wizard.addFinishClickHandler(handler);
      chain.finishHandlerRegistration = registration;
      return this;
    }

    /**
     * Set a default handler to be called when finish is clicked: validate the current step and hide.
     *
     * @return
     */
    public Builder onFinish() {
      return onFinish(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          if(chain.current.validate()) chain.wizard.hide();
        }
      });
    }

    /**
     * Set a default handler to be called when close is clicked: validate the current step and hide.
     *
     * @return
     */
    public Builder onClose() {
      return onClose(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          if(chain.current.validate()) chain.wizard.hide();
        }
      });
    }

    /**
     * Set a specific handler to be called when close is clicked.
     *
     * @param handler
     * @return
     */
    public Builder onClose(ClickHandler handler) {
      registration = chain.wizard.addCloseClickHandler(handler);
      chain.closeHandlerRegistration = registration;
      return this;
    }

    /**
     * Set a specific handler to be called when cancel is clicked.
     *
     * @param handler
     * @return
     */
    public Builder onCancel(ClickHandler handler) {
      registration = chain.wizard.addCancelClickHandler(handler);
      chain.cancelHandlerRegistration = registration;
      return this;
    }

    /**
     * Set a default handler to be called when cancel is clicked: just hide.
     *
     * @return
     */
    public Builder onCancel() {
      return onCancel(new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          chain.wizard.hide();
        }
      });
    }

    /**
     * Get the last registration handler after a click handler was added.
     *
     * @return
     */
    public HandlerRegistration getRegistrationHandler() {
      return registration;
    }

    /**
     * Build the chain of steps.
     *
     * @return
     */
    public WizardStepChain build() {
      return chain;
    }

  }

}
