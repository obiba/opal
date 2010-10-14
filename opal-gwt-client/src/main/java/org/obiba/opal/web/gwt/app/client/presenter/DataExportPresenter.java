/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
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
    super.registerHandler(getDisplay().addSubmitClickHandler(new SubmitClickHandler()));
    super.registerHandler(getDisplay().addJobLinkClickHandler(new DataCommonPresenter.JobLinkClickHandler(eventBus, jobListPresenter)));
    super.registerHandler(getDisplay().addFileFormatChangeHandler(new FileFormatChangeHandler()));
    getDisplay().setFileWidgetDisplay(fileSelectionPresenter.getDisplay());

    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        getDisplay().setDatasources(datasources);
      }
    }).send();

    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

  private void initFileSelectionType() {
    if(getDisplay().getFileFormat().equalsIgnoreCase("csv")) {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE_OR_FOLDER);
    } else {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    }
  }

  private void displayMessages(NotificationType type, List<String> messages) {
    eventBus.fireEvent(new NotificationEvent(type, messages, null));
  }

  private List<String> formValidationErrors() {
    List<String> result = new ArrayList<String>();
    if(tableListPresenter.getTables().size() == 0) {
      result.add("At least one table must be selected for export.");
    }
    if(getDisplay().isDestinationFile()) {
      String filename = getDisplay().getOutFile();
      if(filename == null || filename.equals("")) {
        result.add("File name cannot be empty.");
      }
      if(getDisplay().getFileFormat().equalsIgnoreCase("csv") && fileSelectionPresenter.getFileTypeSelected() != null && fileSelectionPresenter.getFileTypeSelected().equals(FileSelectionType.FILE)) {
        if(getDisplay().isWithVariables()) {
          result.add("Variables and data cannot be exported in the same CSV file. Select a directory instead or do not export variables.");
        }
        if(tableListPresenter.getTables().size() > 1) {
          result.add("Several tables cannot be exported in the same CSV file. Select a directory instead or export only one table.");
        }
      }
    }
    return result;
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
  // Interfaces and classes
  //

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
      List<String> errors = formValidationErrors();
      if(!errors.isEmpty()) {
        displayMessages(NotificationType.ERROR, errors);
      } else {
        ResourceRequestBuilderFactory.newBuilder().forResource("/shell/copy").post() //
        .withResourceBody(CopyCommandOptionsDto.stringify(createCopycommandOptions())) //
        .withCallback(400, new ClientFailureResponseCodeCallBack()) //
        .withCallback(201, new SuccessResponseCodeCallBack()).send();
      }
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
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);

      getDisplay().renderConclusionStep(jobId);
    }
  }

  public interface Display extends DataCommonPresenter.Display {

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

  }

}
