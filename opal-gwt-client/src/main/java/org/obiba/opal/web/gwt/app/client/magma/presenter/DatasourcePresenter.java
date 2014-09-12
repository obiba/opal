/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.AddViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class DatasourcePresenter extends PresenterWidget<DatasourcePresenter.Display>
    implements DatasourceUiHandlers, DatasourceSelectionChangeEvent.DatasourceSelectionChangeHandler {

  private final ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider;

  private final ModalProvider<AddViewModalPresenter> createViewModalProvider;

  private final ModalProvider<DataExportPresenter> dataExportModalProvider;

  private final ModalProvider<DataCopyPresenter> dataCopyModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private TranslationMessages translationMessages;

  private String datasourceName;

  private JsArray<TableDto> tables;

  private DatasourceDto datasource;

  private Runnable deleteConfirmation;

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus,
      ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider,
      ModalProvider<DataExportPresenter> dataExportModalProvider,
      ModalProvider<AddViewModalPresenter> createViewModalProvider,
      ModalProvider<DataCopyPresenter> dataCopyModalProvider,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.tablePropertiesModalProvider = tablePropertiesModalProvider.setContainer(this);
    this.dataExportModalProvider = dataExportModalProvider.setContainer(this);
    this.createViewModalProvider = createViewModalProvider.setContainer(this);
    this.dataCopyModalProvider = dataCopyModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);

    // Delete tables confirmation handler
    addRegisteredHandler(ConfirmationEvent.getType(), new DeleteConfirmationEventHandler());
  }

  private void downloadMetadata() {
    // if some tables are selected
    UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLES_EXCEL.create();
    for(TableDto tableDto : getView().getSelectedTables()) {
      uriBuilder.query("table", tableDto.getName());
    }

    fireEvent(new FileDownloadRequestEvent(uriBuilder.build(datasourceName)));
  }

  private void initDatasource() {
    // rely on 304
    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder() //
        .forResource(UriBuilders.DATASOURCE.create().build(datasourceName)) //
        .withCallback(new InitResourceCallback()) //
        .get().send();
  }

  @Override
  public void onDatasourceSelectionChange(DatasourceSelectionChangeEvent event) {
    datasourceName = event.getDatasource();
    initDatasource();
  }

  @Override
  public void onImportData() {
    fireEvent(new WizardRequiredEvent(DataImportPresenter.WizardType, datasourceName));
  }

  @Override
  public void onExportData() {
    DataExportPresenter export = dataExportModalProvider.get();
    Set<TableDto> exportTables = new HashSet<TableDto>();

    int selectedTablesSize = getView().getSelectedTables().size();
    if(selectedTablesSize > 0) {
      exportTables.addAll(getView().getSelectedTables());
      export.setExportTables(exportTables, getView().getAllTables().size() == selectedTablesSize);
    } else {
      // Get all tables
      exportTables.addAll(getView().getAllTables());
      export.setExportTables(exportTables, true);
    }

    export.setDatasourceName(datasourceName);
  }

  @Override
  public void onCopyData() {
    Set<TableDto> copyTables = new HashSet<>();

    int selectedTablesSize = getView().getSelectedTables().size();
    boolean allTables = true;

    if(selectedTablesSize > 0) {
      copyTables.addAll(getView().getSelectedTables());
      allTables = getView().getAllTables().size() == selectedTablesSize;
    } else {
      copyTables.addAll(getView().getAllTables());
    }

    // Display error when copying multiple tables but there is only one project (cannot rename tables)
    if(copyTables.size() > 1) {
      checkDatasourceCountBeforeInitModal(copyTables, allTables, selectedTablesSize);
    } else {
      initDataCopyModal(copyTables, allTables);
    }
  }

  private void checkDatasourceCountBeforeInitModal(final Set<TableDto> copyTables, boolean allTables,
      final int selectedTablesSize) {
    final boolean finalAllTables = allTables;
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder()
        .forResource(UriBuilders.DATASOURCES_COUNT.create().build()).get().withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(Integer.parseInt(response.getText()) > 1) {
          initDataCopyModal(copyTables, finalAllTables);
        } else if(selectedTablesSize > 0) {
          fireEvent(NotificationEvent.newBuilder().warn("CannotCopySelectedTablesWithinProject").sticky().build());
        } else {
          // Explain that this action would select all tables...
          fireEvent(NotificationEvent.newBuilder().warn("CannotCopyAllTablesWithinProject").sticky().build());
        }
      }
    }, SC_OK).send();
  }

  private void initDataCopyModal(Set<TableDto> copyTables, boolean allTables) {
    DataCopyPresenter copy = dataCopyModalProvider.get();
    copy.setDatasourceName(datasourceName);
    copy.setCopyTables(copyTables, allTables);
  }

  @Override
  public void onAddTable() {
    TablePropertiesModalPresenter p = tablePropertiesModalProvider.get();
    p.initialize(datasource);
  }

  @Override
  public void onAddUpdateTables() {
    fireEvent(new WizardRequiredEvent(VariablesImportPresenter.WIZARD_TYPE, datasourceName));
  }

  @Override
  public void onAddView() {
    createViewModalProvider.get().setDatasourceName(datasourceName);
  }

  @Override
  public void onDownloadDictionary() {
    downloadMetadata();
  }

  @Override
  public void onDeleteTables(List<TableDto> tableDtos) {
    if(tableDtos.isEmpty()) {
      fireEvent(NotificationEvent.newBuilder().error("DeleteTableSelectAtLeastOne").build());
    } else {
      JsArrayString tableNames = JsArrays.create().cast();
      for(TableDto table : tableDtos) {
        tableNames.push(table.getName());
      }

      deleteConfirmation = new RemoveRunnable(tableNames);

      fireEvent(ConfirmationRequiredEvent.createWithMessages(deleteConfirmation, translationMessages.removeTables(),
          translationMessages.confirmRemoveTables(tableNames.length())));
    }
  }

  @Override
  public void onTablesFilterUpdate(String filter) {
    if(Strings.isNullOrEmpty(filter)) {
      getView().renderRows(tables);
    } else {
      JsArray<TableDto> filteredTables = JsArrays.create();
      for(TableDto table : JsArrays.toIterable(tables)) {
        if(tableMatches(table, filter)) {
          filteredTables.push(table);
        }
      }
      getView().renderRows(filteredTables);
    }
  }

  /**
   * Check if table name matches all the words of the table filter.
   *
   * @param table
   * @param filter
   * @return
   */
  private boolean tableMatches(TableDto table, String filter) {
    String name = table.getName().toLowerCase();
    for(String token : filter.toLowerCase().split(" ")) {
      if(!Strings.isNullOrEmpty(token)) {
        if(!name.contains(token)) return false;
      }
    }
    return true;
  }

  //
  // Interfaces and classes
  //

  private class InitResourceCallback implements ResourceCallback<DatasourceDto> {
    @Override
    public void onResource(Response response, DatasourceDto resource) {
      datasource = resource;
      displayDatasource(datasource);
    }

    private void displayDatasource(DatasourceDto datasourceDto) {
      getView().setDatasource(datasourceDto);
      updateTables();
      authorize();
    }

    private void updateTables() {
      updateTables(true);
    }

    private void updateTables(final boolean withCounts) {
      UriBuilder ub = UriBuilders.DATASOURCE_TABLES.create();
      if(withCounts) ub.query("counts", "true");
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(ub.build(datasourceName))
          .get().withCallback(new TablesResourceCallback(datasourceName)) //
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(NotificationEvent.newBuilder().error((ClientErrorDto) JsonUtils.unsafeEval(response.getText()))
                  .build());
              if(withCounts) updateTables(false);
            }
          }, Response.SC_BAD_REQUEST, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND).send();
    }

    private void authorize() {
      authorizeDatasource();
      authorizeProject();

      // set permissions
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_PERMISSIONS_DATASOURCE.create().build(datasourceName)) //
          .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())) //
          .post().send();
    }

    private void authorizeDatasource() {
      // create tables
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLES.create().build(datasourceName)) //
          .authorize(getView().getAddUpdateTablesAuthorizer()) //
          .post().send();
      // create views
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_VIEWS.create().build(datasourceName)) //
          .authorize(getView().getAddViewAuthorizer()) //
          .post().send();
      // export variables in excel
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.DATASOURCE_TABLES.create().build(datasourceName) + "/excel") //
          .authorize(getView().getExcelDownloadAuthorizer()) //
          .get().send();
    }

    private void authorizeProject() {
      // export data
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_COMMANDS_EXPORT.create().build(datasourceName)) //
          .authorize(getView().getExportDataAuthorizer()) //
          .post().send();
      // copy data
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_COMMANDS_COPY.create().build(datasourceName)) //
          .authorize(getView().getCopyDataAuthorizer()) //
          .post().send();
      // import data
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_COMMANDS_IMPORT.create().build(datasourceName)) //
          .authorize(getView().getImportDataAuthorizer()) //
          .post().send();
    }
  }

  private final class TablesResourceCallback implements ResourceCallback<JsArray<TableDto>> {

    private final String datasourceName;

    private TablesResourceCallback(String datasourceName) {
      this.datasourceName = datasourceName;
      getView().beforeRenderRows();
    }

    @Override
    public void onResource(Response response, JsArray<TableDto> resource) {
      if(datasourceName.equals(DatasourcePresenter.this.datasourceName)) {
        tables = JsArrays.toSafeArray(resource);
        getView().renderRows(resource);
        getView().afterRenderRows();
      }
    }
  }

  private class RemoveRunnable implements Runnable {

    private static final int BATCH_SIZE = 20;

    int nb_deleted = 0;

    final JsArrayString tableNames;

    private RemoveRunnable(JsArrayString tableNames) {
      this.tableNames = tableNames;
    }

    private String getUri() {
      UriBuilder uriBuilder = UriBuilders.DATASOURCE_TABLES.create();

      for(int i = nb_deleted, added = 0; i < tableNames.length() && added < BATCH_SIZE; i++, added++) {
        uriBuilder.query("table", tableNames.get(i));
      }

      return uriBuilder.build(datasource.getName());
    }

    @Override
    public void run() {
      // show loading
      getView().beforeRenderRows();
      ResourceRequestBuilderFactory.newBuilder().forResource(getUri())//
          .delete()//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == SC_OK) {
                nb_deleted += BATCH_SIZE;

                if(nb_deleted < tableNames.length()) {
                  run();
                } else {
                  initDatasource();
                }
              } else {
                String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
                fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
              }

            }
          }, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND).send();
    }
  }

  private class DeleteConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(deleteConfirmation != null && event.getSource().equals(deleteConfirmation) &&
          event.isConfirmed()) {
        deleteConfirmation.run();
        deleteConfirmation = null;
      }
    }
  }

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
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.DATASOURCE,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_DATASOURCE, datasourceName);
      setInSlot(null, resourcePermissionsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<DatasourceUiHandlers> {

    void beforeRenderRows();

    void renderRows(JsArray<TableDto> rows);

    void afterRenderRows();

    void setDatasource(DatasourceDto dto);

    HasAuthorization getAddUpdateTablesAuthorizer();

    HasAuthorization getAddViewAuthorizer();

    HasAuthorization getImportDataAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    List<TableDto> getSelectedTables();

    List<TableDto> getAllTables();
  }
}
