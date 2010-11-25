/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.WidgetProvider;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class DataImportView extends Composite implements DataImportPresenter.Display {

  @UiTemplate("DataImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DataImportView> {
  }

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep formatSelectionStep;

  @UiField
  WizardStep formatStep;

  @UiField
  WizardStep destinationSelectionStep;

  @UiField
  WizardStep identityArchiveStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  HTMLPanel formatSelectionHelp;

  @UiField
  HTMLPanel destinationSelectionHelp;

  @UiField
  ListBox formatListBox;

  private DataImportPresenter.DataImportStepDisplay formatStepDisplay;

  private DataImportPresenter.DataImportStepDisplay identityArchiveStepDisplay;

  private WizardStepChain stepChain;

  private ValidationHandler formatStepValidator;

  public DataImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
    .append(formatSelectionStep, formatSelectionHelp)//
    .title("Select the type of data you wish to import.")// TODO

    .append(formatStep)//
    .help(new WidgetProvider() {

      @Override
      public Widget getWidget() {
        return formatStepDisplay.getStepHelp();
      }
    }) //
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return formatStepValidator.validate();
      }
    }).title("Select the file to be imported.")// TODO

    .append(identityArchiveStep)//
    .title("Provide the following additional information.")// TODO
    .help(new WidgetProvider() {

      @Override
      public Widget getWidget() {
        return identityArchiveStepDisplay.getStepHelp();
      }
    }) //

    .append(destinationSelectionStep, destinationSelectionHelp)//
    .title("Select the destination (where you wish to write the data to).")// TODO

    .append(conclusionStep)//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        conclusionStep.setStepTitle("Data to import are being validated..."); // TODO
      }
    })//
    .onPrevious().build();

  }

  private void initWidgets() {
    formatListBox.addItem(translations.csvLabel(), ImportFormat.CSV.name());
    formatListBox.addItem(translations.opalXmlLabel(), ImportFormat.XML.name());
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
  public HandlerRegistration addNextClickHandler(ClickHandler handler) {
    return null;
  }

  @Override
  public ImportFormat getImportFormat() {
    String formatString = formatListBox.getValue(formatListBox.getSelectedIndex());
    return ImportFormat.valueOf(formatString);
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
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void showDialog() {
    stepChain.reset();
    dialog.center();
    dialog.show();
  }

  @Override
  public HandlerRegistration addFormatChangeHandler(ChangeHandler handler) {
    return formatListBox.addChangeHandler(handler);
  }

  @Override
  public void setFormatStepDisplay(DataImportPresenter.DataImportStepDisplay display) {
    this.formatStepDisplay = display;
    formatStep.removeStepContent();
    formatStep.add(display.asWidget());
  }

  @Override
  public void setFormatStepValidator(ValidationHandler handler) {
    this.formatStepValidator = handler;
  }

  @Override
  public void setIdentityArchiveStepDisplay(DataImportPresenter.DataImportStepDisplay display) {
    this.identityArchiveStepDisplay = display;
    identityArchiveStep.removeStepContent();
    identityArchiveStep.add(display.asWidget());
  }

  @Override
  public void setDestinationSelectionDisplay(DataImportPresenter.DataImportStepDisplay display) {
    destinationSelectionStep.removeStepContent();
    destinationSelectionStep.add(display.asWidget());
  }

  @Override
  public HandlerRegistration addImportClickHandler(final ClickHandler handler) {
    return dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(destinationSelectionStep.isVisible()) {
          // asynchronous next, see setConclusion()
          handler.onClick(evt);
        } else
          stepChain.onNext();
      }
    });
  }

  @Override
  public void renderConclusion(ConclusionStepPresenter presenter) {
    dialog.setProgress(true);
    conclusionStep.removeStepContent();
    presenter.reset();
    conclusionStep.add(presenter.getDisplay().asWidget());
    stepChain.onNext();
    dialog.setPreviousEnabled(false);
    dialog.setCancelEnabled(false);
    dialog.setFinishEnabled(false);
    presenter.setTransientDatasourceCreatedCallback(new DatasourceCreatedCallback() {

      @Override
      public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
        conclusionStep.setStepTitle("Data import validation completed.");
        dialog.setFinishEnabled(true);
        dialog.setProgress(false);
      }

      @Override
      public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
        conclusionStep.setStepTitle("Data import validation failed.");
        dialog.setCancelEnabled(true);
        dialog.setPreviousEnabled(true);
        dialog.setProgress(false);
      }
    });

  }

}
