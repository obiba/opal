/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.view;

import org.obiba.opal.web.gwt.app.client.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends DataCommonView implements DataExportPresenter.Display {

  @UiTemplate("DataExportView.ui.xml")
  interface DataExportUiBinder extends UiBinder<Widget, DataExportView> {
  }

  private static DataExportUiBinder uiBinder = GWT.create(DataExportUiBinder.class);

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
    initWidgets();
  }

  @Override
  protected void initWidgets() {
    super.initWidgets();
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
  public void renderConclusionStep(String jobId) {
    super.renderConclusionStep(jobId);
    instructionsLabel.setText(translations.dataExportInstructionsConclusion());
  }

  @Override
  public void renderFormStep() {
    super.renderFormStep();
    instructionsLabel.setText(translations.dataExportInstructions());
    setSubmitEnabled(false);
  }

}
