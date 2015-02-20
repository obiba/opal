/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter.ImportDataInputsHandler;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.DatasourceParsingErrorPanel;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.Skippable;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.watopi.chosen.client.event.ChosenChangeEvent;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;
import static org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter.ImportDataStepHandler;

@SuppressWarnings("OverlyCoupledClass")
public class DataImportView extends ModalPopupViewWithUiHandlers<ModalUiHandlers> implements DataImportPresenter.Display {

  interface Binder extends UiBinder<Widget, DataImportView> {}

  private final Translations translations;

  @UiField
  WizardModalBox dialog;

  @UiField
  WizardStep formatSelectionStep;

  @UiField
  WizardStep formatStep;

  @UiField
  WizardStep destinationSelectionStep;

  @UiField
  WizardStep unitSelectionStep;

  @UiField
  WizardStep comparedDatasourcesReportStep;

  @UiField
  SimplePanel comparedDatasourcesReportPanel;

  @UiField
  Panel datasourceErrors;

  @UiField
  DatasourceParsingErrorPanel parsingErrors;

  @UiField
  WizardStep valuesStep;

  @UiField
  WizardStep archiveStep;

  @UiField
  FlowPanel helpCsv;

  @UiField
  FlowPanel helpOpalXml;

  @UiField
  FlowPanel helpSpss;

  @UiField
  FlowPanel helpLimeSurvey;

  @UiField
  FlowPanel helpOpalRest;

  @UiField
  FlowPanel helpJDBC;

  @UiField
  FlowPanel helpHealthCanada;

  @UiField
  FlowPanel helpGeonamesPostalCodes;

  @UiField
  Chooser formatChooser;

  private WizardStepChain stepChain;

  private StepInHandler comparedDatasourcesReportStepInHandler;

  private StepInHandler unitSelectionStepInHandler;

  private ImportDataStepHandler datasourceValuesStepInHandler;

  private ImportDataInputsHandler importDataInputsHandler;

