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
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataExportView extends PopupViewImpl implements DataExportPresenter.Display {

  private static final Translations translations = GWT.create(Translations.class);

  @UiTemplate("DataExportView.ui.xml")
  interface DataExportUiBinder extends UiBinder<DialogBox, DataExportView> {
  }

  private static DataExportUiBinder uiBinder = GWT.create(DataExportUiBinder.class);

  private final Widget widget;

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep tablesStep;

  @UiField
  WizardStep destinationStep;

  @UiField
  WizardStep unitStep;

  @UiField
  ListBox units;

  @UiField
  SimplePanel tablesPanel;

  @UiField
  SimplePanel filePanel;

  @UiField
  ListBox fileFormat;

  @UiField
  RadioButton opalId;

  @UiField
  RadioButton unitId;

  @UiField
  HTMLPanel destinationHelpPanel;

  @UiField
  HTMLPanel unitHelpPanel;

  @UiField
  CheckBox useAlias;

  @UiField
  Panel unitSelection;

  @UiField
  Label noUnitSelection;

  @UiField
  Label noUnitLabel;

  private FileSelectionPresenter.Display fileSelection;

  private TableListPresenter.Display tablesList;

  private ValidationHandler tablesValidator;

  private ValidationHandler destinationValidator;

  private WizardStepChain stepChain;

  @Inject
  public DataExportView(EventBus eventBus) {
    super(eventBus);
    this.widget = uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
    .append(tablesStep)//
    .title(translations.dataExportInstructions())//
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return tablesValidator.validate();
      }
    })//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        clearTablesStep();
      }
    })//
    .append(destinationStep, destinationHelpPanel)//
    .title(translations.dataExportDestination())//
    .onValidate(new ValidationHandler() {

      @Override
      public boolean validate() {
        return destinationValidator.validate();
      }
    })//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        clearDestinationStep();
      }
    })//
    .append(unitStep, unitHelpPanel)//
    .title(translations.dataExportUnit())//
    .onReset(new ResetHandler() {

      @Override
      public void onReset() {
        clearUnitStep();
      }
    })//

    .onNext().onPrevious().build();
  }

  private void initWidgets() {
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
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected PopupPanel asPopupPanel() {
    return dialog;
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
    noUnitLabel.setVisible(units.length() == 0);
    unitSelection.setVisible(units.length() > 0);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler submitHandler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        submitHandler.onClick(evt);
      }
    });
  }

  @Override
  public boolean isIncremental() {
    return false;
  }

  @Override
  public boolean isUseAlias() {
    return useAlias.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return true;
  }

  @Override
  public boolean isUnitId() {
    return unitId.getValue();
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
    tablesList = display;
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

  public HandlerRegistration addFileFormatChangeHandler(ChangeHandler handler) {
    return fileFormat.addChangeHandler(handler);
  }

  @Override
  public void show() {
    stepChain.reset();
    super.show();
  }

  private void clearTablesStep() {
    tablesStep.setVisible(true);
    dialog.setHelpEnabled(false);
    if(tablesList != null) tablesList.clear();
  }

  private void clearDestinationStep() {
    fileFormat.setEnabled(true);
    if(fileSelection != null) {
      fileSelection.setEnabled(true);
      fileSelection.clearFile();
    }
  }

  private void clearUnitStep() {
    opalId.setValue(true);
    unitId.setValue(false);
    units.setEnabled(false);
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
  public void setTablesValidator(ValidationHandler handler) {
    this.tablesValidator = handler;
  }

  @Override
  public void setDestinationValidator(ValidationHandler handler) {
    this.destinationValidator = handler;
  }

  @Override
  public void renderUnitSelection(boolean identifierEntityTable) {
    noUnitSelection.setVisible(!identifierEntityTable);
    unitSelection.setVisible(identifierEntityTable && units.getItemCount() > 0);
    noUnitLabel.setVisible(identifierEntityTable && units.getItemCount() == 0);
  }

}
