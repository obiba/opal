/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.job.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableListUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DataExportPresenter extends WidgetPresenter<DataExportPresenter.Display> {

  @Inject
  private TableListPresenter tableListPresenter;

  @Inject
  private FileSelectionPresenter fileSelectionPresenter;

  @Inject
  private JobListPresenter jobListPresenter;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public DataExportPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  public DataExportPresenter(Display display, EventBus eventBus, TableListPresenter tableListPresenter, FileSelectionPresenter fileSelectionPresenter) {
    this(display, eventBus);
    this.tableListPresenter = tableListPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
  }

  protected void initDisplayComponents() {
    tableListPresenter.bind();
    getDisplay().setTableWidgetDisplay(tableListPresenter.getDisplay());

    initFileSelectionType();
    fileSelectionPresenter.bind();
    super.registerHandler(getDisplay().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getDisplay().addCloseClickHandler(new FinishClickHandler()));
    super.registerHandler(getDisplay().addSubmitClickHandler(new SubmitClickHandler()));
    super.registerHandler(getDisplay().addJobLinkClickHandler(new JobLinkClickHandler(eventBus, jobListPresenter)));
    super.registerHandler(getDisplay().addFileFormatChangeHandler(new FileFormatChangeHandler()));
    super.registerHandler(eventBus.addHandler(TableListUpdateEvent.getType(), new TablesToExportChangedHandler()));
    getDisplay().setFileWidgetDisplay(fileSelectionPresenter.getDisplay());
    getDisplay().setTablesValidator(new TablesValidator());
    getDisplay().setDestinationValidator(new DestinationValidator());

  }

  private void initFileSelectionType() {
    if(getDisplay().getFileFormat().equalsIgnoreCase("csv")) {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE_OR_FOLDER);
    } else {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
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
    tableListPresenter.clear();
    getDisplay().showDialog();
  }

  private void initDatasourcesAndUnits() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        if(datasources != null && datasources.length() > 0) {
          getDisplay().setDatasources(filterDatasources(datasources));
          initUnits();
        } else {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "NoDataToExport", null));
        }
      }
    }).send();
  }

  private List<DatasourceDto> filterDatasources(JsArray<DatasourceDto> datasources) {

    List<DatasourceDto> filteredDatasources = new ArrayList<DatasourceDto>();
    Set<String> originDatasourceName = getOriginDatasourceNames();
    for(DatasourceDto datasource : JsArrays.toList(datasources)) {
      if(!originDatasourceName.contains(datasource.getName())) {
        filteredDatasources.add(datasource);
      }
    }
    return filteredDatasources;
  }

  private Set<String> getOriginDatasourceNames() {
    Set<String> originDatasourceNames = new HashSet<String>();
    for(TableDto table : tableListPresenter.getTables()) {
      originDatasourceNames.add(table.getDatasourceName());
    }
    return originDatasourceNames;
  }

  private void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

  //
  // Interfaces and classes
  //

  private final class DestinationValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      List<String> errors = formValidationErrors();
      if(errors.size() > 0) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, errors, null));
        return false;
      }
      return true;
    }

    private List<String> formValidationErrors() {
      List<String> result = new ArrayList<String>();

      if(getDisplay().isDestinationFile()) {
        String filename = getDisplay().getOutFile();
        if(filename == null || filename.equals("")) {
          result.add("DestinationFileIsMissing");
        }
        if(getDisplay().getFileFormat().equalsIgnoreCase("csv") && fileSelectionPresenter.getFileTypeSelected() != null && fileSelectionPresenter.getFileTypeSelected().equals(FileSelectionType.FILE)) {
          if(getDisplay().isWithVariables()) {
            result.add("SelectCSVDirectoryOrDoNotExportVariables");
            // result.add("Variables and data cannot be exported in the same CSV file. Select a directory instead or do not export variables.");
          }
          if(tableListPresenter.getTables().size() > 1) {
            result.add("SelectCSVDirectoryOrExportOneTable");
            // result.add("Several tables cannot be exported in the same CSV file. Select a directory instead or export only one table.");
          }
        }
      }
      return result;
    }
  }

  private final class TablesValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(tableListPresenter.getTables().size() == 0) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, "ExportDataMissingTables", null));
        return false;
      }
      return true;
    }
  }

  class FileFormatChangeHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent arg0) {
      initFileSelectionType();
      fileSelectionPresenter.getDisplay().clearFile();
    }
  }

  class SubmitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getDisplay().renderPendingConclusion();
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/copy").post() //
      .withResourceBody(CopyCommandOptionsDto.stringify(createCopycommandOptions())) //
      .withCallback(400, new ClientFailureResponseCodeCallBack()) //
      .withCallback(201, new SuccessResponseCodeCallBack()).send();
    }

    private CopyCommandOptionsDto createCopycommandOptions() {
      CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

      JsArrayString selectedTables = JavaScriptObject.createArray().cast();
      for(TableDto table : tableListPresenter.getTables()) {
        selectedTables.push(table.getDatasourceName() + "." + table.getName());
      }

      dto.setTablesArray(selectedTables);
      if(getDisplay().isDestinationDataSource()) {
        dto.setDestination(getDisplay().getSelectedDatasource());
      } else {
        dto.setFormat(getDisplay().getFileFormat());
        dto.setOut(getDisplay().getOutFile());
      }
      dto.setNonIncremental(!getDisplay().isIncremental());
      dto.setNoVariables(!getDisplay().isWithVariables());
      if(getDisplay().isUseAlias()) dto.setTransform("attribute('alias').isNull().value ? name() : attribute('alias')");
      if(getDisplay().isUnitId()) dto.setUnit(getDisplay().getSelectedUnit());

      return dto;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
      getDisplay().renderFailedConclusion();
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      getDisplay().renderCompletedConclusion(jobId);
    }
  }

  static class JobLinkClickHandler implements ClickHandler {

    private final EventBus eventBus;

    private final JobListPresenter jobListPresenter;

    public JobLinkClickHandler(EventBus eventBus, JobListPresenter jobListPresenter) {
      super();
      this.eventBus = eventBus;
      this.jobListPresenter = jobListPresenter;
    }

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(jobListPresenter));
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getDisplay().hideDialog();
    }
  }

  public class TablesToExportChangedHandler implements TableListUpdateEvent.Handler {

    @Override
    public void onTableListUpdate(TableListUpdateEvent event) {
      initDatasourcesAndUnits();
    }
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    void setTablesValidator(ValidationHandler validationHandler);

    void setDestinationValidator(ValidationHandler handler);

    /** Set a collection of Opal datasources retrieved from Opal. */
    void setDatasources(List<DatasourceDto> datasources);

    /** Get the Opal datasource selected by the user. */
    String getSelectedDatasource();

    /** Set a collection of Opal units retrieved from Opal. */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /** Get the Opal unit selected by the user. */
    String getSelectedUnit();

    /** Get the form submit button. */
    HandlerRegistration addSubmitClickHandler(ClickHandler handler);

    /** Display the conclusion step */
    void renderCompletedConclusion(String jobId);

    void renderFailedConclusion();

    void renderPendingConclusion();

    /** Add a handler to the job list */
    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    boolean isDestinationFile();

    String getOutFile();

    String getFileFormat();

    HandlerRegistration addDestinationFileClickHandler(ClickHandler handler);

    HandlerRegistration addDestinationDatasourceClickHandler(ClickHandler handler);

    HandlerRegistration addFileFormatChangeHandler(ChangeHandler handler);

    HandlerRegistration addWithVariablesClickHandler(ClickHandler handler);

    boolean isIncremental();

    boolean isWithVariables();

    boolean isUseAlias();

    boolean isUnitId();

    boolean isDestinationDataSource();

    void setTableWidgetDisplay(TableListPresenter.Display display);

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

  }

}
