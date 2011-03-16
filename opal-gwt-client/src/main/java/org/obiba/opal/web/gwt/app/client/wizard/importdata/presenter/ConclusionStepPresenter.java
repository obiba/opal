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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createdatasource.presenter.DatasourceCreatedCallback;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.ConflictDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.CsvDatasourceTableBundleDto;
import org.obiba.opal.web.model.client.magma.DatasourceCompareDto;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.DatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.DatasourceParsingErrorDto.ClientErrorDtoExtensions;
import org.obiba.opal.web.model.client.magma.FsDatasourceFactoryDto;
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.ImportCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ConclusionStepPresenter extends WidgetPresenter<ConclusionStepPresenter.Display> {

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  private JobListPresenter jobListPresenter;

  @Inject
  private ImportData importData;

  private DatasourceCreatedCallback transientDatasourceCreatedCallback;

  @Inject
  public ConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    getDisplay().showJobId(importData.getJobId());
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addJobLinkClickHandler(new JobLinkClickHandler()));
  }

  public void setTransientDatasourceCreatedCallback(DatasourceCreatedCallback callback) {
    this.transientDatasourceCreatedCallback = callback;
  }

  public void reset() {
    getDisplay().hideErrors();
  }

  public void launchImport(ImportData importData) {
    // TODO Auto-generated method stub
    this.importData = importData;
    createTransientDatasource();
  }

  private void createTransientDatasource() {

    final DatasourceFactoryDto factory = createDatasourceFactoryDto(importData);

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
          importData.setTransientDatasourceName(datasourceDto.getName());
          datasourceDiff(factory, datasourceDto);
        } else {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
            getDisplay().showDatasourceParsingErrors(errorDto);
          } else {
            eventBus.fireEvent(NotificationEvent.newBuilder().error("fileReadError").build());
          }
          if(transientDatasourceCreatedCallback != null) transientDatasourceCreatedCallback.onFailure(factory, errorDto);
        }
      }
    };

    ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource("/transient-datasources").post().withResourceBody(DatasourceFactoryDto.stringify(factory)).withCallback(201, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler).send();
  }

  public static DatasourceFactoryDto createDatasourceFactoryDto(ImportData importData) {
    if(importData.getImportFormat().equals(ImportFormat.CSV)) {
      return createCSVDatasourceFactoryDto(importData);
    } else if(importData.getImportFormat().equals(ImportFormat.XML)) {
      return createXMLDatasourceFactoryDto(importData);
    } else
      throw new IllegalArgumentException("Import data format not supported: " + importData.getImportFormat());
  }

  private static DatasourceFactoryDto createCSVDatasourceFactoryDto(ImportData importData) {

    CsvDatasourceTableBundleDto csvDatasourceTableBundleDto = CsvDatasourceTableBundleDto.create();
    csvDatasourceTableBundleDto.setName(importData.getDestinationTableName());
    csvDatasourceTableBundleDto.setData(importData.getCsvFile());
    if(importData.getDestinationDatasourceName() != null) {
      csvDatasourceTableBundleDto.setRefTable(importData.getDestinationDatasourceName() + "." + importData.getDestinationTableName());
    }

    @SuppressWarnings("unchecked")
    JsArray<CsvDatasourceTableBundleDto> tables = (JsArray<CsvDatasourceTableBundleDto>) JsArray.createArray();
    tables.push(csvDatasourceTableBundleDto);

    CsvDatasourceFactoryDto csvDatasourceFactoryDto = CsvDatasourceFactoryDto.create();
    csvDatasourceFactoryDto.setCharacterSet(importData.getCharacterSet());
    csvDatasourceFactoryDto.setFirstRow(importData.getRow());
    csvDatasourceFactoryDto.setQuote(importData.getQuote());
    csvDatasourceFactoryDto.setSeparator(importData.getField());
    csvDatasourceFactoryDto.setTablesArray(tables);

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(CsvDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, csvDatasourceFactoryDto);

    return dto;
  }

  private static DatasourceFactoryDto createXMLDatasourceFactoryDto(ImportData importData) {

    FsDatasourceFactoryDto fsDatasourceFactoryDto = FsDatasourceFactoryDto.create();
    fsDatasourceFactoryDto.setFile(importData.getXmlFile());
    fsDatasourceFactoryDto.setUnit(importData.getUnit());

    DatasourceFactoryDto dto = DatasourceFactoryDto.create();
    dto.setExtension(FsDatasourceFactoryDto.DatasourceFactoryDtoExtensions.params, fsDatasourceFactoryDto);

    return dto;
  }

  private void datasourceDiff(final DatasourceFactoryDto factory, final DatasourceDto datasourceDto) {
    ResourceRequestBuilderFactory.<DatasourceCompareDto> newBuilder().forResource("/datasource/" + importData.getTransientDatasourceName() + "/compare/" + importData.getDestinationDatasourceName()).get().withCallback(new ResourceCallback<DatasourceCompareDto>() {

      @Override
      public void onResource(Response response, DatasourceCompareDto resource) {
        JsArray<TableCompareDto> comparedTables = resource.getTableComparisonsArray();
        List<TableCompareError> validationErrors = new ArrayList<TableCompareError>();
        for(int i = 0; i < comparedTables.length(); i++) {
          TableCompareDto tableComparison = comparedTables.get(i);
          collectValidationErrors(tableComparison, validationErrors);
        }
        if(validationErrors.isEmpty()) {
          transientDatasourceCreatedCallback.onSuccess(factory, datasourceDto);
          // TODO launch job
          submitJob();
        } else {
          getDisplay().showTableCompareErrors(validationErrors);
          transientDatasourceCreatedCallback.onFailure(factory, null);
        }
      }
    }).send();
  }

  private void collectValidationErrors(TableCompareDto tableComparison, List<TableCompareError> validationErrors) {
    // collectMissingVariableValidationErrors(tableComparison); // TODO Re-enable as part of OPAL-712.
    collectConflictValidationErrors(tableComparison, validationErrors);
  }

  @SuppressWarnings("unused")
  private void collectMissingVariableValidationErrors(TableCompareDto tableComparison, List<TableCompareError> validationErrors) {
    if(tableComparison.getNewVariablesArray() != null) {
      for(int i = 0; i < tableComparison.getNewVariablesArray().length(); i++) {
        VariableDto variableDto = tableComparison.getNewVariablesArray().get(i);
        validationErrors.add(new TableCompareError(variableDto.getName(), translations.datasourceComparisonErrorMap().get("VariablePresentInSourceButNotDestination")));
      }
    }
  }

  private void collectConflictValidationErrors(TableCompareDto tableComparison, List<TableCompareError> validationErrors) {
    if(tableComparison.getConflictsArray() != null) {
      for(int i = 0; i < tableComparison.getConflictsArray().length(); i++) {
        ConflictDto conflictDto = tableComparison.getConflictsArray().get(i);
        validationErrors.add(new TableCompareError(conflictDto.getVariable().getName(), translations.datasourceComparisonErrorMap().get(conflictDto.getCode())));
      }
    }
  }

  private void submitJob() {
    if(importData.getImportFormat().equals(ImportFormat.XML)) {
      submitJob(createXmlImportCommandOptionsDto());
    } else if(importData.getImportFormat().equals(ImportFormat.CSV)) {
      submitJob(createCsvImportCommandOptionsDto());
    }
  }

  private void submitJob(ImportCommandOptionsDto dto) {
    ResourceRequestBuilderFactory.newBuilder().forResource("/shell/import").post() //
    .withResourceBody(ImportCommandOptionsDto.stringify(dto)) //
    .withCallback(400, new ClientFailureResponseCodeCallBack()) //
    .withCallback(201, new SuccessResponseCodeCallBack()).send();
  }

  private ImportCommandOptionsDto createXmlImportCommandOptionsDto() {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importData.getDestinationDatasourceName());
    dto.setArchive(importData.getArchiveDirectory());
    JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
    selectedFiles.push(importData.getXmlFile());
    dto.setFilesArray(selectedFiles);
    if(importData.isIdentifierSharedWithUnit()) dto.setUnit(importData.getUnit());
    return dto;
  }

  private ImportCommandOptionsDto createCsvImportCommandOptionsDto() {
    ImportCommandOptionsDto dto = ImportCommandOptionsDto.create();
    dto.setDestination(importData.getDestinationDatasourceName());
    if(importData.isArchiveMove()) {
      dto.setArchive(importData.getArchiveDirectory());
      JsArrayString selectedFiles = JavaScriptObject.createArray().cast();
      selectedFiles.push(importData.getCsvFile());
      dto.setFilesArray(selectedFiles);
    }
    if(importData.isIdentifierSharedWithUnit()) dto.setUnit(importData.getUnit());
    dto.setSource(importData.getTransientDatasourceName());
    return dto;
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      importData.setJobId(jobId);
      getDisplay().showJobId(jobId);
    }
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  //
  // Inner classes
  //

  class JobLinkClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(WorkbenchChangeEvent.newBuilder(jobListPresenter).forResource("/shell/commands").build());
    }

  }

  public static class TableCompareError {

    private final String column;

    private final String errorMessageKey;

    public TableCompareError(String column, String errorMessageKey) {
      super();
      this.column = column;
      this.errorMessageKey = errorMessageKey;
    }

    public String getColumn() {
      return column;
    }

    public String getErrorMessageKey() {
      return errorMessageKey;
    }

  }

  //
  // Interfaces
  //

  public interface Display extends WidgetDisplay {

    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    void showJobId(String text);

    public void showTableCompareErrors(final List<TableCompareError> errors);

    public void showDatasourceParsingErrors(ClientErrorDto errorDto);

    public void hideErrors();

  }

}
