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
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.widgets.view.CsvOptionsView;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.app.client.wizard.importidentifiers.presenter.IdentifiersImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
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
  WizardStep fileAndformatSelectionStep;

  @UiField
  SimplePanel selectFilePanel;

  @UiField
  ListBox formatListBox;

  @UiField
  WizardStep csvFormatOptionsStep;

  @UiField
  CsvOptionsView csvOptions;

  @UiField
  WizardStep selectFileContentStep;

  @UiField
  RadioButton identifiersOnly;

  @UiField
  RadioButton identifiersPlusData;

  @UiField
  ListBox units;

  @UiField
  WizardStep conclusionStep;

  @UiField
  SimplePanel conclusionPanel;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  private ValidationHandler stepValidationHandler;

  public IdentifiersImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWidgets() {
    formatListBox.addItem(translations.csvLabel(), ImportFormat.CSV.name());
    formatListBox.addItem(translations.opalXmlLabel(), ImportFormat.XML.name());
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
    .append(fileAndformatSelectionStep)//
    .title(translations.selectFileAndDataFormatLabel())//
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return stepValidationHandler.validate();
      }
    })//

    .append(csvFormatOptionsStep)//
    .title("csv option step")//
    .append(selectFileContentStep)//
    .title("Select file content step")//

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
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return dialog.addNextClickHandler(handler);
  }

  @Override
  public HandlerRegistration addPreviousClickHandler(ClickHandler handler) {
    return dialog.addPreviousClickHandler(handler);
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
  public void setFileSelectorWidgetDisplay(Display display) {
    selectFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public HasText getSelectedFile() {
    return fileSelection.getFileText();
  }

  @Override
  public void setStepValidator(ValidationHandler handler) {
    stepValidationHandler = handler;
  }

  @Override
  public boolean isIdentifiersOnly() {
    return identifiersOnly.getValue();
  }

  @Override
  public boolean isIdentifiersPlusData() {
    return identifiersPlusData.getValue();
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
  }

  @Override
  public String getSelectedUnit() {
    return units.getItemText(units.getSelectedIndex());
  }

  @Override
  public void setCsvOptionsFileSelectorWidgetDisplay(Display display) {
    csvOptions.setCsvFileSelectorWidgetDisplay(display);
  }

  @Override
  public ImportFormat getImportFormat() {
    return ImportFormat.valueOf(formatListBox.getValue(formatListBox.getSelectedIndex()));
  }

  @Override
  public boolean isCsvFormatOptionsStep() {
    return false;
  }

  @Override
  public void setNoFormatOptions() {
    csvFormatOptionsStep.removeStepContent();
    csvFormatOptionsStep.setStepTitle(translations.noFormatOptionsStep());
  }

  @Override
  public void setCsvFormatOptions() {
    csvFormatOptionsStep.removeStepContent();
    csvFormatOptionsStep.setStepTitle(translations.csvFormatOptionsStep());
    csvFormatOptionsStep.add(csvOptions);
  }
}