  @Inject
  public DataImportView(EventBus eventBus, Binder uiBinder, Translations translations) {
    super(eventBus);
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
    initWidgets();
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
        .append(formatSelectionStep)//
        .title(translations.dataImportFormatStep())//

        .append(formatStep)//
        .onValidate(new ValidationHandler() {

          @Override
          public boolean validate() {
            return importDataInputsHandler.validateFormat();
          }
        }).onPrevious(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            clearError();
          }
        }).title(translations.dataImportFileStep())//

        .append(unitSelectionStep)//
        .title(translations.configureDataImport())//
        .onStepIn(new StepInHandler() {
          @Override
          public void onStepIn() {
            unitSelectionStepInHandler.onStepIn();
          }
        }) //

        .append(comparedDatasourcesReportStep)//
        .title(translations.dataImportComparedDatasourcesReportStep())//
        .onStepIn(new StepInHandler() {

          @Override
          public void onStepIn() {
            comparedDatasourcesReportStepInHandler.onStepIn();
          }
        })//
        .onValidate(new ValidationHandler() {

          @Override
          public boolean validate() {
            return importDataInputsHandler.validateComparedDatasourcesReport();
          }
        })//

        .append(valuesStep)//
        .onStepIn(new StepInHandler() {
          @Override
          public void onStepIn() {
            datasourceValuesStepInHandler.onStepIn();
          }
        })//
        .onValidate(new ValidationHandler() {
          @Override
          public boolean validate() {
            return datasourceValuesStepInHandler.isValid();
          }
        })
        .title(translations.dataImportValuesStep())//

        .append(archiveStep, new Skippable() {
          @Override
          public boolean skip() {
            String selection = formatChooser.getSelectedValue();
            return ImportFormat.LIMESURVEY.name().equals(selection) || ImportFormat.JDBC.name().equals(selection) ||
                ImportFormat.REST.name().equals(selection);
          }
        })//
        .title(translations.dataImportArchiveStep())//
        .onNext().onPrevious().build();
  }

  private void initWidgets() {
    formatChooser.addGroup(translations.fileBasedDatasources());
    formatChooser.addItemToGroup(translations.csvLabel(), ImportFormat.CSV.name());
    formatChooser.addItemToGroup(translations.opalXmlLabel(), ImportFormat.XML.name());
    formatChooser.addItemToGroup(translations.spssLabel(), ImportFormat.SPSS.name());
    formatChooser.addGroup(translations.remoteServerBasedDatasources());
    formatChooser.addItemToGroup(translations.limesurveyLabel(), ImportFormat.LIMESURVEY.name());
    formatChooser.addItemToGroup(translations.opalRestLabel(), ImportFormat.REST.name());
    formatChooser.addItemToGroup(translations.opalJDBCLabel(), ImportFormat.JDBC.name());
  }

  @Override
  public void removeFormat(ImportFormat format) {
    for(int i = 0; i < formatChooser.getItemCount(); i++) {
      if(formatChooser.getValue(i).equals(format.name())) {
        formatChooser.removeItem(i);
        break;
      }
    }
  }

  @Override
  public void showError(String messageKey) {
    dialog.addAlert(TranslationsUtils.replaceArguments(translations.userMessageMap().get(messageKey)), AlertType.ERROR);
  }

  @Override
  public void showError(String errorMessage, HasType<ControlGroupType> errorType) {
    if(errorType == null) {
      dialog.addAlert(errorMessage, AlertType.ERROR);

    } else {
      dialog.addAlert(errorMessage, AlertType.ERROR, errorType);
    }
  }

  @Override
  public void clearError() {
    dialog.closeAlerts();
  }

  @Override
  public Widget asWidget() {
    return dialog;
  }

  @Override
  @SuppressWarnings("PMD.NcssMethodCount")
  public void setInSlot(Object slot, IsWidget content) {
    if(!(slot instanceof Slots)) return;
    Widget wContent = content.asWidget();

    switch((Slots) slot) {
      case Destination:
        destinationSelectionStep.removeStepContent();
        destinationSelectionStep.add(wContent);
        break;
      case Unit:
        unitSelectionStep.removeStepContent();
        unitSelectionStep.add(wContent);
        break;
      case Values:
        valuesStep.removeStepContent();
        valuesStep.add(wContent);
        break;
      case Archive:
        archiveStep.removeStepContent();
        archiveStep.add(wContent);
        break;
    }
  }

  @Override
  public ImportFormat getImportFormat() {
    return ImportFormat.valueOf(formatChooser.getSelectedValue());
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
  public void onShow() {
    stepChain.reset();
  }

  @Override
  public HandlerRegistration addFormatChangeHandler(ChosenChangeEvent.ChosenChangeHandler handler) {
    return formatChooser.addChosenChangeHandler(handler);
  }

  @Override
  public void setFormatStepDisplay(WizardStepDisplay display) {
    formatStep.removeStepContent();
    formatStep.add(display.asWidget());
  }

  @Override
  public void setImportDataInputsHandler(ImportDataInputsHandler handler) {
    importDataInputsHandler = handler;
  }

  @Override
  public void setComparedDatasourcesReportStepInHandler(StepInHandler handler) {
    comparedDatasourcesReportStepInHandler = handler;
  }

  @Override
  public void setDatasourceValuesStepInHandler(ImportDataStepHandler handler) {
    datasourceValuesStepInHandler = handler;
  }

  @Override
  public void setComparedDatasourcesReportDisplay(WizardStepDisplay display) {
    comparedDatasourcesReportPanel.clear();
    comparedDatasourcesReportPanel.add(display.asWidget());
    comparedDatasourcesReportPanel.setVisible(true);
    datasourceErrors.setVisible(false);
    parsingErrors.setVisible(false);
  }

  @Override
  public HandlerRegistration addFinishClickHandler(final ClickHandler handler) {
    return dialog.addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent evt) {
        // asynchronous next, see setConclusion()
        handler.onClick(evt);
      }
    });
  }

  @Override
  public void prepareDatasourceCreation() {
    dialog.setProgress(true);
    datasourceErrors.setVisible(false);
    parsingErrors.setVisible(false);
  }

  @Override
  public void showDatasourceCreationError(ClientErrorDto errorDto) {
    comparedDatasourcesReportPanel.setVisible(false);
    if(errorDto != null) {
      if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
        parsingErrors.setErrors(errorDto);
        parsingErrors.setVisible(true);
      } else {
        showError(TranslationsUtils
            .replaceArguments(translations.userMessageMap().get(errorDto.getStatus()), errorDto.getArgumentsArray()),
            null);
      }
    }
    datasourceErrors.setVisible(true);
    dialog.setProgress(false);
    dialog.setNextEnabled(false);
  }

  @Override
  public void setUnitSelectionStepInHandler(StepInHandler handler) {
    unitSelectionStepInHandler = handler;
  }

  @Override
  public void showDatasourceCreationSuccess() {
    comparedDatasourcesReportPanel.setVisible(true);
    datasourceErrors.setVisible(false);
    parsingErrors.setVisible(false);
    dialog.setProgress(false);
  }

  @Override
  public void updateHelp() {
    hideHelpPanels();
    updateHelpPanelsVisibility();
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  private void updateHelpPanelsVisibility() {
    switch(getImportFormat()) {
      case CSV:
        helpCsv.setVisible(true);
        break;
      case XML:
        helpOpalXml.setVisible(true);
        break;
      case LIMESURVEY:
        helpLimeSurvey.setVisible(true);
        break;
      case REST:
        helpOpalRest.setVisible(true);
        break;
      case JDBC:
        helpJDBC.setVisible(true);
        break;
      case SPSS:
        helpSpss.setVisible(true);
        break;
    }
  }

  private void hideHelpPanels() {
    helpCsv.setVisible(false);
    helpOpalXml.setVisible(false);
    helpLimeSurvey.setVisible(false);
    helpJDBC.setVisible(false);
    helpOpalRest.setVisible(false);
    helpSpss.setVisible(false);
    helpHealthCanada.setVisible(false);
    helpGeonamesPostalCodes.setVisible(false);
  }
}
