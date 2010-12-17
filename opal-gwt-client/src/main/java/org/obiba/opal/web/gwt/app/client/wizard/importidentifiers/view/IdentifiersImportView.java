/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IdentifiersImportView extends Composite implements IdentifiersImportPresenter.Display {

  @UiTemplate("IdentifiersImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, IdentifiersImportView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep formatStep;

  @UiField
  Label instructions;

  @UiField
  CsvOptionsView csvOptions;

  @UiField
  WizardStep conclusionStep;

  @UiField
  SimplePanel conclusionPanel;

  private WizardStepChain stepChain;

  public IdentifiersImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWidgets() {
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//

    .append(formatStep)//
    .title(translations.identifiersImportFileStep())//

    .append(conclusionStep)//
    .conclusion()//

    .onNext().onPrevious().build();
  }

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

  @Override
  public void showDialog() {
    stepChain.reset();
    dialog.center();
    dialog.show();
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addCloseClickHandler(ClickHandler handler) {
    return dialog.addCloseClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

  @Override
  public void setCsvOptionsFileSelectorWidgetDisplay(Display display) {
    csvOptions.setCsvFileSelectorWidgetDisplay(display);
  }

  @Override
  public void renderPendingConclusion() {
    conclusionStep.setStepTitle(translations.identifierImportPendingConclusion());
    dialog.setProgress(true);
    stepChain.onNext();
    dialog.setProgress(true);
    dialog.setCloseEnabled(false);
    dialog.setCancelEnabled(false);
  }

  @Override
  public void renderCompletedConclusion() {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.identifierImportCompletedConclusion());
    dialog.setCloseEnabled(true);
    dialog.setProgress(false);
  }

  @Override
  public void renderFailedConclusion() {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.identifierImportFailedConclusion());
    dialog.setCancelEnabled(true);
    dialog.setProgress(false);
  }

  @Override
  public CsvOptionsView getCsvOptions() {
    return csvOptions;
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    csvOptions.setDefaultCharset(defaultCharset);
  }

  @Override
  public void setUnitName(String unitName) {
    if(unitName != null) {
      instructions.setText(translations.importUnitIdentifiersInstructions() + ": " + unitName);
    } else {
      instructions.setText(translations.importOpalIdentifiersInstructions());
    }
  }

}
