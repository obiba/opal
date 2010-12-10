/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.copydata.view;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataCopyView extends Composite implements DataCopyPresenter.Display {

  private static final Translations translations = GWT.create(Translations.class);

  @UiTemplate("DataCopyView.ui.xml")
  interface DataCopyUiBinder extends UiBinder<DialogBox, DataCopyView> {
  }

  private static DataCopyUiBinder uiBinder = GWT.create(DataCopyUiBinder.class);

  @UiField
  WizardDialogBox dialog;

  @UiField
  WizardStep tablesStep;

  @UiField
  WizardStep destinationStep;

  @UiField
  WizardStep conclusionStep;

  @UiField
  Anchor jobLink;

  @UiField
  SimplePanel tablesPanel;

  @UiField
  ListBox datasources;

  @UiField
  CheckBox incremental;

  @UiField
  HTMLPanel destinationHelpPanel;

  private TableListPresenter.Display tablesList;

  private ValidationHandler tablesValidator;

  private ValidationHandler destinationValidator;

  private WizardStepChain stepChain;

  public DataCopyView() {
    initWidget(uiBinder.createAndBindUi(this));
    uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
    .append(tablesStep)//
    .title(translations.dataCopyInstructions())//
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
    .title(translations.dataCopyDestination())//
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
    .append(conclusionStep)//
    .title(translations.dataCopyPendingConclusion())//
    .conclusion()//

    .onNext().onPrevious().build();
  }

  private void initWidgets() {
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
  // DataCopy Display
  //

  @Override
  public String getSelectedDatasource() {
    return this.datasources.getValue(this.datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(List<DatasourceDto> datasources) {
    this.datasources.clear();
    for(int i = 0; i < datasources.size(); i++) {
      this.datasources.addItem(datasources.get(i).getName());
    }
  }

  @Override
  public HandlerRegistration addSubmitClickHandler(final ClickHandler submitHandler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        submitHandler.onClick(evt);
      }
    });
  }

  @Override
  public HandlerRegistration addJobLinkClickHandler(ClickHandler handler) {
    return jobLink.addClickHandler(handler);
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return true;
  }

  @Override
  public void setTableWidgetDisplay(TableListPresenter.Display display) {
    tablesList = display;
    display.setListWidth("28em");
    tablesPanel.setWidget(display.asWidget());
  }

  @Override
  public void renderPendingConclusion() {
    conclusionStep.setStepTitle(translations.dataCopyPendingConclusion());
    jobLink.setText("");
    dialog.setProgress(true);
    stepChain.onNext();
    dialog.setCancelEnabled(false);
    dialog.setPreviousEnabled(false);
    dialog.setFinishEnabled(false);
  }

  @Override
  public void renderCompletedConclusion(String jobId) {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.dataCopyCompletedConclusion());
    jobLink.setText(translations.jobLabel() + " #" + jobId);
    dialog.setFinishEnabled(true);
  }

  @Override
  public void renderFailedConclusion() {
    dialog.setProgress(false);
    conclusionStep.setStepTitle(translations.dataCopyFailedConclusion());
    dialog.setCancelEnabled(true);
    dialog.setPreviousEnabled(true);
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

  private void clearTablesStep() {
    tablesStep.setVisible(true);
    dialog.setHelpEnabled(false);
    if(tablesList != null) tablesList.clear();
  }

  private void clearDestinationStep() {
    // TODO datasources
    datasources.setSelectedIndex(0);
    incremental.setValue(true);
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

}
