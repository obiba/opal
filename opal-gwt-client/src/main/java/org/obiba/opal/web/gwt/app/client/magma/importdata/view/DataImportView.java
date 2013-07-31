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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.WizardModalBox;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.Skippable;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepChain;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.WidgetProvider;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter.ImportDataInputsHandler;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.DatasourceParsingErrorPanel;
import org.obiba.opal.web.gwt.app.client.ui.ModalViewImpl;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.Modal;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

@SuppressWarnings("OverlyCoupledClass")
public class DataImportView extends ModalViewImpl implements DataImportPresenter.Display {

  interface ViewUiBinder extends UiBinder<Widget, DataImportView> {}

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

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
  FlowPanel helpHealthCanada;

  @UiField
  FlowPanel helpGeonamesPostalCodes;

  @UiField
  HTMLPanel formatSelectionHelp;

  @UiField
  HTMLPanel destinationSelectionHelp;

  @UiField
  HTMLPanel unitSelectionHelp;

  @UiField
  HTMLPanel archiveHelp;

  @UiField
  Chooser formatChooser;

  private final EventBus eventBus;

  private WizardStepDisplay formatStepDisplay;

  private WizardStepChain stepChain;

  private StepInHandler comparedDatasourcesReportStepInHandler;

  private StepInHandler unitSelectionStepInHandler;

  private StepInHandler datasourceValuesStepInHandler;

  private Widget comparedDatasourcesReportHelp;

  private ImportDataInputsHandler importDataInputsHandler;

  @Inject
  public DataImportView(EventBus eventBus) {
    super(eventBus);
    this.eventBus = eventBus;
    widget = uiBinder.createAndBindUi(this);
    initWidgets();
    initWizardDialog();
  }

  private void initWizardDialog() {
    stepChain = WizardStepChain.Builder.create(dialog)//
        .append(formatSelectionStep, formatSelectionHelp)//
        .title(translations.dataImportFormatStep())//

        .append(formatStep, null, new Skippable() {
          @Override
          public boolean skip() {
            String selection = formatChooser.getSelectedValue();
            return ImportFormat.GEONAMES_POSTAL_CODES.name().equals(selection) ||
                ImportFormat.HEALTH_CANADA.name().equals(selection);
          }
        })//
        .help(new WidgetProvider() {

          @Override
          public Widget getWidget() {
            return formatStepDisplay.getStepHelp();
          }
        }) //
        .onValidate(new ValidationHandler() {

          @Override
          public boolean validate() {
            return importDataInputsHandler.validateFormat();
          }
        }).title(translations.dataImportFileStep())//

        .append(destinationSelectionStep, destinationSelectionHelp)//
        .title(translations.dataImportDestinationStep())//
        .onValidate(new ValidationHandler() {

          @Override
          public boolean validate() {
            return importDataInputsHandler.validateDestination();
          }
        })//

        .append(unitSelectionStep, unitSelectionHelp)//
        .title(translations.configureDataImport())//
        .onStepIn(new StepInHandler() {
          @Override
          public void onStepIn() {
            unitSelectionStepInHandler.onStepIn();
          }
        }) //

        .append(comparedDatasourcesReportStep)//
        .title(translations.dataImportComparedDatasourcesReportStep())//
        .help(new WidgetProvider() {

          @Override
          public Widget getWidget() {
            return comparedDatasourcesReportHelp;
          }
        })//
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
        }).title(translations.dataImportValuesStep())//

        .append(archiveStep, archiveHelp, new Skippable() {
          @Override
          public boolean skip() {
            String selection = formatChooser.getSelectedValue();
            return ImportFormat.LIMESURVEY.name().equals(selection) || ImportFormat.REST.name().equals(selection) ||
                ImportFormat.GEONAMES_POSTAL_CODES.name().equals(selection) ||
                ImportFormat.HEALTH_CANADA.name().equals(selection);
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
    formatChooser.addGroup(translations.publicDatasources());
    formatChooser.addItemToGroup(translations.geonamesPostalCodesLabel(), ImportFormat.GEONAMES_POSTAL_CODES.name());
    formatChooser.addItemToGroup(translations.healthCanadaLabel(), ImportFormat.HEALTH_CANADA.name());
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  protected Modal asModal() {
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
  public void show() {
    stepChain.reset();
    super.show();
  }

  @Override
  public HandlerRegistration addFormatChangeHandler(ChangeHandler handler) {
    return formatChooser.addChangeHandler(handler);
  }

  @Override
  public void setFormatStepDisplay(WizardStepDisplay display) {
    formatStepDisplay = display;
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
  public void setDatasourceValuesStepInHandler(StepInHandler handler) {
    datasourceValuesStepInHandler = handler;
  }

  @Override
  public void setComparedDatasourcesReportDisplay(WizardStepDisplay display) {
    comparedDatasourcesReportPanel.clear();
    comparedDatasourcesReportPanel.add(display.asWidget());
    comparedDatasourcesReportPanel.setVisible(true);
    datasourceErrors.setVisible(false);
    parsingErrors.setVisible(false);
    comparedDatasourcesReportHelp = display.getStepHelp();
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
        eventBus.fireEvent(
            NotificationEvent.newBuilder().error(errorDto.getStatus()).args(errorDto.getArgumentsArray()).build());
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
      case SPSS:
        helpSpss.setVisible(true);
        break;
      case HEALTH_CANADA:
        helpHealthCanada.setVisible(true);
        break;
      case GEONAMES_POSTAL_CODES:
        helpGeonamesPostalCodes.setVisible(true);
        break;

    }
  }

  private void hideHelpPanels() {
    helpCsv.setVisible(false);
    helpOpalXml.setVisible(false);
    helpLimeSurvey.setVisible(false);
    helpOpalRest.setVisible(false);
    helpSpss.setVisible(false);
    helpHealthCanada.setVisible(false);
    helpGeonamesPostalCodes.setVisible(false);
  }
}
