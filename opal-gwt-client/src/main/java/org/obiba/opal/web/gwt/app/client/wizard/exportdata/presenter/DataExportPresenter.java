/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.FileSelectorPresenter.FileSelectionType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardPresenterWidget;
import org.obiba.opal.web.gwt.app.client.wizard.WizardProxy;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.WizardView;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DataExportPresenter extends WizardPresenterWidget<DataExportPresenter.Display> {

  public static final WizardType WizardType = new WizardType();

  private final RequestCredentials credentials;

  public static class Wizard extends WizardProxy<DataExportPresenter> {

    @Inject
    protected Wizard(EventBus eventBus, Provider<DataExportPresenter> wizardProvider) {
      super(eventBus, WizardType, wizardProvider);
    }

  }

  private final FileSelectionPresenter fileSelectionPresenter;

  private String datasourceName;

  private TableDto table;

  protected String identifierEntityType;

  @Inject
  public DataExportPresenter(Display display, EventBus eventBus, FileSelectionPresenter fileSelectionPresenter,
      RequestCredentials credentials) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
    this.credentials = credentials;
  }

  @Override
  protected void onBind() {
    super.onBind();
    initDisplayComponents();
  }

  protected void initDisplayComponents() {
    initFileSelectionType();
    fileSelectionPresenter.bind();
    fileSelectionPresenter.getDisplay().setFile("/home/" + credentials.getUsername() + "/export");
    getView().setFileWidgetDisplay(fileSelectionPresenter.getDisplay());
    getView().setTablesValidator(new TablesValidator());
    getView().setDestinationValidator(new DestinationValidator());
  }

  private void initFileSelectionType() {
    fileSelectionPresenter.setFileSelectionType(FileSelectionType.FOLDER);
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    fileSelectionPresenter.unbind();
    datasourceName = null;
    table = null;
  }

  @Override
  public void onReveal() {
    initUnits();
    getView().setUsername(credentials.getUsername());
  }

  private void initUnits() {
    ResponseCodeCallback errorCallback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource("/functional-units").get()
        .withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
          @Override
          public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
            getView().setUnits(units);
          }
        }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
    ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource("/functional-units/entities/table").get()
        .withCallback(new ResourceCallback<TableDto>() {
          @Override
          public void onResource(Response response, TableDto resource) {
            identifierEntityType = resource.getEntityType();
          }
        }).withCallback(Response.SC_FORBIDDEN, errorCallback).send();
  }

  @Override
  public void onWizardRequired(WizardRequiredEvent event) {
    if(event.getEventParameters().length == 0) {
      datasourceName = null;
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource("/datasources/tables").get()
          .withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              getView().addTableSelections(JsArrays.toSafeArray(resource));
            }

          }).send();
    } else {
      //noinspection ChainOfInstanceofChecks
      if(event.getEventParameters()[0] instanceof String) {
        datasourceName = (String) event.getEventParameters()[0];
      } else if(event.getEventParameters()[0] instanceof TableDto) {
        table = (TableDto) event.getEventParameters()[0];
        datasourceName = table.getDatasourceName();
      } else {
        throw new IllegalArgumentException("unexpected event parameter type (expected String)");
      }
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder()
          .forResource("/datasource/" + datasourceName + "/tables").get()
          .withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              getView().addTableSelections(JsArrays.toSafeArray(resource));
              if(table != null) {
                getView().selectTable(table);
              } else {
                getView().selectAllTables();
              }
            }

          }).send();
    }
  }

  @Override
  protected boolean hideOnFinish() {
    return true;
  }

  @Override
  protected void onFinish() {
    super.onFinish();

    UriBuilder uriBuilder = UriBuilder.create();
    if(datasourceName == null) {
      uriBuilder.segment("shell", "copy");
    } else {
      uriBuilder.segment("datasource", datasourceName, "commands", "_copy");
    }
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).post() //
        .withResourceBody(CopyCommandOptionsDto.stringify(createCopyCommandOptions())) //
        .withCallback(Response.SC_BAD_REQUEST, new ClientFailureResponseCodeCallBack()) //
        .withCallback(Response.SC_CREATED, new SuccessResponseCodeCallBack()).send();
  }

  private CopyCommandOptionsDto createCopyCommandOptions() {
    CopyCommandOptionsDto dto = CopyCommandOptionsDto.create();

    JsArrayString selectedTables = JavaScriptObject.createArray().cast();
    if(table != null) {
      selectedTables.push(table.getDatasourceName() + "." + table.getName());
    } else {
      for(TableDto tableDto : getView().getSelectedTables()) {
        selectedTables.push(tableDto.getDatasourceName() + "." + tableDto.getName());
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
      if(filename == null || "".equals(filename)) {
        result.add("DestinationFileIsMissing");
      }
      return result;
    }
  }

  private final class TablesValidator implements ValidationHandler {
    @Override
    public boolean validate() {
      if(getView().getSelectedTables().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("ExportDataMissingTables").build());
        return false;
      }
      // Check for duplicate table names
      Collection<String> namesMemento = new HashSet<String>();
      for(TableDto dto : getView().getSelectedTables()) {
        if(namesMemento.contains(dto.getName())) {
          getEventBus().fireEvent(
              NotificationEvent.newBuilder().error("ExportDataDuplicateTableNames").args(dto.getName()).build());
          return false;
        }
        namesMemento.add(dto.getName());
      }

      boolean identifierEntityTable = false;
      for(TableDto dto : getView().getSelectedTables()) {
        if(dto.getEntityType().equals(identifierEntityType)) {
          identifierEntityTable = true;
          break;
        }
      }
      getView().renderUnitSelection(identifierEntityTable);
      return true;
    }
  }

  class ClientFailureResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
    }
  }

  class SuccessResponseCodeCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      String location = response.getHeader("Location");
      String jobId = location.substring(location.lastIndexOf('/') + 1);
      String destination = getView().getOutFile();
      getEventBus().fireEvent(
          NotificationEvent.newBuilder().info("DataExportationProcessLaunched").args(jobId, destination).build());
    }
  }

  public interface Display extends WizardView {

    void renderUnitSelection(boolean identifierEntityTable);

    void setTablesValidator(ValidationHandler validationHandler);

    void setDestinationValidator(ValidationHandler handler);

    /**
     * Set a collection of Opal units retrieved from Opal.
     */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /**
     * Get the Opal unit selected by the user.
     */
    String getSelectedUnit();

    String getOutFile();

    void addTableSelections(JsArray<TableDto> tables);

    void selectTable(TableDto tableDto);

    void selectAllTables();

    List<TableDto> getSelectedTables();

    String getFileFormat();

    boolean isIncremental();

    boolean isWithVariables();

    boolean isUseAlias();

    boolean isUnitId();

    void setFileWidgetDisplay(FileSelectionPresenter.Display display);

    void setUsername(String username);
  }

}
