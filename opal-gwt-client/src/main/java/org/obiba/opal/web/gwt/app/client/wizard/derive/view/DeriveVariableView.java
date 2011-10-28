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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveVariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DeriveVariableView extends Composite implements DeriveVariablePresenter.Display {

  @UiTemplate("DeriveVariableView.ui.xml")
  interface ViewUiBinder extends UiBinder<WizardDialogBox, DeriveVariableView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep categoricalStep;

  @UiField
  WizardStep summaryStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  Panel summary;

  @UiField
  HorizontalTabLayout tabs;

  @UiField
  Panel values;

  private ValidationHandler derivedValidator;

  private final WizardDialogBox dialog;

  private WizardStepChain stepChain;

  public DeriveVariableView() {
    this.dialog = uiBinder.createAndBindUi(this);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

    .append(categoricalStep)//
    .title("Categorical")//
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return derivedValidator.validate();
      }
    }) //

    .append(summaryStep)//
    .title("Summary")//

    .append(conclusionStep)//
    .title("Conclusion")//

    .onNext().onPrevious().build();
  }

  @Override
  public void showDialog() {
    if(stepChain == null) {
      initWizardDialog();
    }

    stepChain.reset();
    clear();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void clear() {
    categoricalStep.setVisible(true);
    summaryStep.setVisible(false);
    conclusionStep.setVisible(false);
  }

  @Override
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.add(widget.asWidget());
  }

  @Override
  public void setDerivedVariableValidator(ValidationHandler validator) {
    derivedValidator = validator;
  }

  //
  // Widget Display methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

}
