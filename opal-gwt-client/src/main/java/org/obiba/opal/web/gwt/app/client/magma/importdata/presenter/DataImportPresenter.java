/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.support.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.ui.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallbacks;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.database.DatabaseDto;
import org.obiba.opal.web.model.client.database.SqlSettingsDto;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingConfigDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.watopi.chosen.client.event.ChosenChangeEvent;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

@SuppressWarnings("OverlyCoupledClass")
public class DataImportPresenter extends WizardPresenterWidget<DataImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  private DataConfigFormatStepPresenter formatStepPresenter;

  private final CsvFormatStepPresenter csvFormatStepPresenter;

  private final XmlFormatStepPresenter xmlFormatStepPresenter;

  private final SpssFormatStepPresenter spssFormatStepPresenter;

  private final LimesurveyStepPresenter limesurveyStepPresenter;

  private final JdbcStepPresenter jdbcStepPresenter;

  private final RestStepPresenter restStepPresenter;

  private final NoFormatStepPresenter noFormatStepPresenter;

  private final IdentifiersMappingSelectionStepPresenter identifiersMappingSelectionStepPresenter;

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final DatasourceValuesStepPresenter datasourceValuesStepPresenter;

  private final ArchiveStepPresenter archiveStepPresenter;

  private final Translations translations;

  private TransientDatasourceHandler transientDatasourceHandler;

  private ImportConfig importConfig;

  private String destination;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public DataImportPresenter(Display display, EventBus eventBus, //
      CsvFormatStepPresenter csvFormatStepPresenter, XmlFormatStepPresenter xmlFormatStepPresenter, //
      LimesurveyStepPresenter limesurveyStepPresenter, JdbcStepPresenter jdbcStepPresenter, //
      SpssFormatStepPresenter spssFormatStepPresenter,//
      RestStepPresenter restStepPresenter,//
      NoFormatStepPresenter noFormatStepPresenter,//
      IdentifiersMappingSelectionStepPresenter identifiersMappingSelectionStepPresenter, //
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ArchiveStepPresenter archiveStepPresenter, //
      DatasourceValuesStepPresenter datasourceValuesStepPresenter, Translations translations) {
    super(eventBus, display);
    this.csvFormatStepPresenter = csvFormatStepPresenter;
    this.xmlFormatStepPresenter = xmlFormatStepPresenter;
    this.spssFormatStepPresenter = spssFormatStepPresenter;
    this.limesurveyStepPresenter = limesurveyStepPresenter;
    this.jdbcStepPresenter = jdbcStepPresenter;
    this.restStepPresenter = restStepPresenter;
    this.noFormatStepPresenter = noFormatStepPresenter;
    this.identifiersMappingSelectionStepPresenter = identifiersMappingSelectionStepPresenter;
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.archiveStepPresenter = archiveStepPresenter;
    this.datasourceValuesStepPresenter = datasourceValuesStepPresenter;
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    bindPresenters();
    comparedDatasourcesReportPresenter.allowIgnoreAllModifications(false);
    setInSlotPresenters();

    getView().setUnitSelectionStepInHandler(new UnitSelectionStepInHandler());
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getView());
    getView().setComparedDatasourcesReportStepInHandler(transientDatasourceHandler = new TransientDatasourceHandler());
    getView().setDatasourceValuesStepInHandler(new DatasourceValuesHandler());

    addEventHandlers();
    updateFormatChooser();

    getEventBus().addHandlerToSource(NotificationEvent.getType(), datasourceValuesStepPresenter,
        new NotificationEvent.Handler() {
          @Override
          public void onUserMessage(NotificationEvent event) {
            for(String message : event.getMessages()) {
              getView().showError(TranslationsUtils
                  .replaceArguments(translations.userMessageMap().get(message), event.getMessageArgs()), null);
            }
            event.setConsumed(true);
          }
        });
  }

  private void setInSlotPresenters() {
    setInSlot(Display.Slots.Unit, identifiersMappingSelectionStepPresenter);
    setInSlot(Display.Slots.Values, datasourceValuesStepPresenter);
    setInSlot(Display.Slots.Archive, archiveStepPresenter);
    setInSlot(Display.Slots.Limesurvey, limesurveyStepPresenter);
    setInSlot(Display.Slots.Jdbc, jdbcStepPresenter);
    setInSlot(Display.Slots.Rest, restStepPresenter);
  }

  private void bindPresenters() {
    csvFormatStepPresenter.bind();
    xmlFormatStepPresenter.bind();
    spssFormatStepPresenter.bind();
    limesurveyStepPresenter.bind();
    jdbcStepPresenter.bind();
    restStepPresenter.bind();
    comparedDatasourcesReportPresenter.bind();
  }

  private void updateFormatChooser() {
    // Remove LimeSurvey and/or JDBC formats if no database of those types exists
    ResourceRequestBuilderFactory.<JsArray<DatabaseDto>>newBuilder()
        .forResource(UriBuilders.DATABASES_SQL.create().build())
        .withCallback(new ResourceCallback<JsArray<DatabaseDto>>() {

          @Override
          public void onResource(Response response, JsArray<DatabaseDto> resource) {
            boolean limeSurvey = false;
            boolean jdbc = false;
            for(int i = 0; i < resource.length(); i++) {
              SqlSettingsDto sqlSettingsDto = resource.get(i).getSqlSettings();
              if(sqlSettingsDto.getSqlSchema().getName().equals(SqlSettingsDto.SqlSchema.LIMESURVEY.getName())) {
                limeSurvey = true;
              } else if(sqlSettingsDto.getSqlSchema().getName().equals(SqlSettingsDto.SqlSchema.JDBC.getName()) &&
                  resource.get(i).getUsage().getName().equals(DatabaseDto.Usage.IMPORT.getName())) {
                jdbc = true;
              }
            }
            // Hide if not found
            if(!limeSurvey) {
              getView().removeFormat(ImportFormat.LIMESURVEY);
            }
            if(!jdbc) {
              getView().removeFormat(ImportFormat.JDBC);
            }
          }
        })//
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().removeFormat(ImportFormat.LIMESURVEY);
            getView().removeFormat(ImportFormat.JDBC);
          }
        }, Response.SC_FORBIDDEN, Response.SC_INTERNAL_SERVER_ERROR) //
        .get().send();
  }

  private void addEventHandlers() {
    registerHandler(getView().addFormatChangeHandler(new ChosenChangeEvent.ChosenChangeHandler() {
      @Override
      public void onChange(ChosenChangeEvent event) {
        updateFormatStepDisplay();
      }
    }));
    getView().setImportDataInputsHandler(new ImportDataInputsHandlerImpl());
  }

  public interface ImportDataStepHandler extends StepInHandler {
    boolean isValid();
  }

  public interface ImportDataInputsHandler {
    boolean validateFormat();

    boolean validateComparedDatasourcesReport();
  }

  @Override
  protected void onFinish() {
    ImportConfig dataToImport = transientDatasourceHandler.getImportConfig();
    archiveStepPresenter.updateImportData(dataToImport);
    launchImport(dataToImport);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    csvFormatStepPresenter.unbind();
    xmlFormatStepPresenter.unbind();
    spssFormatStepPresenter.unbind();
    limesurveyStepPresenter.unbind();
    jdbcStepPresenter.unbind();
    restStepPresenter.unbind();
  }

  @Override
  public void onReveal() {
    updateFormatStepDisplay();
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        String datasourceName = (String) event.getEventParameters()[0];
        destination = datasourceName;
        csvFormatStepPresenter.setDestination(datasourceName);
      }
    }
  }

  @Override
  protected boolean hideOnFinish() {
    return true;
  }

  @Override
  protected void onCancel() {
    super.onCancel();
    if(transientDatasourceHandler != null) {
      transientDatasourceHandler.removeTransientDatasource();
    }
  }

  @SuppressWarnings({ "PMD.NcssMethodCount", "OverlyLongMethod" })
  private void updateFormatStepDisplay() {
    getView().updateHelp();
    switch(getView().getImportFormat()) {
      case CSV:
        csvFormatStepPresenter.clear();
        formatStepPresenter = csvFormatStepPresenter;
        getView().setFormatStepDisplay(csvFormatStepPresenter.getView());
        break;
      case XML:
        formatStepPresenter = xmlFormatStepPresenter;
        getView().setFormatStepDisplay(xmlFormatStepPresenter.getView());
        break;
      case LIMESURVEY:
        formatStepPresenter = limesurveyStepPresenter;
        getView().setFormatStepDisplay(limesurveyStepPresenter.getView());
        break;
      case JDBC:
        formatStepPresenter = jdbcStepPresenter;
        getView().setFormatStepDisplay(jdbcStepPresenter.getView());
        break;
      case REST:
        formatStepPresenter = restStepPresenter;
        getView().setFormatStepDisplay(restStepPresenter.getView());
        break;
      case SPSS:
        formatStepPresenter = spssFormatStepPresenter;
        getView().setFormatStepDisplay(spssFormatStepPresenter.getView());
        break;
      default:
        noFormatStepPresenter.setImportFormat(getView().getImportFormat());
        formatStepPresenter = noFormatStepPresenter;
        getView().setFormatStepDisplay(noFormatStepPresenter.getView());
    }
  }

  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  private void launchImport(@SuppressWarnings("ParameterHidesMemberVariable") ImportConfig importConfig) {
    this.importConfig = importConfig;
    switch(importConfig.getImportFormat()) {
      case XML:
        submitJob(createImportCommandOptionsDto(importConfig.getXmlFile()));
        break;
      case CSV:
        submitJob(createImportCommandOptionsDto(importConfig.getCsvFile()));
        break;
      case LIMESURVEY:
        submitJob(createLimesurveyImportCommandOptionsDto());
        break;
      case JDBC:
        submitJob(createJdbcImportCommandOptionsDto());
        break;
      case REST:
        submitJob(createRestImportCommandOptionsDto());
        break;
      case SPSS:
        submitJob(createImportCommandOptionsDto(importConfig.getSpssFile()));
        break;
      case HEALTH_CANADA:
        submitJob(createImportCommandOptionsDto(null));
        break;
      case GEONAMES_POSTAL_CODES:
        submitJob(createImportCommandOptionsDto(null));
        break;
    }
  }

  private void submitJob(ImportCommandOptionsDto dto) {
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilder.create().segment("project", dto.getDestination(), "commands", "_import").build()) //
        .post() //
        .withResourceBody(ImportCommandOptionsDto.stringify(dto)) //
        .withCallback(new SubmitJobResponseCodeCallBack(), SC_CREATED, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR) //
        .send();
  }

  private ImportCommandOptionsDto createLimesurveyImportCommandOptionsDto() {
    return createImportCommandOptionsDto(null);
  }

  private ImportCommandOptionsDto createJdbcImportCommandOptionsDto() {
    return createImportCommandOptionsDto(null);
  }

  private ImportCommandOptionsDto createRestImportCommandOptionsDto() {
    return createImportCommandOptionsDto(null);
  }

  private ImportCommandOptionsDto createImportCommandOptionsDto(@Nullable String selectedFile) {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importConfig.getDestinationDatasourceName());
    if(importConfig.isArchiveMove()) {
      dto.setArchive(importConfig.getArchiveDirectory());
      JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
      selectedFiles.push(selectedFile);
      dto.setFilesArray(selectedFiles);
    }
    if(importConfig.isIdentifierSharedWithUnit()) {
      IdentifiersMappingConfigDto idConfig = IdentifiersMappingConfigDto.create();
      idConfig.setName(importConfig.getIdentifiersMapping());
      idConfig.setAllowIdentifierGeneration(importConfig.isAllowIdentifierGeneration());
      idConfig.setIgnoreUnknownIdentifier(importConfig.isIgnoreUnknownIdentifier());
      dto.setIdConfig(idConfig);
    }
    JsArrayString selectedTables = JavaScriptObject.createArray().cast();
    for(String tableName : comparedDatasourcesReportPresenter.getSelectedTables()) {
      selectedTables.push(importConfig.getTransientDatasourceName() + "." + tableName);
    }
    dto.setTablesArray(selectedTables);
    return dto;
  }

  private final class SubmitJobResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == SC_CREATED) {
        String location = response.getHeader("Location");
        String jobId = location.substring(location.lastIndexOf('/') + 1);
        getEventBus()
            .fireEvent(NotificationEvent.newBuilder().info("DataImportationProcessLaunched").args(jobId).build());
      } else {
        getView().showError(response.getText());
      }
    }
  }

  private final class ImportDataInputsHandlerImpl implements ImportDataInputsHandler {
    @Override
    public boolean validateFormat() {
      getView().clearError();
      if(formatStepPresenter.validate()) return true;

      for(Map.Entry<HasType<ControlGroupType>, String> entry : getErrorEntries()) {
        getView().showError(entry.getValue(), entry.getKey());
      }

      return false;
    }

    private Iterable<Map.Entry<HasType<ControlGroupType>, String>> getErrorEntries() {
      Set<Map.Entry<HasType<ControlGroupType>, String>> entries
          = new HashSet<Map.Entry<HasType<ControlGroupType>, String>>();

      switch(getView().getImportFormat()) {
        case CSV:
          entries = csvFormatStepPresenter.getErrors().entrySet();
          break;
        case XML:
          entries = xmlFormatStepPresenter.getErrors().entrySet();
          break;
        case REST:
          entries = restStepPresenter.getErrors().entrySet();
          break;
        case SPSS:
          entries = spssFormatStepPresenter.getErrors().entrySet();
          break;
      }
      return entries;
    }

    @Override
    public boolean validateComparedDatasourcesReport() {
      getView().clearError();
      if(comparedDatasourcesReportPresenter.getSelectedTables().isEmpty()) {
        getView().showError("TableSelectionIsRequired");
        return false;
      }
      ImportConfig localImportConfig = transientDatasourceHandler.getImportConfig();
      datasourceValuesStepPresenter.setDatasource(localImportConfig.getTransientDatasourceName(),
          comparedDatasourcesReportPresenter.getSelectedTables());

      return comparedDatasourcesReportPresenter.canBeSubmitted();
    }
  }

  public static class Wizard extends WizardProxy<DataImportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataImportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final class UnitSelectionStepInHandler implements StepInHandler {

    @Override
    public void onStepIn() {
      importConfig = formatStepPresenter.getImportConfig();
      importConfig.setDestinationDatasourceName(destination);
    }
  }

  private final class TransientDatasourceHandler implements StepInHandler {

    private ImportConfig importConfig;

    private Request transientRequest;

    private Request diffRequest;

    public ImportConfig getImportConfig() {
      return importConfig;
    }

    @Override
    public void onStepIn() {
      comparedDatasourcesReportPresenter.getView().clearDisplay();
      removeTransientDatasource();
      importConfig = formatStepPresenter.getImportConfig();
      importConfig.setDestinationDatasourceName(destination);
      identifiersMappingSelectionStepPresenter.updateImportConfig(importConfig);
      createTransientDatasource();
    }

    private void removeTransientDatasource() {
      if(importConfig == null) return;
      if(transientRequest != null) {
        transientRequest.cancel();
        transientRequest = null;
      }
      if(diffRequest != null) {
        diffRequest.cancel();
        diffRequest = null;
      }

      deleteTransientDatasource();
      importConfig = null;
    }

    private void deleteTransientDatasource() {
      if(importConfig.getTransientDatasourceName() == null) return;

      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilder.create().segment("datasource", importConfig.getTransientDatasourceName()).build()) //
          .delete() //
          .withCallback(ResponseCodeCallbacks.NO_OP, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND) //
          .send();
    }

    private void createTransientDatasource() {
      getView().prepareDatasourceCreation();

      DatasourceFactoryDto factory = DatasourceDtos.createDatasourceFactoryDto(importConfig);

      String factoryStr = DatasourceFactoryDto.stringify(factory);
      transientRequest = ResourceRequestBuilderFactory.<DatasourceFactoryDto>newBuilder().forResource(
          UriBuilders.PROJECT_TRANSIENT_DATASOURCE.create().build(importConfig.getDestinationDatasourceName())) //
          .withResourceBody(factoryStr) //
          .withCallback(new CreateTransientDatasourceCallback(factory), SC_CREATED, SC_BAD_REQUEST,
              SC_INTERNAL_SERVER_ERROR) //
          .post().send();
    }

    private class CreateTransientDatasourceCallback implements ResponseCodeCallback {

      private final DatasourceFactoryDto factory;

      private CreateTransientDatasourceCallback(DatasourceFactoryDto factory) {
        this.factory = factory;
      }

      @Override
      public void onResponseCode(Request request, Response response) {
        transientRequest = null;
        if(response.getStatusCode() == SC_CREATED) {
          DatasourceDto datasourceDto = JsonUtils.unsafeEval(response.getText());
          importConfig.setTransientDatasourceName(datasourceDto.getName());
          datasourceDiff(datasourceDto);
        } else {
          getView().showDatasourceCreationError((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
        }
      }

      private void datasourceDiff(DatasourceDto datasourceDto) {

        DatasourceCreatedCallback datasourceCreatedCallback = new DatasourceCreatedCallback() {

          @Override
          public void onSuccess(@SuppressWarnings("ParameterHidesMemberVariable") DatasourceFactoryDto factory,
              DatasourceDto datasource) {
            getView().showDatasourceCreationSuccess();
          }

          @Override
          public void onFailure(@SuppressWarnings("ParameterHidesMemberVariable") DatasourceFactoryDto factory,
              ClientErrorDto error) {
            getView().showDatasourceCreationError(error);
          }
        };
        diffRequest = comparedDatasourcesReportPresenter
            .compare(importConfig.getTransientDatasourceName(), importConfig.getDestinationDatasourceName(),
                datasourceCreatedCallback, factory, datasourceDto);
        getView().showDatasourceCreationSuccess();
      }
    }

  }

  private final class DatasourceValuesHandler implements ImportDataStepHandler {
    @Override
    public void onStepIn() {
      datasourceValuesStepPresenter.getView().hideErrors();
    }

    @Override
    public boolean isValid() {
      return datasourceValuesStepPresenter.isValid();
    }
  }

  public interface Display extends WizardView, HasUiHandlers<ModalUiHandlers> {

    enum Slots {
      Destination, Unit, Values, Archive, Limesurvey, Jdbc, Rest
    }

    ImportFormat getImportFormat();

    void updateHelp();

    void setImportDataInputsHandler(ImportDataInputsHandler handler);

    void prepareDatasourceCreation();

    void showDatasourceCreationSuccess();

    void showDatasourceCreationError(ClientErrorDto errorDto);

    void setUnitSelectionStepInHandler(StepInHandler handler);

    void setComparedDatasourcesReportStepInHandler(StepInHandler handler);

    void setComparedDatasourcesReportDisplay(WizardStepDisplay display);

    void setDatasourceValuesStepInHandler(ImportDataStepHandler handler);

    HandlerRegistration addFormatChangeHandler(ChosenChangeEvent.ChosenChangeHandler handler);

    void setFormatStepDisplay(WizardStepDisplay display);

    void removeFormat(ImportFormat format);

    void showError(String messageKey);

    void showError(String errorMessage, HasType<ControlGroupType> errorType);

    void clearError();
  }

  public interface DataConfigFormatStepPresenter {

    /**
     * Get the import config as collected.
     *
     * @return
     */
    ImportConfig getImportConfig();

    /**
     * Validate the import data were correctly provided, and send notification error messages if any.
     *
     * @return
     */
    boolean validate();

  }

}
