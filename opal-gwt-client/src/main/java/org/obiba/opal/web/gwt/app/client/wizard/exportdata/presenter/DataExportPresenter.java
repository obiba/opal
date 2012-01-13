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
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableListUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.PopupView;

public class DataExportPresenter extends WizardPresenterWidget<DataExportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  public static class Wizard extends WizardProxy<DataExportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataExportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final TableListPresenter tableListPresenter;

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private TableDto table;

  protected String identifierEntityType;

  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, TableListPresenter tableListPresenter, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.tableListPresenter = tableListPresenter;
    this.fileSelectionPresenter = fileSelectionPresenter;
  }

  @Override
  protected void onBind() {
    initDisplayComponents();
    tableListPresenter.clear();
    if(datasourceName != null) {
      tableListPresenter.selectDatasourceTables(datasourceName);
    } else if(table != null) {
      tableListPresenter.selectTable(table);
    }
  }

  protected void initDisplayComponents() {
    tableListPresenter.bind();
    getView().setTableWidgetDisplay(tableListPresenter.getDisplay());

    initFileSelectionType();
    fileSelectionPresenter.bind();
    super.registerHandler(getView().addCancelClickHandler(new CancelClickHandler()));
    super.registerHandler(getView().addCloseClickHandler(new FinishClickHandler()));
    super.registerHandler(getView().addSubmitClickHandler(new SubmitClickHandler()));
    super.registerHandler(getView().addJobLinkClickHandler(new JobLinkClickHandler()));
    super.registerHandler(getView().addFileFormatChangeHandler(new FileFormatChangeHandler()));
    super.registerHandler(getEventBus().addHandler(TableListUpdateEvent.getType(), new TablesToExportChangedHandler()));
    getView().setFileWidgetDisplay(fileSelectionPresenter.getDisplay());
    getView().setTablesValidator(new TablesValidator());
    getView().setDestinationValidator(new DestinationValidator());
  }

  private void initFileSelectionType() {
    if(getView().getFileFormat().equalsIgnoreCase("csv")) {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
    } else {
      fileSelectionPresenter.setFileSelectionType(FileSelectionType.FILE);
    }
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    tableListPresenter.unbind();
    fileSelectionPresenter.unbind();
    datasourceName = null;
    table = null;
  }

  @Override
  public void onReveal() {
    initUnits();
  }

  private void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getView().setUnits(units);
      }
    }).send();
    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/functional-units/entities/table").get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        identifierEntityType = resource.getEntityType();
      }
    }).send();
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length != 0) {
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else if(event.getEventParameters()[0] instanceof TableDto) {
        table = (TableDto) event.getEventParameters()[0];
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
    }
  }

  //
  // Interfaces and classes
  //

  private final class DestinationValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      List<String> errors = formValidationErrors();
      if(errors.size() > 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(errors).build());
        return false;
      }
      return true;
    }

    private List<String> formValidationErrors() {
      List<String> result = new ArrayList<String>();

      String filename = getView().getOutFile();
      if(filename == null || filename.equals("")) {
        result.add("DestinationFileIsMissing");
      } else if(getView().getFileFormat().equalsIgnoreCase("excel") && !filename.endsWith(".xls") && !filename.endsWith(".xlsx")) {
        result.add("ExcelFileSuffixInvalid");
      } else if(getView().getFileFormat().equalsIgnoreCase("xml") && !filename.endsWith(".zip")) {
        result.add("ZipFileSuffixInvalid");
      }
      return result;
    }
  }

  private final class TablesValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(tableListPresenter.getTables().size() == 0) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("ExportDataMissingTables").build());
        return false;
      } else {
        boolean identifierEntityTable = false;
        for(TableDto dto : tableListPresenter.getTables()) {
          if(dto.getEntityType().equals(identifierEntityType)) {
            identifierEntityTable = true;
            break;
          }
        }
        getView().renderUnitSelection(identifierEntityTable);
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
      getView().renderPendingConclusion();
      ResourceRequestBuilderFactory.newBuilder().forResource("/shell/copy").post() //
      .withResourceBody(CopyCommandOptionsDto.stringify(createCopycommandOptions())) //
      .withCallback(400, new ClientFailureResponseCodeCallBack()) //
      .withCallback(201, new SuccessResponseCodeCallBack()).send();
    }

    private CopyCommandOptionsDto createCopycommandOptions() {
      CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

      JsArrayString selectedTables = JavaScriptObject.createArray().cast();
      if(table != null) {
        selectedTables.push(table.getDatasourceName() + "." + table.getName());
      } else {
        for(TableDto table : tableListPresenter.getTables()) {
          selectedTables.push(table.getDatasourceName() + "." + table.getName());
        }
      }

      dto.setTablesArray(selectedTables);
      dto.setFormat(getView().getFileFormat());
      dto.setOut(getView().getOutFile());
      dto.setNonIncremental(!getView().isIncremental());
      dto.setNoVariables(!getView().isWithVariables());
      if(getView().isUseAlias()) dto.setTransform("attribute('alias').isNull().value ? name() : attribute('alias')");
      if(getView().isUnitId()) dto.setUnit(getView().getSelectedUnit());

      return dto;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      getView().renderFailedConclusion();
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      getView().renderCompletedConclusion(jobId);
    }
  }

  class JobLinkClickHandler implements ClickHandler {

    public JobLinkClickHandler() {
      super();
    }

    @Override
    public void onClick(ClickEvent arg0) {
      getEventBus().fireEvent(new PlaceChangeEvent(Places.jobsPlace));
    }
  }

  class CancelClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getView().hide();
    }
  }

  class FinishClickHandler implements ClickHandler {

    public void onClick(ClickEvent arg0) {
      getView().hide();
    }
  }

  public class TablesToExportChangedHandler implements TableListUpdateEvent.Handler {

    @Override
    public void onTableListUpdate(TableListUpdateEvent event) {
      initUnits();
    }
  }

  public interface Display extends PopupView {

    void renderUnitSelection(boolean identifierEntityTable);

    void setTablesValidator(ValidationHandler validationHandler);

    void setDestinationValidator(ValidationHandler handler);

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

    String getOutFile();

    String getFileFormat();

    HandlerRegistration addFileFormatChangeHandler(ChangeHandler handler);

    boolean isIncremental();

    boolean isWithVariables();

    boolean isUseAlias();

    boolean isUnitId();

    void setTableWidgetDisplay(TableListPresenter.Display display);

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    HandlerRegistration addCloseClickHandler(ClickHandler handler);

  }

}
