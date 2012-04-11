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
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.util.DatasourceDtos;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepController.StepInHandler;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DataImportPresenter.Display.Slots;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.ComparedDatasourcesReportStepPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class DataImportPresenter extends WizardPresenterWidget<DataImportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  private DataImportFormatStepPresenter formatStepPresenter;

  private final CsvFormatStepPresenter csvFormatStepPresenter;

  private final XmlFormatStepPresenter xmlFormatStepPresenter;

  private final LimesurveyStepPresenter limesurveyStepPresenter;

  private final DestinationSelectionStepPresenter destinationSelectionStepPresenter;

  private final UnitSelectionStepPresenter unitSelectionStepPresenter;

  private final ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter;

  private final DatasourceValuesStepPresenter datasourceValuesStepPresenter;

  private final ArchiveStepPresenter archiveStepPresenter;

  private TransientDatasourceHandler transientDatasourceHandler;

  private ImportData importData;

  @Inject
  @SuppressWarnings("PMD.ExcessiveParameterList")
  public DataImportPresenter(final Display display, final EventBus eventBus, //
      CsvFormatStepPresenter csvFormatStepPresenter, XmlFormatStepPresenter xmlFormatStepPresenter, //
      LimesurveyStepPresenter limesurveyStepPresenter,//
      DestinationSelectionStepPresenter destinationSelectionStepPresenter,
      UnitSelectionStepPresenter unitSelectionStepPresenter, //
      ComparedDatasourcesReportStepPresenter comparedDatasourcesReportPresenter,
      ArchiveStepPresenter archiveStepPresenter, //
      DatasourceValuesStepPresenter datasourceValuesStepPresenter) {
    super(eventBus, display);
    this.csvFormatStepPresenter = csvFormatStepPresenter;
    this.xmlFormatStepPresenter = xmlFormatStepPresenter;
    this.limesurveyStepPresenter = limesurveyStepPresenter;
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
    limesurveyStepPresenter.bind();
    comparedDatasourcesReportPresenter.bind();

    comparedDatasourcesReportPresenter.allowIgnoreAllModifications(false);

    setInSlot(Slots.Destination, destinationSelectionStepPresenter);
    setInSlot(Slots.Unit, unitSelectionStepPresenter);
    setInSlot(Slots.Values, datasourceValuesStepPresenter);
    setInSlot(Slots.Archive, archiveStepPresenter);

    //TODO
    setInSlot(new Object(), limesurveyStepPresenter);

    getView().setComparedDatasourcesReportDisplay(comparedDatasourcesReportPresenter.getDisplay());
    getView().setComparedDatasourcesReportStepInHandler(transientDatasourceHandler = new TransientDatasourceHandler());

    addEventHandlers();
  }

  protected void addEventHandlers() {
    super.registerHandler(getView().addFormatChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent evt) {
        updateFormatStepDisplay();
      }
    }));
    getView().setImportDataInputsHandler(new ImportDataInputsHandlerImpl());

  }

  public interface ImportDataInputsHandler {
    public boolean validateFormat();

    public boolean validateDestination();

    public boolean validateComparedDatasourcesReport();
  }

  @Override
  protected void onFinish() {
    ImportData importData = transientDatasourceHandler.getImportData();
    archiveStepPresenter.updateImportData(importData);
    launchImport(importData);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    csvFormatStepPresenter.unbind();
    xmlFormatStepPresenter.unbind();
    limesurveyStepPresenter.unbind();
  }

  @Override
  public void onReveal() {
    destinationSelectionStepPresenter.refreshDisplay(); // to refresh the datasources
    updateFormatStepDisplay();
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

  private void updateFormatStepDisplay() {
    destinationSelectionStepPresenter.setImportFormat(getView().getImportFormat());
    if(getView().getImportFormat().equals(ImportFormat.CSV)) {
      csvFormatStepPresenter.clear();
      this.formatStepPresenter = csvFormatStepPresenter;
      getView().setFormatStepDisplay(csvFormatStepPresenter.getDisplay());
    } else if(getView().getImportFormat().equals(ImportFormat.XML)) {
      this.formatStepPresenter = xmlFormatStepPresenter;
      getView().setFormatStepDisplay(xmlFormatStepPresenter.getDisplay());
    } else if(getView().getImportFormat() == ImportFormat.LIMESURVEY) {
      this.formatStepPresenter = limesurveyStepPresenter;
      getView().setFormatStepDisplay(limesurveyStepPresenter.getView());
    } else {
      this.formatStepPresenter = null;
      throw new IllegalStateException("Unknown format: " + getView().getImportFormat());
    }
  }

  private void launchImport(ImportData importData) {
    this.importData = importData;

    if(importData.getImportFormat().equals(ImportFormat.XML)) {
      submitJob(createImportCommandOptionsDto(importData.getXmlFile()));
    } else if(importData.getImportFormat().equals(ImportFormat.CSV)) {
      submitJob(createImportCommandOptionsDto(importData.getCsvFile()));
    } else if(importData.getImportFormat().equals(ImportFormat.LIMESURVEY)) {
      submitJob(createLimesurveyImportCommandOptionsDto());
    }
  }

  private void submitJob(ImportCommandOptionsDto dto) {

    ResponseCodeCallback callback = new SubmitJobResponseCodeCallBack();

    ResourceRequestBuilderFactory.newBuilder().forResource("/shell/import").post() //
        .withResourceBody(ImportCommandOptionsDto.stringify(dto)) //
        .withCallback(201, callback).withCallback(400, callback).withCallback(500, callback).send();
  }

  private ImportCommandOptionsDto createLimesurveyImportCommandOptionsDto() {
    return createImportCommandOptionsDto(null);
  }

  private ImportCommandOptionsDto createImportCommandOptionsDto(String selectedFile) {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importData.getDestinationDatasourceName());
    if(importData.isArchiveMove()) {
      dto.setArchive(importData.getArchiveDirectory());
      JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
      selectedFiles.push(selectedFile);
      dto.setFilesArray(selectedFiles);
    }
    if(importData.isIdentifierSharedWithUnit()) {
      dto.setUnit(importData.getUnit());
      dto.setForce(false);
    } else {
      dto.setForce(true);
    }
    JsArrayString selectedTables = JavaScriptObject.createArray().cast();
    for(String tableName : comparedDatasourcesReportPresenter.getSelectedTables()) {
      selectedTables.push(importData.getTransientDatasourceName() + "." + tableName);
    }
    dto.setTablesArray(selectedTables);
    return dto;
  }

  //
  // Inner classes and interfaces
  //

  private final class SubmitJobResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      if(response.getStatusCode() == 201) {
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
        if(getView().getImportFormat().equals(ImportFormat.CSV)) {
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
      if(comparedDatasourcesReportPresenter.getSelectedTables().size() == 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("TableSelectionIsRequired").build());
        return false;
      }
      datasourceValuesStepPresenter
          .setDatasource(transientDatasourceHandler.getImportData().getTransientDatasourceName(),
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

  private final class TransientDatasourceHandler implements StepInHandler {

    private ImportData importData;

    private Request transientRequest;

    private Request diffRequest;

    public ImportData getImportData() {
      return importData;
    }

    @Override
    public void onStepIn() {
      comparedDatasourcesReportPresenter.getDisplay().clearDisplay();
      removeTransientDatasource();
      importData = formatStepPresenter.getImportData();
      destinationSelectionStepPresenter.updateImportData(importData);
      unitSelectionStepPresenter.updateImportData(importData);
      createTransientDatasource();
    }

    private void removeTransientDatasource() {
      if(importData == null) return;
      if(transientRequest != null) {
        transientRequest.cancel();
        transientRequest = null;
      }
      if(diffRequest != null) {
        diffRequest.cancel();
        diffRequest = null;
      }

      deleteTransientDatasource();
      importData = null;
    }

    private void deleteTransientDatasource() {
      if(importData.getTransientDatasourceName() == null) return;

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          // ignore
        }
      };
      //TODO use uribuilder
      ResourceRequestBuilderFactory.newBuilder().forResource("/datasource/" + importData.getTransientDatasourceName())
          .delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler)
          .withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }

    private void createTransientDatasource() {
      getView().prepareDatasourceCreation();

      final DatasourceFactoryDto factory = DatasourceDtos.createDatasourceFactoryDto(importData);

      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        public void onResponseCode(Request request, Response response) {
          transientRequest = null;
          if(response.getStatusCode() == 201) {
            DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
            importData.setTransientDatasourceName(datasourceDto.getName());
            datasourceDiff(factory, datasourceDto);
          } else {
            getView().showDatasourceCreationError((ClientErrorDto) JsonUtils.unsafeEval(response.getText()));
          }
        }
      };

      transientRequest = ResourceRequestBuilderFactory.<DatasourceFactoryDto>newBuilder()
          .forResource("/transient-datasources").post()//
          .withResourceBody(DatasourceFactoryDto.stringify(factory))//
          .withCallback(201, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler)
          .send();
    }

    private void datasourceDiff(final DatasourceFactoryDto factory, final DatasourceDto datasourceDto) {
      diffRequest = comparedDatasourcesReportPresenter.compare(importData.getTransientDatasourceName(), //
          importData.getDestinationDatasourceName(), new DatasourceCreatedCallback() {

        @Override
        public void onSuccess(DatasourceFactoryDto factory, DatasourceDto datasource) {
          getView().showDatasourceCreationSuccess();
        }

        @Override
        public void onFailure(DatasourceFactoryDto factory, ClientErrorDto error) {
          getView().showDatasourceCreationError(error);
        }
      },//
          factory, datasourceDto);
      getView().showDatasourceCreationSuccess();
    }
  }

  public interface Display extends WizardView {

    enum Slots {
      Destination, Unit, Values, Archive
    }

    ImportFormat getImportFormat();

    void setImportDataInputsHandler(ImportDataInputsHandler handler);

    void prepareDatasourceCreation();

    void showDatasourceCreationSuccess();

    void showDatasourceCreationError(ClientErrorDto errorDto);

    public void setComparedDatasourcesReportStepInHandler(StepInHandler handler);

    void setComparedDatasourcesReportDisplay(WizardStepDisplay display);

    HandlerRegistration addFormatChangeHandler(ChangeHandler handler);

    void setFormatStepDisplay(WizardStepDisplay display);

  }

  public interface DataImportFormatStepPresenter {

    /**
     * Get the import data as collected.
     *
     * @return
     */
    public ImportData getImportData();

    /**
     * Validate the import data were correctly provided, and send notification error messages if any.
     *
     * @return
     */
    public boolean validate();

  }

}
