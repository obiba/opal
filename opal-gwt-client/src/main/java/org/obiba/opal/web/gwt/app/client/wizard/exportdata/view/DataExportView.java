/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.exportdata.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends Composite implements DataExportPresenter.Display {

  private static final Translations translations = GWT.create(Translations.class);

  @UiTemplate("DataExportView.ui.xml")
  interface DataExportUiBinder extends UiBinder<DialogBox, DataExportView> {
  }

  private static DataExportUiBinder uiBinder = GWT.create(DataExportUiBinder.class);

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep tablesStep;

  @UiField
  WizardStep destinationStep;

  @UiField
  WizardStep optionsStep;

  @UiField
  WizardStep unitStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  ListBox datasources;

  @UiField
  ListBox units;

  @UiField
  Anchor jobLink;

  @UiField
  SimplePanel tablesPanel;

  @UiField
  SimplePanel filePanel;

  @UiField
  ListBox fileFormat;

  @UiField
  RadioButton destinationDataSource;

  @UiField
  RadioButton destinationFile;

  @UiField
  CheckBox incremental;

  @UiField
  CheckBox withVariables;

  @UiField
  CheckBox useAlias;

  @UiField
  RadioButton opalId;

  @UiField
  RadioButton unitId;

  private FileSelectionPresenter.Display fileSelection;

  public DataExportView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
    initTablesStep();
    initDestinationStep();
    initOptionsStep();
    initUnitStep();
    initConclusionStep();
  }

  private void initWizardDialog() {
    dialog.hide();
    clear();
    dialog.addPreviousClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        if(conclusionStep.isVisible()) {
          conclusionStep.setVisible(false);
          unitStep.setVisible(true);
          dialog.setNextEnabled(true);
        } else if(unitStep.isVisible()) {
          unitStep.setVisible(false);
          destinationStep.setVisible(true);
        } else if(destinationStep.isVisible()) {
          destinationStep.setVisible(false);
          optionsStep.setVisible(true);
        } else if(optionsStep.isVisible()) {
          optionsStep.setVisible(false);
          tablesStep.setVisible(true);
          dialog.setPreviousEnabled(false);
        }
      }
    });
  }

  private void initTablesStep() {
    tablesStep.setVisible(true);
    tablesStep.setStepTitle(translations.dataExportInstructions());
  }

  private void initOptionsStep() {
    optionsStep.setVisible(false);
    optionsStep.setStepTitle(translations.dataExportOptions());
  }

  private void initDestinationStep() {
    destinationStep.setVisible(false);
    destinationStep.setStepTitle(translations.dataExportDestination());
  }

  private void initUnitStep() {
    unitStep.setVisible(false);
    unitStep.setStepTitle(translations.dataExportUnit());
  }

  private void initConclusionStep() {
    conclusionStep.setVisible(false);
    conclusionStep.setStepTitle(translations.dataExportPendingConclusion());
  }

  private void initWidgets() {
    destinationDataSource.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        datasources.setEnabled(true);
        fileSelection.setEnabled(false);
        fileFormat.setEnabled(false);
      }
    });
    destinationFile.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        datasources.setEnabled(false);
        fileSelection.setEnabled(true);
        fileFormat.setEnabled(true);
      }
    });

    destinationFile.setValue(true);
    datasources.setEnabled(false);

    opalId.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        units.setEnabled(false);
      }
    });
    unitId.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        units.setEnabled(true);
      }
    });
    opalId.setValue(true);
    units.setEnabled(false);
    incremental.setValue(true);
    withVariables.setValue(true);
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

  //
  // DataExport Display
  // 

  @Override
  public String getSelectedDatasource() {
    return this.datasources.getValue(this.datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(JsArray<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.length(); i++) {
      this.datasources.addItem(datasources.get(i).getName(), datasources.get(i).getName());
    }
  }

  @Override
  public String getSelectedUnit() {
    return this.units.getValue(this.units.getSelectedIndex());
  }

  @Override
  public void setUnits(JsArray<FunctionalUnitDto> units) {
    this.units.clear();
    for(int i = 0; i < units.length(); i++) {
      this.units.addItem(units.get(i).getName());
    }
  }

  @Override
  public HandlerRegistration addSubmitClickHandler(final ClickHandler submitHandler) {
    return dialog.addNextClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        if(tablesStep.isVisible()) {
          tablesStep.setVisible(false);
          optionsStep.setVisible(true);
          dialog.setPreviousEnabled(true);
        } else if(optionsStep.isVisible()) {
          optionsStep.setVisible(false);
          destinationStep.setVisible(true);
        } else if(destinationStep.isVisible()) {
          destinationStep.setVisible(false);
          unitStep.setVisible(true);
        } else if(unitStep.isVisible()) {
          submitHandler.onClick(evt);
        }
      }
    });
  }

  @Override
  public HandlerRegistration addJobLinkClickHandler(ClickHandler handler) {
    return jobLink.addClickHandler(handler);
  }

  @Override
  public boolean isDestinationFile() {
    return destinationFile.getValue();
  }

  @Override
  public HandlerRegistration addDestinationFileClickHandler(ClickHandler handler) {
    return destinationFile.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addDestinationDatasourceClickHandler(ClickHandler handler) {
    return destinationDataSource.addClickHandler(handler);
  }

  @Override
  public HandlerRegistration addWithVariablesClickHandler(ClickHandler handler) {
    return withVariables.addClickHandler(handler);
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public boolean isUseAlias() {
    return useAlias.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return withVariables.getValue();
  }

  @Override
  public boolean isUnitId() {
    return unitId.getValue();
  }

  @Override
  public boolean isDestinationDataSource() {
    return destinationDataSource.getValue();
  }

  @Override
  public String getOutFile() {
    return fileSelection.getFile();
  }

  @Override
  public String getFileFormat() {
    return fileFormat.getValue(fileFormat.getSelectedIndex());
  }

  @Override
  public void setTableWidgetDisplay(TableListPresenter.Display display) {
    display.setListWidth("28em");
    tablesPanel.setWidget(display.asWidget());
  }

  @Override
  public void setFileWidgetDisplay(FileSelectionPresenter.Display display) {
    filePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setEnabled(true);
    fileSelection.setFieldWidth("20em");
    fileFormat.setEnabled(true);
  }

  @Override
  public HandlerRegistration addFileFormatChangeHandler(ChangeHandler handler) {
    return fileFormat.addChangeHandler(handler);
  }

  @Override
  public void renderPendingConclusion() {
    // TODO Auto-generated method stub
    conclusionStep.setStepTitle(translations.dataExportPendingConclusion());
    unitStep.setVisible(false);
    conclusionStep.setVisible(true);
    dialog.setCancelEnabled(false);
    dialog.setPreviousEnabled(false);
    dialog.setNextEnabled(false);
    jobLink.setText("");
  }

  @Override
  public void renderCompletedConclusion(String jobId) {
    conclusionStep.setStepTitle(translations.dataExportCompletedConclusion());
    // TODO
    jobLink.setText(translations.jobLabel() + " #" + jobId);
    dialog.setFinishEnabled(true);
  }

  @Override
  public void renderFailedConclusion() {
    conclusionStep.setStepTitle(translations.dataExportFailedConclusion());
    // TODO
    dialog.setCancelEnabled(true);
    dialog.setPreviousEnabled(true);
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void showDialog() {
    clear();
    dialog.center();
    dialog.show();
  }

  private void clear() {
    tablesStep.setVisible(true);
    optionsStep.setVisible(false);
    destinationStep.setVisible(false);
    unitStep.setVisible(false);
    conclusionStep.setVisible(false);
    dialog.setPreviousEnabled(false);
    dialog.setNextEnabled(true);
    dialog.setFinishEnabled(false);
    dialog.setCancelEnabled(true);
  }

  @Override
  public HandlerRegistration addCancelClickHandler(ClickHandler handler) {
    return dialog.addCancelClickHandler(handler);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(ClickHandler handler) {
    return dialog.addFinishClickHandler(handler);
  }

}
