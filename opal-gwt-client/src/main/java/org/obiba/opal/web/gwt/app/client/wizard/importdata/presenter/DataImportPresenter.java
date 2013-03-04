/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.util.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter.Display.Slots;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallbacks;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;
import static org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig.ImportFormat;

@SuppressWarnings("OverlyCoupledClass")
public class DataImportPresenter extends WizardPresenterWidget<DataImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  private DataConfigFormatStepPresenter formatStepPresenter;

  private final CsvFormatStepPresenter csvFormatStepPresenter;

  private final XmlFormatStepPresenter xmlFormatStepPresenter;

  private final SpssFormatStepPresenter spssFormatStepPresenter;

  private final LimesurveyStepPresenter limesurveyStepPresenter;

  private final RestStepPresenter restStepPresenter;

  private final DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  private final UnitSelectionStepPresenter unitSelectionStepPresenter;

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final DatasourceValuesStepPresenter datasourceValuesStepPresenter;

  private final ArchiveStepPresenter archiveStepPresenter;

  private TransientDatasourceHandler transientDatasourceHandler;

  private ImportConfig importConfig;

  @Inject
  @SuppressWarnings({ "PMD.ExcessiveParameterList", "ConstructorWithTooManyParameters" })
  public DataImportPresenter(Display display, EventBus eventBus, //
      CsvFormatStepPresenter csvFormatStepPresenter, XmlFormatStepPresenter xmlFormatStepPresenter, //
      LimesurveyStepPresenter limesurveyStepPresenter, SpssFormatStepPresenter spssFormatStepPresenter,//
      RestStepPresenter restStepPresenter,//
      DestinationSelectionStepPresenter destinationSelectionStepPresenter,
      UnitSelectionStepPresenter unitSelectionStepPresenter, //
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ArchiveStepPresenter archiveStepPresenter, //
      DatasourceValuesStepPresenter datasourceValuesStepPresenter) {
    super(eventBus, display);
    this.csvFormatStepPresenter = csvFormatStepPresenter;
    this.xmlFormatStepPresenter = xmlFormatStepPresenter;
    this.spssFormatStepPresenter = spssFormatStepPresenter;
    this.limesurveyStepPresenter = limesurveyStepPresenter;
    this.restStepPresenter = restStepPresenter;
    this.destinationSelectionStepPresenter = destinationSelectionStepPresenter;
    this.unitSelectionStepPresenter = unitSelectionStepPresenter;
    this.comparedDatasourcesReportPresenter = comparedDatasourcesReportPresenter;
    this.archiveStepPresenter = archiveStepPresenter;
    this.datasourceValuesStepPresenter = datasourceValuesStepPresenter;
  }

  @Override
  protected void onBind() {
    super.onBind();
    csvFormatStepPresenter.bind();
    xmlFormatStepPresenter.bind();
    spssFormatStepPresenter.bind();
    limesurveyStepPresenter.bind();
    restStepPresenter.bind();
    comparedDatasourcesReportPresenter.bind();

    comparedDatasourcesReportPresenter.allowIgnoreAllModifications(false);

    setInSlot(Slots.Destination, destinationSelectionStepPresenter);
    setInSlot(Slots.Unit, unitSelectionStepPresenter);
    setInSlot(Slots.Values, datasourceValuesStepPresenter);
    setInSlot(Slots.Archive, archiveStepPresenter);
    setInSlot(Slots.Limesurvey, limesurveyStepPresenter);
    setInSlot(Slots.Rest, restStepPresenter);

    getView().setUnitSelectionStepInHandler(new UnitSelectionStepInHandler());
    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getDisplay());
    getView().setComparedDatasourcesReportStepInHandler(transientDatasourceHandler = new TransientDatasourceHandler());

    addEventHandlers();
  }

  private void addEventHandlers() {
    registerHandler(getView().addFormatChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent evt) {
        updateFormatStepDisplay();
      }
    }));
    getView().setImportDataInputsHandler(new ImportDataInputsHandlerImpl());
  }

  public interface ImportDataInputsHandler {
    boolean validateFormat();

    boolean validateDestination();

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
    restStepPresenter.unbind();
  }

  @Override
  public void onReveal() {
    destinationSelectionStepPresenter.refreshDisplay(); // to refresh the datasources
    updateFormatStepDisplay();
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    destinationSelectionStepPresenter.setDestination(null);
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        String datasourceName = (String) event.getEventParameters()[0];
        destinationSelectionStepPresenter.setDestination(datasourceName);
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
    destinationSelectionStepPresenter.setImportFormat(getView().getImportFormat());
    getView().updateHelp();
    switch(getView().getImportFormat()) {
      case CSV:
        csvFormatStepPresenter.clear();
        formatStepPresenter = csvFormatStepPresenter;
        getView().setFormatStepDisplay(csvFormatStepPresenter.getDisplay());
        break;
      case XML:
        formatStepPresenter = xmlFormatStepPresenter;
        getView().setFormatStepDisplay(xmlFormatStepPresenter.getDisplay());
        break;
      case LIMESURVEY:
        formatStepPresenter = limesurveyStepPresenter;
        getView().setFormatStepDisplay(limesurveyStepPresenter.getView());
        break;
      case REST:
        formatStepPresenter = restStepPresenter;
        getView().setFormatStepDisplay(restStepPresenter.getView());
        break;
      case SPSS:
        formatStepPresenter = spssFormatStepPresenter;
        getView().setFormatStepDisplay(spssFormatStepPresenter.getDisplay());
        break;
      default:
        formatStepPresenter = null;
        throw new IllegalStateException("Unknown format: " + getView().getImportFormat());
    }
  }

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
      case REST:
        submitJob(createRestImportCommandOptionsDto());
        break;
      case SPSS:
        submitJob(createImportCommandOptionsDto(importConfig.getSpssFile()));
        break;
    }
  }

  private void submitJob(ImportCommandOptionsDto dto) {
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilder.create().segment("datasource", dto.getDestination(), "commands", "_import").build()) //
        .post() //
        .withResourceBody(ImportCommandOptionsDto.stringify(dto)) //
        .withCallback(new SubmitJobResponseCodeCallBack(), SC_CREATED, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR) //
        .send();
  }

  private ImportCommandOptionsDto createLimesurveyImportCommandOptionsDto() {
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
      dto.setUnit(importConfig.getUnit());
      dto.setForce(false);
      dto.setIgnore(true);
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
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  private final class ImportDataInputsHandlerImpl implements ImportDataInputsHandler {
    @Override
    public boolean validateFormat() {
      if(formatStepPresenter.validate()) {
        if(getView().getImportFormat() == ImportFormat.CSV) {
          String name = csvFormatStepPresenter.getSelectedFile();
          name = name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.'));
          destinationSelectionStepPresenter.getView().setTable(name);
        } else {
          destinationSelectionStepPresenter.getView().setTable("");
        }
        return true;
      }
      return false;
    }

    @Override
    public boolean validateDestination() {
      return destinationSelectionStepPresenter.validate();
    }

    @Override
    public boolean validateComparedDatasourcesReport() {
      if(comparedDatasourcesReportPresenter.getSelectedTables().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("TableSelectionIsRequired").build());
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
      destinationSelectionStepPresenter.updateImportConfig(importConfig);
      unitSelectionStepPresenter.setEntityType(importConfig.getEntityType());
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
      comparedDatasourcesReportPresenter.getDisplay().clearDisplay();
      removeTransientDatasource();
      importConfig = formatStepPresenter.getImportConfig();
      destinationSelectionStepPresenter.updateImportConfig(importConfig);
      unitSelectionStepPresenter.updateImportConfig(importConfig);
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

      transientRequest = ResourceRequestBuilderFactory.<DatasourceFactoryDto>newBuilder()
          .forResource("/transient-datasources") //
          .post() //
          .withResourceBody(DatasourceFactoryDto.stringify(factory)) //
          .withCallback(new CreateTransientDatasourceCallback(factory), SC_CREATED, SC_BAD_REQUEST,
              SC_INTERNAL_SERVER_ERROR) //
          .send();
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
          DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
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

  public interface Display extends WizardView {

    enum Slots {
      Destination, Unit, Values, Archive, Limesurvey, Rest
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

    HandlerRegistration addFormatChangeHandler(ChangeHandler handler);

    void setFormatStepDisplay(WizardStepDisplay display);

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
