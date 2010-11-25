/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.view;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.WidgetProvider;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DatasourceParsingErrorTable;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class VariablesImportView extends Composite implements VariablesImportPresenter.Display {
  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  //
  // Instance Variables
  //

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep fileSelectionStep;

  @UiField
  WizardStep compareStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  SimplePanel fileSelectionPanel;

  @UiField
  HTMLPanel fileSelectionHelp;

  @UiField
  Button downloadExcelTemplateButton;

  @UiField
  Label failed;

  @UiField
  DatasourceParsingErrorTable datasourceParsingErrorTable;

  @UiField
  ListBox datasources;

  private FileSelectionPresenter.Display fileSelection;

  private WizardStepChain stepChain;

  private DatasourceCreatedCallback datasourceCreatedCallback;

  private ComparedDatasourcesReportStepPresenter.Display compareDisplay;

  private ClickHandler fileSelectedHandler;

  private ClickHandler importHandler;

  //
  // Constructors
  //

  public VariablesImportView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
    .append(fileSelectionStep, fileSelectionHelp)//
    .title("Select the variables file to use for creating or updating tables.")// TODO
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        hideErrors();
      }
    })//

    .append(compareStep)//
    .title("Review the modifications before applying them.")// TODO
    .help(new WidgetProvider() {

      @Override
      public Widget getWidget() {
        return compareDisplay.getStepHelp();
      }
    })//

    .append(conclusionStep)//
    .title("Importing variables...")// TODO

    .onNext(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(fileSelectionStep.isVisible()) {
          fileSelectedHandler.onClick(evt);
          dialog.setProgress(true);
          dialog.setNextEnabled(false);
          dialog.setCancelEnabled(false);
        } else if(compareStep.isVisible()) {
          importHandler.onClick(evt);
          stepChain.onNext();
          dialog.setPreviousEnabled(false);
          dialog.setCancelEnabled(false);
        } else
          stepChain.onNext();
      }
    })//
    .onPrevious().onFinish().onCancel().build();
  }

  private void showErrors(ClientErrorDto errorDto) {
    failed.setVisible(true);

    if(errorDto != null && errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
      datasourceParsingErrorTable.setErrors(errorDto);
      datasourceParsingErrorTable.setVisible(true);
    }
  }

  //
  // UploadVariablesStepPresenter.Display Methods
  //

  @Override
  public String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasources.addItem(datasources.get(i).getName());
    }

  }

  public HandlerRegistration addDownloadExcelTemplateClickHandler(ClickHandler handler) {
    return downloadExcelTemplateButton.addClickHandler(handler);
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
  public void setFileSelectionDisplay(Display display) {
    fileSelectionPanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
  }

  @Override
  public HandlerRegistration addFileSelectedClickHandler(ClickHandler handler) {
    this.fileSelectedHandler = handler;
    return stepChain.getNextHandlerRegistration();
  }

  @Override
  public void hideErrors() {
    failed.setVisible(false);
    datasourceParsingErrorTable.setVisible(false);
  }

  @Override
  public void setComparedDatasourcesReportDisplay(ComparedDatasourcesReportStepPresenter.Display display) {
    compareDisplay = display;
    compareStep.removeStepContent();
    compareStep.add(display.asWidget());
  }

  @Override
  public void setConclusionDisplay(ConclusionStepPresenter.Display display) {
    // conclusionDisplay = display;
    conclusionStep.removeStepContent();
    conclusionStep.add(display.asWidget());
  }

  @Override
  public DatasourceCreatedCallback getDatasourceCreatedCallback() {
    if(datasourceCreatedCallback == null) {
      datasourceCreatedCallback = new DatasourceCreatedCallback() {

        @Override
        public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
          dialog.setCancelEnabled(true);
          dialog.setProgress(false);
          stepChain.onNext();
        }

        @Override
        public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
          showErrors(error);
          dialog.setProgress(false);
          dialog.setNextEnabled(true);
          dialog.setCancelEnabled(true);
        }
      };
    }
    return datasourceCreatedCallback;
  }

  public Widget asWidget() {
    return this;
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("VariablesImportView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, VariablesImportView> {
  }

  @Override
  public HandlerRegistration addImportClickHandler(ClickHandler handler) {
    this.importHandler = handler;
    return stepChain.getNextHandlerRegistration();
  }

}
