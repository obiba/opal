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
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DefaultWizardStepController implements WizardStepController {

  public static class Builder {

    private final DefaultWizardStepController currentStepCtrl;

    Builder(DefaultWizardStepController ctrl) {
      this.currentStepCtrl = ctrl;
    }

    public static Builder create(WizardStep step, Widget help) {
      return new Builder(new DefaultWizardStepController(step, help));
    }

    public static Builder create(WizardStep step) {
      return create(step, null);
    }

    /**
     * Set the title of the last appended step.
     * @param text
     * @return
     */
    public Builder title(String text) {
      currentStepCtrl.getStep().setStepTitle(text);
      return this;
    }

    /**
     * Set a provider of help for the last appended step.
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
     * @return
     */
    public Builder conclusion() {
      currentStepCtrl.setConclusion(true);
      return this;
    }

    /**
     * Callback that validates the current step before switching to the next step.
     * @param validator
     * @return
     */
    public Builder onValidate(ValidationHandler validator) {
      currentStepCtrl.setValidator(validator);
      return this;
    }

    /**
     * Callback to ask for the step to reset its display.
     * @param handler
     * @return
     */
    public Builder onReset(ResetHandler handler) {
      currentStepCtrl.setReset(handler);
      return this;
    }

    /**
     * Callback to execute some code before steping into this step
     * @param handler
     * @return
     */
    public Builder onStepIn(StepInHandler handler) {
      currentStepCtrl.setStepInHandler(handler);
      return this;
    }

    /**
     * @return
     */
    public DefaultWizardStepController build() {
      return currentStepCtrl;
    }

  }

  private WizardStep step;

  private WidgetProvider help;

  private WizardStepController next;

  private WizardStepController previous;

  private StepInHandler stepInHandler;

  private ValidationHandler validator;

  private ResetHandler reset;

  private boolean finish = false;

  private boolean conclusion = false;

  DefaultWizardStepController(WizardStep step, final Widget help) {
    super();
    this.step = step;
    if(help != null) {
      this.help = new WidgetProviderImpl(help);
    }
  }

  void setNext(WizardStepController next) {
    this.next = next;
  }

  void setPrevious(WizardStepController previous) {
    this.previous = previous;
  }

  public void setStepInHandler(StepInHandler stepInHandler) {
    this.stepInHandler = stepInHandler;
  }

  void setValidator(ValidationHandler validator) {
    this.validator = validator;
  }

  void setReset(ResetHandler reset) {
    this.reset = reset;
  }

  public void setHelpProvider(WidgetProvider provider) {
    this.help = provider;
  }

  public void setFinish(boolean finish) {
    this.finish = finish;
  }

  public void setConclusion(boolean conclusion) {
    this.conclusion = conclusion;
  }

  @Override
  public Widget getHelp() {
    return help != null ? help.getWidget() : null;
  }

  @Override
  public WizardStep getStep() {
    return step;
  }

  public void onStepIn() {
    if(stepInHandler != null) {
      stepInHandler.onStepIn();
    }
  }

  @Override
  public WizardStepController onNext() {
    WizardStepController next = getNext();
    if(next == null) throw new IllegalStateException("No next step");
    if(getStep().isVisible()) {
      if(!validate()) return this;
      getStep().setVisible(false);
      next.onStepIn();
      next.getStep().setVisible(true);
      return next;
    }
    return next.onNext();
  }

  @Override
  public WizardStepController onPrevious() {
    if(previous == null) throw new IllegalStateException("No previous step");
    if(getStep().isVisible()) {
      getStep().setVisible(false);
      previous.getStep().setVisible(true);
      return previous;
    }
    return previous.onPrevious();
  }

  @Override
  public boolean hasNext() {
    return next != null;
  }

  @Override
  public boolean hasPrevious() {
    return previous != null;
  }

  @Override
  public void reset() {
    getStep().setVisible(!hasPrevious());
    if(reset != null) {
      reset.onReset();
    }
    if(next != null) {
      next.reset();
    }
  }

  @Override
  public boolean validate() {
    return validator != null ? validator.validate() : true;
  }

  @Override
  public boolean isFinish() {
    return finish || (next != null && next.isConclusion()) || (hasNext() == false);
  }

  @Override
  public boolean isConclusion() {
    return conclusion;
  }

  protected WizardStepController getNext() {
    return next;
  }

  private static class WidgetProviderImpl implements WidgetProvider {

    private Widget w;

    public WidgetProviderImpl(Widget w) {
      super();
      this.w = w;
      w.removeFromParent();
    }

    @Override
    public Widget getWidget() {
      return w;
    }

  }

}
