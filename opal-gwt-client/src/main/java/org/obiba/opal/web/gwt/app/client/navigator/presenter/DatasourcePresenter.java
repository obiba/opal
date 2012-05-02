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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceRemovedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.wizard.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class DatasourcePresenter extends Presenter<DatasourcePresenter.Display, DatasourcePresenter.Proxy> {

  private String datasourceName;

  private JsArray<TableDto> tables;

  private JsArray<DatasourceDto> datasources;

  private Runnable removeDatasourceConfirmation;

  private Provider<AuthorizationPresenter> authorizationPresenter;

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus, Proxy proxy, Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display, proxy);
    this.authorizationPresenter = authorizationPresenter;
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, NavigatorPresenter.CENTER_PANE, this);
  }

  @ProxyEvent
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent e) {
    if(isVisible() == false) {
      forceReveal();
      displayDatasource(e.getSelection());
    }
  }

  @Override
  protected void onBind() {
    super.onBind();

    super.registerHandler(getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionHandler()));
    super.registerHandler(getEventBus().addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionHandler()));
    super.registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    getView().setExcelDownloadCommand(new ExcelDownloadCommand());
    getView().setExportDataCommand(new ExportDataCommand());
    getView().setCopyDataCommand(new CopyDataCommand());
    getView().setAddUpdateTablesCommand(new AddUpdateTablesCommand());
    getView().setRemoveDatasourceCommand(new RemoveDatasourceCommand());
    getView().setAddViewCommand(new AddViewCommand());
    getView().setNextCommand(new NextCommand());
    getView().setPreviousCommand(new PreviousCommand());
    super.registerHandler(getEventBus().addHandler(SiblingTableSelectionEvent.getType(), new SiblingTableSelectionHandler()));
    super.getView().setTableNameFieldUpdater(new TableNameFieldUpdater());
    super.registerHandler(getEventBus().addHandler(DatasourceUpdatedEvent.getType(), new DatasourceUpdatedEventHandler()));

    // OPAL-975
    super.registerHandler(getEventBus().addHandler(ViewSavedEvent.getType(), new ViewSavedEventHandler()));
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
  public void onReveal() {
    initDatasources();
  }

  private void authorize() {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName);
    // create tables
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/files/meta").get().authorize(getView().getAddUpdateTablesAuthorizer()).send();
    // create views
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/views").post().authorize(getView().getAddViewAuthorizer()).send();
    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/tables/excel").get().authorize(getView().getExcelDownloadAuthorizer()).send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post()//
    .authorize(CascadingAuthorizer.newBuilder().and("/files/meta", HttpMethod.GET)//
    .and("/functional-units", HttpMethod.GET)//
    .and("/functional-units/entities/table", HttpMethod.GET)//
    .authorize(getView().getExportDataAuthorizer()).build())//
    .send();
    // copy data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/copy").post().authorize(getView().getCopyDataAuthorizer()).send();
    // remove
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build()).delete().authorize(getView().getRemoveDatasourceAuthorizer()).send();
    // set permissions
    AclRequest.newResourceAuthorizationRequestBuilder().authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  private void displayDatasource(String datasourceName) {
    if(datasources == null) return;

    for(int i = 0; i < datasources.length(); i++) {
      if(datasources.get(i).getName().equals(datasourceName)) {
        displayDatasource(datasources.get(i));
        break;
      }
    }
  }

  private void displayDatasource(DatasourceDto datasourceDto) {
    displayDatasource(datasourceDto, null);
  }

  private void displayDatasource(final DatasourceDto datasourceDto, final TableDto tableDto) {
    if(datasourceName == null || !isCurrentDatasource(datasourceDto)) {
      datasourceName = datasourceDto.getName();
      getView().setDatasource(datasourceDto);
      updateTable(tableDto != null ? tableDto.getName() : null);

      // make sure the list of datasources is initialized before looking for siblings
      if(datasources == null || datasources.length() == 0 || getDatasourceIndex(datasourceDto) < 0) {
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

      authorize();
    } else if(tableDto != null) {
      selectTable(tableDto.getName());
    } else {
      updateTable(null);
    }
  }

  private void displayDatasourceSiblings(DatasourceDto datasourceDto) {
    int index = getDatasourceIndex(datasourceDto);
    getView().setPreviousName(index > 0 ? datasources.get(index - 1).getName() : null);
    getView().setNextName(index < datasources.length() - 1 ? datasources.get(index + 1).getName() : null);
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
      getView().setTableSelection(tables.get(index), index);
    }
  }

  private void updateTable(final String tableName) {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName, "tables");
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource(ub.build()).get().withCallback(new TablesResourceCallback(datasourceName, tableName)).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = new StringBuilder("/datasource/").append(datasource).append("/tables/excel").toString();
    getEventBus().fireEvent(new FileDownloadEvent(downloadUrl));
  }

  private void addView(String datasource) {
    getEventBus().fireEvent(new WizardRequiredEvent(CreateViewStepPresenter.WizardType, datasource));
  }

  private void removeDatasource(final String datasource) {

    ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() != Response.SC_OK) {
          getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
        } else {
          initDatasources();
          getEventBus().fireEvent(new DatasourceRemovedEvent(datasource));
        }
      }
    };
    UriBuilder uriBuilder = UriBuilder.create();
    uriBuilder.segment("datasource", datasourceName);
    ResourceRequestBuilderFactory.newBuilder().forResource(uriBuilder.build()).delete().withCallback(Response.SC_OK, callbackHandler).withCallback(Response.SC_FORBIDDEN, callbackHandler).withCallback(Response.SC_INTERNAL_SERVER_ERROR, callbackHandler).withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
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
        datasources = JsArrays.toSafeArray(resource);
      }

    }).send();
  }

  //
  // Interfaces and classes
  //

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(null);
    }

    @Override
    public void authorized() {
      AuthorizationPresenter authz = authorizationPresenter.get();
      String node = UriBuilder.create().segment("datasource", datasourceName).build();
      authz.setAclRequest("datasource", new AclRequest(AclAction.DATASOURCE_ALL, node), //
      new AclRequest(AclAction.CREATE_TABLE, node), //
      new AclRequest(AclAction.CREATE_VIEW, node));
      setInSlot(null, authz);
    }
  }

  final class PreviousCommand implements Command {
    @Override
    public void execute() {
      for(int i = 0; i < datasources.length(); i++) {
        if(datasources.get(i).getName().equals(datasourceName)) {
          if(i != 0) {
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i - 1)));
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
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i + 1)));
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

      getEventBus().fireEvent(new ConfirmationRequiredEvent(removeDatasourceConfirmation, "removeDatasource", "confirmRemoveDatasource"));
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
      getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType, datasourceName));
    }
  }

  final class CopyDataCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(DataCopyPresenter.WizardType, datasourceName));
    }
  }

  final class AddUpdateTablesCommand implements Command {
    @Override
    public void execute() {
      getEventBus().fireEvent(new WizardRequiredEvent(VariablesImportPresenter.WizardType, datasourceName));
    }
  }

  private final class TablesResourceCallback implements ResourceCallback<JsArray<TableDto>> {

    private final String datasourceName;

    private final String selectTableName;

    private TablesResourceCallback(String datasourceName, String selectTableName) {
      this.datasourceName = datasourceName;
      this.selectTableName = selectTableName;
      getView().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<TableDto> resource) {
      if(this.datasourceName.equals(DatasourcePresenter.this.datasourceName)) {
        tables = JsArrays.toSafeArray(resource);
        getView().renderRows(resource);
        selectTable(selectTableName);
        getView().afterRenderRows();
      }
    }
  }

  class TableNameFieldUpdater implements FieldUpdater<TableDto, String> {
    @Override
    public void update(int index, TableDto tableDto, String value) {
      getEventBus().fireEvent(new TableSelectionChangeEvent(DatasourcePresenter.this, tableDto, getPreviousTableName(index), getNextTableName(index)));
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
        UriBuilder ub = UriBuilder.create().segment("datasource", event.getSelection().getDatasourceName());
        ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource(ub.build()).get().withCallback(new ResourceCallback<DatasourceDto>() {
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

      // This fires a TableSelectionChangeEvent if the selection changes
      getView().setTableSelection(siblingSelection, siblingIndex);

      getEventBus().fireEvent(new TableSelectionChangeEvent(DatasourcePresenter.this, siblingSelection, getPreviousTableName(siblingIndex), getNextTableName(siblingIndex)));
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
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i - 1)));
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
            getEventBus().fireEvent(new DatasourceSelectionChangeEvent(datasources.get(i + 1)));
          }
          break;
        }
      }
    }
  }

  class DatasourceUpdatedEventHandler implements DatasourceUpdatedEvent.Handler {

    @Override
    public void onDatasourceUpdated(DatasourceUpdatedEvent event) {
      if(event.getDatasourceName().equals(datasourceName)) {
        displayDatasource(event.getDatasourceName());
      }
    }
  }

  // OPAL-975
  class ViewSavedEventHandler implements ViewSavedEvent.Handler {

    @Override
    public void onViewSaved(ViewSavedEvent event) {
      UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName);
      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().forResource(ub.build()).get().withCallback(new ResourceCallback<DatasourceDto>() {

        @Override
        public void onResource(Response response, DatasourceDto resource) {
          displayDatasource(resource, null);
        }
      }).send();
    }
  }

  @ProxyStandard
  public interface Proxy extends com.gwtplatform.mvp.client.proxy.Proxy<DatasourcePresenter> {
  }

  public interface Display extends View {

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

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getRemoveDatasourceAuthorizer();

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

  }
}
