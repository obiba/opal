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
class WizardStepControllerImpl implements WizardStepController {

  private WizardStep step;

  private WidgetProvider help;

  private WizardStepController next;

  private WizardStepController previous;

  private ValidationHandler validator;

  private ResetHandler reset;

  private boolean canFinish = false;

  WizardStepControllerImpl(WizardStep step, final Widget help) {
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

  void setValidator(ValidationHandler validator) {
    this.validator = validator;
  }

  void setReset(ResetHandler reset) {
    this.reset = reset;
  }

  public void setHelpProvider(WidgetProvider provider) {
    this.help = provider;
  }

  public void setCanFinish(boolean canFinish) {
    this.canFinish = canFinish;
  }

  @Override
  public Widget getHelp() {
    return help != null ? help.getWidget() : null;
  }

  @Override
  public WizardStep getStep() {
    return step;
  }

  @Override
  public WizardStepController onNext() {
    if(next == null) throw new IllegalStateException("No next step");
    if(getStep().isVisible()) {
      if(!validate()) return this;
      getStep().setVisible(false);
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
  public boolean canFinish() {
    return canFinish;
  }

  private class WidgetProviderImpl implements WidgetProvider {

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
