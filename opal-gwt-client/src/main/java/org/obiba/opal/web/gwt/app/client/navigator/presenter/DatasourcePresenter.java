/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class DatasourcePresenter extends WidgetPresenter<DatasourcePresenter.Display> {

  private String datasourceName;

  private JsArray<TableDto> tables;

  private JsArray<DatasourceDto> datasources;

  private Runnable removeDatasourceConfirmation;

  @Inject
  private Provider<NavigatorPresenter> navigationPresenter;

  //
  // Constructors
  //

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionHandler()));
    super.registerHandler(eventBus.addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionHandler()));
    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    getDisplay().setExcelDownloadCommand(new ExcelDownloadCommand());
    getDisplay().setExportDataCommand(new ExportDataCommand());
    getDisplay().setCopyDataCommand(new CopyDataCommand());
    getDisplay().setAddUpdateTablesCommand(new AddUpdateTablesCommand());
    getDisplay().setRemoveDatasourceCommand(new RemoveDatasourceCommand());
    getDisplay().setAddViewCommand(new AddViewCommand());
    getDisplay().setNextCommand(new NextCommand());
    getDisplay().setPreviousCommand(new PreviousCommand());
    super.registerHandler(eventBus.addHandler(SiblingTableSelectionEvent.getType(), new SiblingTableSelectionHandler()));
    super.getDisplay().setTableNameFieldUpdater(new TableNameFieldUpdater());
    super.registerHandler(eventBus.addHandler(DatasourceUpdatedEvent.getType(), new DatasourceUpdatedEventHandler()));

    // OPAL-975
    super.registerHandler(eventBus.addHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler()));
  }

  @Override
  protected void onUnbind() {
  }

  private int getTableIndex(String tableName) {
    int tableIndex = 0;
    for(int i = 0; i < tables.length(); i++) {
      if(tables.get(i).getName().equals(tableName)) {
        tableIndex = i;
        break;
      }
    }
    return tableIndex;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  public void refreshDisplay() {
    initDatasources();
  }

  @Override
  public void revealDisplay() {
  }

  private void authorize() {
    // create tables
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasource/" + datasourceName + "/tables").post().authorize(getDisplay().getAddUpdateTablesAuthorizer()).send();
    // create views
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasource/" + datasourceName + "/views").post().authorize(getDisplay().getAddViewAuthorizer()).send();
  }

  private void displayDatasource(DatasourceDto datasourceDto) {
    displayDatasource(datasourceDto, null);
  }

  private void displayDatasource(final DatasourceDto datasourceDto, final TableDto tableDto) {
    if(datasourceName == null || !isCurrentDatasource(datasourceDto)) {
      datasourceName = datasourceDto.getName();
      getDisplay().setDatasource(datasourceDto);
      updateTable(tableDto != null ? tableDto.getName() : null);

      // make sure the list of datasources is initialized before looking for siblings
      if(datasources == null || datasources.length() == 0) {
        ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            datasources = (resource != null) ? resource : (JsArray<DatasourceDto>) JsArray.createArray();
            displayDatasourceSiblings(datasourceDto);
          }

        }).send();
      } else {
        displayDatasourceSiblings(datasourceDto);
      }

    } else if(tableDto != null) {
      selectTable(tableDto.getName());
    } else {
      updateTable(null);
    }
    authorize();
  }

  private void displayDatasourceSiblings(DatasourceDto datasourceDto) {
    int index = getDatasourceIndex(datasourceDto);
    getDisplay().setPreviousName(index > 0 ? datasources.get(index - 1).getName() : null);
    getDisplay().setNextName(index < datasources.length() - 1 ? datasources.get(index + 1).getName() : null);
  }

  private int getDatasourceIndex(DatasourceDto datasourceDto) {
    int index = -1;
    for(int i = 0; i < datasources.length(); i++) {
      if(datasources.get(i).getName().equals(datasourceDto.getName())) {
        index = i;
        break;
      }
    }
    return index;
  }

  private boolean isCurrentDatasource(DatasourceDto datasourceDto) {
    return datasourceDto.getName().equals(datasourceName);
  }

  private void selectTable(String tableName) {
    if(tableName != null) {
      int index = getTableIndex(tableName);
      getDisplay().setTableSelection(tables.get(index), index);
    }
  }

  private void updateTable(final String tableName) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasourceName + "/tables").get().withCallback(new TablesResourceCallback(datasourceName, tableName)).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append("/datasource/").append(datasource).append("/variables/excel").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
  }

  private void addView(String datasource) {
    eventBus.fireEvent(new WizardRequiredEvent(WizardType.CREATE_VIEW, datasource));
  }

  private void removeDatasource(String datasource) {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, response.getText(), null));
        } else {
          eventBus.fireEvent(new WorkbenchChangeEvent(navigationPresenter.get()));
        }
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/datasource/" + datasourceName).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
  }

  private String getPreviousTableName(int index) {
    TableDto previous = null;
    if(index > 0) {
      previous = tables.get(index - 1);
    }
    return previous != null ? previous.getName() : null;
  }

  private String getNextTableName(int index) {
    TableDto next = null;
    if(index < tables.length() - 1) {
      next = tables.get(index + 1);
    }
    return next != null ? next.getName() : null;
  }

  private void initDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        datasources = (resource != null) ? resource : (JsArray<DatasourceDto>) JsArray.createArray();
      }

    }).send();
  }

  //
  // Interfaces and classes
  //

  final class PreviousCommand implements Command {
    @Override
    public void execute() {
      for(int i = 0; i < datasources.length(); i++) {
        if(datasources.get(i).getName().equals(datasourceName)) {
          if(i != 0) {
            eventBus.fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i - 1)));
          }
          break;
        }
      }
    }
  }

  final class NextCommand implements Command {
    @Override
    public void execute() {
      for(int i = 0; i < datasources.length(); i++) {
        if(datasources.get(i).getName().equals(datasourceName)) {
          if(i < datasources.length() - 1) {
            eventBus.fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i + 1)));
          }
          break;
        }
      }
    }
  }

  final class ExcelDownloadCommand implements Command {
    @Override
    public void execute() {
      downloadMetadata(datasourceName);
    }
  }

  final class RemoveDatasourceCommand implements Command {
    @Override
    public void execute() {
      removeDatasourceConfirmation = new Runnable() {
        public void run() {
          removeDatasource(datasourceName);
        }
      };

      eventBus.fireEvent(new ConfirmationRequiredEvent(removeDatasourceConfirmation, "removeDatasource", "confirmRemoveDatasource"));
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(removeDatasourceConfirmation != null && event.getSource().equals(removeDatasourceConfirmation) && event.isConfirmed()) {
        removeDatasourceConfirmation.run();
        removeDatasourceConfirmation = null;
      }
    }
  }

  final class AddViewCommand implements Command {
    @Override
    public void execute() {
      addView(datasourceName);
    }
  }

  final class ExportDataCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new WizardRequiredEvent(WizardType.EXPORT_DATA, datasourceName));
    }
  }

  final class CopyDataCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new WizardRequiredEvent(WizardType.COPY_DATA, datasourceName));
    }
  }

  final class AddUpdateTablesCommand implements Command {
    @Override
    public void execute() {
      eventBus.fireEvent(new WizardRequiredEvent(WizardType.IMPORT_VARIABLES, datasourceName));
    }
  }

  private final class TablesResourceCallback implements ResourceCallback<JsArray<TableDto>> {

    private final String datasourceName;

    private final String selectTableName;

    private TablesResourceCallback(String datasourceName, String selectTableName) {
      this.datasourceName = datasourceName;
      this.selectTableName = selectTableName;
      getDisplay().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<TableDto> resource) {
      if(this.datasourceName.equals(DatasourcePresenter.this.datasourceName)) {
        tables = (resource != null) ? resource : (JsArray<TableDto>) JsArray.createArray();
        getDisplay().renderRows(resource);
        selectTable(selectTableName);
        getDisplay().afterRenderRows();
      }
    }
  }

  class TableNameFieldUpdater implements FieldUpdater<TableDto, String> {
    @Override
    public void update(int index, TableDto tableDto, String value) {
      eventBus.fireEvent(new TableSelectionChangeEvent(DatasourcePresenter.this, tableDto, getPreviousTableName(index), getNextTableName(index)));
    }
  }

  class DatasourceSelectionHandler implements DatasourceSelectionChangeEvent.Handler {
    @Override
    public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
      displayDatasource(event.getSelection());
    }
  }

  class TableSelectionHandler implements TableSelectionChangeEvent.Handler {

    @Override
    public void onTableSelectionChanged(final TableSelectionChangeEvent event) {
      if(!event.getSelection().getDatasourceName().equals(datasourceName)) {
        ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + event.getSelection().getDatasourceName()).get().withCallback(new ResourceCallback<DatasourceDto>() {
          @Override
          public void onResource(Response response, DatasourceDto resource) {
            displayDatasource(resource, event.getSelection());
          }
        }).send();
      } else {
        selectTable(event.getSelection().getName());
      }
    }
  }

  class SiblingTableSelectionHandler implements SiblingTableSelectionEvent.Handler {
    @Override
    public void onSiblingTableSelection(SiblingTableSelectionEvent event) {
      TableDto siblingSelection = event.getCurrentSelection();

      // Look for the table and its position in the list by its name.
      // Having an position of the current variable would be more efficient.
      int siblingIndex = 0;
      int currentTableIndex = getTableIndex(event.getCurrentSelection().getName());
      if(event.getDirection().equals(SiblingTableSelectionEvent.Direction.NEXT) && currentTableIndex < tables.length() - 1) {
        siblingIndex = currentTableIndex + 1;
      } else if(event.getDirection().equals(SiblingTableSelectionEvent.Direction.PREVIOUS) && currentTableIndex != 0) {
        siblingIndex = currentTableIndex - 1;
      } else {
        siblingIndex = currentTableIndex;
      }
      siblingSelection = tables.get(siblingIndex);

      getDisplay().setTableSelection(siblingSelection, siblingIndex);

      eventBus.fireEvent(new TableSelectionChangeEvent(DatasourcePresenter.this, siblingSelection, getPreviousTableName(siblingIndex), getNextTableName(siblingIndex)));
    }
  }

  class SpreadSheetClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      downloadMetadata(datasourceName);
    }
  }

  class PreviousClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      for(int i = 0; i < datasources.length(); i++) {
        if(datasources.get(i).getName().equals(datasourceName)) {
          if(i != 0) {
            eventBus.fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i - 1)));
          }
          break;
        }
      }
    }
  }

  class NextClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      for(int i = 0; i < datasources.length(); i++) {
        if(datasources.get(i).getName().equals(datasourceName)) {
          if(i < datasources.length() - 1) {
            eventBus.fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i + 1)));
          }
          break;
        }
      }
    }
  }

  class DatasourceUpdatedEventHandler implements DatasourceUpdatedEvent.Handler {

    @Override
    public void onDatasourceUpdated(DatasourceUpdatedEvent event) {
      if(event.getSelection().getName().equals(datasourceName)) {
        displayDatasource(event.getSelection());
      }
    }
  }

  // OPAL-975
  class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource("/datasource/" + datasourceName).get().withCallback(new ResourceCallback<DatasourceDto>() {

        @Override
        public void onResource(Response response, DatasourceDto resource) {
          displayDatasource(resource, null);
        }
      }).send();
    }
  }

  public interface Display extends WidgetDisplay {

    void setTableSelection(TableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<TableDto> rows);

    void afterRenderRows();

    void setDatasource(DatasourceDto dto);

    void setPreviousName(String name);

    void setNextName(String name);

    void setExcelDownloadCommand(Command cmd);

    void setExportDataCommand(Command cmd);

    void setAddUpdateTablesCommand(Command cmd);

    void setRemoveDatasourceCommand(Command cmd);

    void setAddViewCommand(Command cmd);

    void setNextCommand(Command cmd);

    void setPreviousCommand(Command cmd);

    void setTableNameFieldUpdater(FieldUpdater<TableDto, String> updater);

    void setCopyDataCommand(Command cmd);

    HasAuthorization getAddUpdateTablesAuthorizer();

    HasAuthorization getAddViewAuthorizer();

  }
}
