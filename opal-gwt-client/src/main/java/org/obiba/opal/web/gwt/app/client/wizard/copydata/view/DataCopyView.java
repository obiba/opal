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
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.ResetHandler;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.TableChooser;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardDialogBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupViewImpl;

/**
 * View of the dialog used to export data from Opal.
 */
public class DataCopyView extends PopupViewImpl implements DataCopyPresenter.Display {

  private static final Translations translations = GWT.create(Translations.class);

  @UiTemplate("DataCopyView.ui.xml")
  interface DataCopyUiBinder extends UiBinder<DialogBox, DataCopyView> {}

  private static final DataCopyUiBinder uiBinder = GWT.create(DataCopyUiBinder.class);

  private final Widget widget;

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

  @UiField(provided = true)
  final TableChooser tableChooser;

  @UiField
  ListBox datasources;

  @UiField
  CheckBox incremental;

  @UiField
  CheckBox copyNullValues;

  @UiField
  HTMLPanel destinationHelpPanel;

  @UiField
  CheckBox useAlias;

  private ValidationHandler tablesValidator;

  private ValidationHandler destinationValidator;

  private WizardStepChain stepChain;

  @Inject
  public DataCopyView(EventBus eventBus) {
    super(eventBus);
    tableChooser = new TableChooser(true);
    widget = uiBinder.createAndBindUi(this);
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

          private void clearTablesStep() {
            tablesStep.setVisible(true);
            dialog.setHelpEnabled(false);
            tableChooser.clear();
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

          private void clearDestinationStep() {
            // TODO datasources
            datasources.setSelectedIndex(0);
            incremental.setValue(true);
          }
        })//
        .append(conclusionStep)//
        .title(translations.dataCopyPendingConclusion())//
        .conclusion()//

        .onNext().onPrevious().build();
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
  public String getSelectedDatasource() {
    return datasources.getValue(datasources.getSelectedIndex());
  }

  @Override
  public void setDatasources(List<DatasourceDto> datasources) {
    this.datasources.clear();
    for(DatasourceDto datasource : datasources) {
      this.datasources.addItem(datasource.getName());
    }
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
  public HandlerRegistration addJobLinkClickHandler(ClickHandler handler) {
    return jobLink.addClickHandler(handler);
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public boolean isCopyNullValues() {
    return copyNullValues.getValue();
  }

  @Override
  public boolean isWithVariables() {
    return true;
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
  public void show() {
    stepChain.reset();
    super.show();
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
    tablesValidator = handler;
  }

  @Override
  public void setDestinationValidator(ValidationHandler handler) {
    destinationValidator = handler;
  }

  @Override
  public boolean isUseAlias() {
    return useAlias.getValue();
  }

  @Override
  public void addTableSelections(JsArray<TableDto> tables) {
    tableChooser.addTableSelections(tables);
  }

  @Override
  public void selectTable(TableDto table) {
    tableChooser.selectTable(table);
  }

  @Override
  public void selectAllTables() {
    tableChooser.selectAllTables();
  }

  @Override
  public List<TableDto> getSelectedTables() {
    return tableChooser.getSelectedTables();
  }

}
