/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.wizard.BranchingWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 *
 */
public class DeriveVariableView extends PopupViewImpl implements DeriveVariablePresenter.Display {

  @UiTemplate("DeriveVariableView.ui.xml")
  interface ViewUiBinder extends UiBinder<WizardDialogBox, DeriveVariableView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField FlowPanel stepsFlow;

  @UiField WizardStep scriptEvaluationStep;

  private final WizardDialogBox dialog;

  private WizardStepChain stepChain;

  private DefaultWizardStepController.Builder stepControllerBuilder;

  @Inject
  public DeriveVariableView(EventBus eventBus) {
    super(eventBus);
    dialog = uiBinder.createAndBindUi(this);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public void setStartStep(DefaultWizardStepController.Builder stepControllerBuilder) {
    this.stepControllerBuilder = stepControllerBuilder;
    DefaultWizardStepController stepController = stepControllerBuilder.build();
    stepController.addSteps(stepsFlow);
  }

  @Override
  public void addBranchStep(DefaultWizardStepController stepController,
      BranchingWizardStepController.Condition condition) {
    ((BranchingWizardStepController.Builder) stepControllerBuilder).branch(stepController, condition);
    stepController.addSteps(stepsFlow);
  }

  @Override
  public DefaultWizardStepController.Builder getScriptEvaluationStepBuilder(
      WizardStepController.StepInHandler handler) {
    return DefaultWizardStepController.Builder.create(scriptEvaluationStep) //
        .title(translations.derivedVariableEvaluation()) //
        .onStepIn(handler);
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    if(slot == DeriveVariablePresenter.Display.Slots.Summary) {
      scriptEvaluationStep.add(content);
    }
  }

  @Override
  public void show() {
    if(stepChain == null) {
      stepChain = WizardStepChain.Builder.create(dialog).append(stepControllerBuilder.build()).onNext().onPrevious()
          .build();
    }
    stepChain.reset();
    super.show();
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  public void setScriptEvaluationSuccess(boolean success, boolean hasNextStep) {
    dialog.setNextEnabled(hasNextStep && success);
    dialog.setFinishEnabled(!hasNextStep && success);
  }

}
