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

import org.obiba.opal.web.gwt.app.client.event.UserMessageEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ErrorDialogPresenter.MessageDialogType;
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
import org.obiba.opal.web.model.client.magma.TableCompareDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;
import org.obiba.opal.web.model.client.ws.DatasourceParsingErrorDto;
import org.obiba.opal.web.model.client.ws.DatasourceParsingErrorDto.ClientErrorDtoExtensions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DestinationSelectionStepPresenter extends WidgetPresenter<DestinationSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    void setDatasources(JsArray<DatasourceDto> datasources);

    String getSelectedDatasource();

    boolean hasTable();

    String getSelectedTable();

    void hideTables();

    void showTables();

  }

  private List<CsvValidationError> validationErrors = new ArrayList<CsvValidationError>();

  @Inject
  private ImportData importData;

  @Inject
  private ValidationReportStepPresenter validationReportStepPresenter;

  @Inject
  private IdentityArchiveStepPresenter identityArchiveStepPresenter;

  private static Translations translations = GWT.create(Translations.class);

  @Inject
  public DestinationSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    hideShowTables();

    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();
  }

  private void hideShowTables() {
    if(importData.getImportFormat().equals(ImportFormat.XML)) {
      getDisplay().hideTables();
    } else {
      getDisplay().showTables();
    }
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
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

  class NextClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      importData.setDestinationDatasourceName(getDisplay().getSelectedDatasource());
      if(getDisplay().hasTable()) importData.setDestinationTableName(getDisplay().getSelectedTable());

      if(importData.getImportFormat().equals(ImportFormat.CSV)) {
        createTransientCsvDatasource();
      }
      if(importData.getImportFormat().equals(ImportFormat.XML)) {
        eventBus.fireEvent(new WorkbenchChangeEvent(identityArchiveStepPresenter));
      }
    }

  }

  private void datasourceDiff() {
    validationErrors.clear();
    ResourceRequestBuilderFactory.<DatasourceCompareDto> newBuilder().forResource("/datasource/" + importData.getTransientDatasourceName() + "/compare/" + importData.getDestinationDatasourceName()).get().withCallback(new ResourceCallback<DatasourceCompareDto>() {

      @Override
      public void onResource(Response response, DatasourceCompareDto resource) {
        JsArray<TableCompareDto> comparedTables = resource.getTableComparisonsArray();
        for(int i = 0; i < comparedTables.length(); i++) {
          TableCompareDto tableComparison = comparedTables.get(i);
          collectValidationErrors(tableComparison);
        }
        moveToNextStepAfterDatasourceDiff();
      }
    }).send();
  }

  private void collectValidationErrors(TableCompareDto tableComparison) {
    collectMissingVariableValidationErrors(tableComparison);
    collectConflictValidationErrors(tableComparison);
  }

  private void collectMissingVariableValidationErrors(TableCompareDto tableComparison) {
    if(tableComparison.getNewVariablesArray() != null) {
      for(int i = 0; i < tableComparison.getNewVariablesArray().length(); i++) {
        VariableDto variableDto = tableComparison.getNewVariablesArray().get(i);
        validationErrors.add(new CsvValidationError(variableDto.getName(), translations.datasourceComparisonErrorMap().get("VariablePresentInSourceButNotDestination")));
      }
    }
  }

  private void collectConflictValidationErrors(TableCompareDto tableComparison) {
    if(tableComparison.getConflictsArray() != null) {
      for(int i = 0; i < tableComparison.getConflictsArray().length(); i++) {
        ConflictDto conflictDto = tableComparison.getConflictsArray().get(i);
        validationErrors.add(new CsvValidationError(conflictDto.getVariable().getName(), translations.datasourceComparisonErrorMap().get(conflictDto.getCode())));
      }
    }
  }

  protected void moveToNextStepAfterDatasourceDiff() {
    if(validationErrors.isEmpty()) {
      eventBus.fireEvent(new WorkbenchChangeEvent(identityArchiveStepPresenter));
    } else {
      validationReportStepPresenter.getDisplay().setValidationErrors(validationErrors);
      eventBus.fireEvent(new WorkbenchChangeEvent(validationReportStepPresenter));
    }
  }

  public static class CsvValidationError {

    private final String column;

    private final String errorMessageKey;

    public CsvValidationError(String column, String errorMessageKey) {
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

  public void createTransientCsvDatasource() {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == 201) {
          DatasourceDto datasourceDto = (DatasourceDto) JsonUtils.unsafeEval(response.getText());
          importData.setTransientDatasourceName(datasourceDto.getName());
          datasourceDiff();
        } else {
          final ClientErrorDto errorDto = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
          if(errorDto.getExtension(ClientErrorDtoExtensions.errors) != null) {
            validationReportStepPresenter.getDisplay().setParsingErrors(extractDatasourceParsingErrors(errorDto));
            eventBus.fireEvent(new WorkbenchChangeEvent(validationReportStepPresenter));
          } else {
            eventBus.fireEvent(new UserMessageEvent(MessageDialogType.ERROR, "fileReadError", null));
          }
        }
      }
    };

    DatasourceFactoryDto dto = createDatasourceFactoryDto();
    ResourceRequestBuilderFactory.<DatasourceFactoryDto> newBuilder().forResource("/datasources").post().withResourceBody(DatasourceFactoryDto.stringify(dto)).withCallback(201, callbackHandler).withCallback(400, callbackHandler).withCallback(500, callbackHandler).send();
  }

  @SuppressWarnings("unchecked")
  private List<DatasourceParsingErrorDto> extractDatasourceParsingErrors(ClientErrorDto dto) {
    List<DatasourceParsingErrorDto> datasourceParsingErrors = new ArrayList<DatasourceParsingErrorDto>();

    JsArray<DatasourceParsingErrorDto> errors = (JsArray<DatasourceParsingErrorDto>) dto.getExtension(ClientErrorDtoExtensions.errors);
    if(errors != null) {
      for(int i = 0; i < errors.length(); i++) {
        datasourceParsingErrors.add(errors.get(i));
      }
    }

    return datasourceParsingErrors;
  }

  private DatasourceFactoryDto createDatasourceFactoryDto() {

    CsvDatasourceTableBundleDto csvDatasourceTableBundleDto = CsvDatasourceTableBundleDto.create();
    csvDatasourceTableBundleDto.setName(importData.getDestinationTableName());
    csvDatasourceTableBundleDto.setData(importData.getCsvFile());
    csvDatasourceTableBundleDto.setRefTable(importData.getDestinationDatasourceName() + "." + importData.getDestinationTableName());

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
}
