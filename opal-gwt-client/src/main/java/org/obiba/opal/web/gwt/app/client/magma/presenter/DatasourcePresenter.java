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

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AclRequest;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.copydata.presenter.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.magma.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.app.client.ui.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.magma.exportdata.presenter.DataExportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.DataImportPresenter;
import org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter.VariablesImportPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.AclAction;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class DatasourcePresenter extends PresenterWidget<DatasourcePresenter.Display>
    implements DatasourceUiHandlers, DatasourceSelectionChangeEvent.Handler {

  private String datasourceName;

  private JsArray<TableDto> tables;

  private DatasourceDto datasource;

  private final Provider<AuthorizationPresenter> authorizationPresenter;

  @Inject
  public DatasourcePresenter(Display display, EventBus eventBus,
      Provider<AuthorizationPresenter> authorizationPresenter) {
    super(eventBus, display);
    this.authorizationPresenter = authorizationPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(DatasourceSelectionChangeEvent.getType(), this);
    getView().setTableNameFieldUpdater(new TableNameFieldUpdater());
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

  private void authorize() {
    UriBuilder ub = UriBuilder.create().segment("datasource", datasourceName);
    // create tables
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/tables").post()
        .authorize(getView().getAddUpdateTablesAuthorizer()).send();
    // create views
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/views").post()
        .authorize(getView().getAddViewAuthorizer()).send();
    // export variables in excel
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/tables/excel").get()
        .authorize(getView().getExcelDownloadAuthorizer()).send();
    // export data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_copy").post()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units", HttpMethod.GET)//
            .and("/functional-units/entities/table", HttpMethod.GET)//
            .authorize(getView().getExportDataAuthorizer()).build())//
        .send();
    // copy data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_copy").post()
        .authorize(getView().getCopyDataAuthorizer()).send();
    // import data
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(ub.build() + "/commands/_import").post()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units", HttpMethod.GET)//
            .and("/functional-units/entities/table", HttpMethod.GET)//
            .authorize(getView().getImportDataAuthorizer()).build())//
        .send();
  }

  private void displayDatasource(final DatasourceDto datasourceDto, @Nullable String table) {
    getView().setDatasource(datasourceDto);
    updateTable(table);
    authorize();

    if(table != null) {
      selectTable(table);
    } else {
      getView().afterRenderRows();
    }
  }

  private void selectTable(String tableName) {
    if(tableName != null) {
      int index = getTableIndex(tableName);
      getView().setTableSelection(tables.get(index), index);
    }
  }

  private void updateTable(@Nullable String tableName) {
    UriBuilder ub = UriBuilder.URI_DATASOURCE_TABLES.query("counts", "true");
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder().forResource(ub.build(datasourceName)).get()
        .withCallback(new TablesResourceCallback(datasourceName, tableName)).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = "/datasource/" + datasource + "/tables/excel";
    getEventBus().fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  private void addView(String datasource) {
    getEventBus().fireEvent(new WizardRequiredEvent(CreateViewStepPresenter.WizardType, datasource));
  }

  private String getPreviousTableName(int index) {
    TableDto previous = null;
    if(index > 0) {
      previous = tables.get(index - 1);
    }
    return previous == null ? null : previous.getName();
  }

  private String getNextTableName(int index) {
    TableDto next = null;
    if(index < tables.length() - 1) {
      next = tables.get(index + 1);
    }
    return next == null ? null : next.getName();
  }

  private void initDatasource() {
    if(datasource == null || !datasource.getName().equals(datasourceName)) {
      ResourceRequestBuilderFactory.<DatasourceDto>newBuilder()
          .forResource(UriBuilder.URI_DATASOURCE.build(datasourceName)).get()
          .withCallback(new ResourceCallback<DatasourceDto>() {
            @Override
            public void onResource(Response response, DatasourceDto resource) {
              datasource = resource;
              displayDatasource(datasource, null);
            }

          }).send();
    }
  }

  @Override
  public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
    GWT.log("onDatasourceSelectionChanged=" + event.getSelection());
    datasourceName = event.getSelection();
    initDatasource();
  }

  @Override
  public void onImportData() {
    getEventBus().fireEvent(new WizardRequiredEvent(DataImportPresenter.WizardType, datasourceName));
  }

  @Override
  public void onExportData() {
    getEventBus().fireEvent(new WizardRequiredEvent(DataExportPresenter.WizardType, datasourceName));
  }

  @Override
  public void onCopyData() {
    getEventBus().fireEvent(new WizardRequiredEvent(DataCopyPresenter.WizardType, datasourceName));
  }

  @Override
  public void onAddTable() {
    getEventBus().fireEvent(new WizardRequiredEvent(VariablesImportPresenter.WIZARD_TYPE, datasourceName));
  }

  @Override
  public void onAddView() {
    addView(datasourceName);
  }

  @Override
  public void onDownloadDictionary() {
    downloadMetadata(datasourceName);
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
      authz.setAclRequest("datasource", new AclRequest(AclAction.CREATE_TABLE, node), //
          new AclRequest(AclAction.CREATE_VIEW, node), //
          new AclRequest(AclAction.DATASOURCE_ALL, node));
      setInSlot(null, authz);
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
      if(datasourceName.equals(DatasourcePresenter.this.datasourceName)) {
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
      getEventBus().fireEvent(
          new TableSelectionChangeEvent(DatasourcePresenter.this, tableDto, getPreviousTableName(index),
              getNextTableName(index)));
    }
  }

  public interface Display extends View, HasUiHandlers<DatasourceUiHandlers> {

    void setTableSelection(TableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<TableDto> rows);

    void afterRenderRows();

    void setDatasource(DatasourceDto dto);

    void setTableNameFieldUpdater(FieldUpdater<TableDto, String> updater);

    HasAuthorization getAddUpdateTablesAuthorizer();

    HasAuthorization getAddViewAuthorizer();

    HasAuthorization getImportDataAuthorizer();

    HasAuthorization getExportDataAuthorizer();

    HasAuthorization getCopyDataAuthorizer();

    HasAuthorization getExcelDownloadAuthorizer();
  }
}
