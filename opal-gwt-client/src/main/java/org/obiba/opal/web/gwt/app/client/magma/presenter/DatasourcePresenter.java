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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.AddViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResources;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.PermissionResourceType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
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
    implements DatasourceUiHandlers, DatasourceSelectionChangeEvent.Handler {

  private final ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider;

  private final ModalProvider<AddViewModalPresenter> createViewModalProvider;

  private final ModalProvider<DataExportPresenter> dataExportModalProvider;

  private final ModalProvider<DataCopyPresenter> dataCopyModalProvider;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  private final ResourcePermissionsPresenter resourcePermissionsPresenter;

  private final Translations translations;

  private String datasourceName;

  private JsArray<TableDto> tables;

  private DatasourceDto datasource;

  private Runnable deleteConfirmation;

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus,
      ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalProvider,
      ModalProvider<DataExportPresenter> dataExportModalProvider,
      ModalProvider<AddViewModalPresenter> createViewModalProvider,
      ModalProvider<DataCopyPresenter> dataCopyModalProvider, Provider<AuthorizationPresenter> authorizationPresenter,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    this.tablePropertiesModalProvider = tablePropertiesModalProvider.setContainer(this);
    this.dataExportModalProvider = dataExportModalProvider.setContainer(this);
    this.createViewModalProvider = createViewModalProvider.setContainer(this);
    this.dataCopyModalProvider = dataCopyModalProvider.setContainer(this);
    this.authorizationPresenter = authorizationPresenter;
    resourcePermissionsPresenter = resourcePermissionsProvider.get();
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
    fireEvent(new FileDownloadRequestEvent("/datasource/" + datasourceName + "/tables/excel"));
  }

  private void initDatasource() {
    // rely on 304
    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder() //
        .forResource(UriBuilders.DATASOURCE.create().build(datasourceName)) //
        .withCallback(new InitResourceCallback()) //
        .get().send();
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    datasourceName = event.getSelection();
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
    DataCopyPresenter copy = dataCopyModalProvider.get();

    Set<TableDto> copyTables = new HashSet<TableDto>();

    int selectedTablesSize = getView().getSelectedTables().size();
    if(selectedTablesSize > 0) {
      copyTables.addAll(getView().getSelectedTables());
      copy.setCopyTables(copyTables, getView().getAllTables().size() == selectedTablesSize);
    } else {
      // Get all tables
      copyTables.addAll(getView().getAllTables());
      copy.setCopyTables(copyTables, true);
    }

    copy.setDatasourceName(datasourceName);
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
//
      fireEvent(ConfirmationRequiredEvent
          .createWithMessages(deleteConfirmation, translations.confirmationTitleMap().get("deleteTables"),
              TranslationsUtils.replaceArguments(translations.confirmationMessageMap()
                  .get(tableNames.length() > 1 ? "confirmDeleteTables" : "confirmDeleteTable"),
                  String.valueOf(tableNames.length()))));
    }
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
      getView().afterRenderRows();
    }

    private void updateTables() {
      UriBuilder ub = UriBuilders.DATASOURCE_TABLES.create().query("counts", "true");
      ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(ub.build(datasourceName)).get()
          .withCallback(new TablesResourceCallback(datasourceName)).send();
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
          .authorize(CascadingAuthorizer.newBuilder() //
              .and("/functional-units", HttpMethod.GET) //
              .and("/functional-units/entities/table", HttpMethod.GET) //
              .authorize(getView().getExportDataAuthorizer()).build()) //
          .post().send();
      // copy data
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_COMMANDS_COPY.create().build(datasourceName)) //
          .authorize(getView().getCopyDataAuthorizer()) //
          .post().send();
      // import data
      ResourceAuthorizationRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.PROJECT_COMMANDS_IMPORT.create().build(datasourceName)) //
          .authorize(CascadingAuthorizer.newBuilder() //
              .and("/functional-units", HttpMethod.GET) //
              .and("/functional-units/entities/table", HttpMethod.GET) //
              .authorize(getView().getImportDataAuthorizer()).build()) //
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
      resourcePermissionsPresenter
          .initialize(PermissionResourceType.DATASOURCE, PermissionResources.datasourcePermissions(datasourceName));
      setInSlot(null, resourcePermissionsPresenter);
//      AuthorizationPresenter authz = authorizationPresenter.get();
//      String node = UriBuilder.create().segment("datasource", datasourceName).build();
//      authz.setAclRequest("datasource", new AclRequest(AclAction.CREATE_TABLE, node), //
//          new AclRequest(AclAction.DATASOURCE_ALL, node));
//      setInSlot(null, authz);
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
