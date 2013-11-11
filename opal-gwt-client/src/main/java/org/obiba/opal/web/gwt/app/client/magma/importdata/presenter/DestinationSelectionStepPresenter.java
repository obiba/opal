/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig.ImportFormat;

public class DestinationSelectionStepPresenter extends PresenterWidget<DestinationSelectionStepPresenter.Display> {

  private ImportFormat importFormat;

  JsArray<DatasourceDto> datasources;

  private String destination;

  private DatasourceDto datasource;

  @Inject
  public DestinationSelectionStepPresenter(EventBus eventBus, Display display) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    getView().setTableSelectionHandler(new TableSelectionHandler() {

      @Override
      public void onTableSelected(String datasource, String table) {
        UriBuilder ub = UriBuilder.create().segment("datasource", datasource, "table", table).query("counts", "false");
        ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build()).get()//
            .withCallback(new ResourceCallback<TableDto>() {

              @Override
              public void onResource(Response response, TableDto resource) {
                if(resource != null) {
                  getView().setEntityType(resource.getEntityType());
                }
              }
            }).send();
      }
    });
  }

  private void refreshDatasource() {
    UriBuilder ub = UriBuilder.create().segment("datasource", destination);
    ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
        .withCallback(new ResourceCallback<DatasourceDto>() {
          @Override
          public void onResource(Response response, DatasourceDto resource) {
            resource.setTableArray(JsArrays.toSafeArray(resource.getTableArray()));
            resource.setViewArray(JsArrays.toSafeArray(resource.getViewArray()));
            datasource = resource;
            getView().setDatasource(datasource);
          }
        }).send();
  }

  public boolean validate() {
    if(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat) {
      String selectedTable = getView().getSelectedTable();
//      // table cannot be empty and cannot be a view
      if(Strings.isNullOrEmpty(selectedTable) || selectedTable.trim().isEmpty()) {
        fireEvent(NotificationEvent.newBuilder().error("DestinationTableRequired").build());
        return false;
      }
      if(selectedTable.contains(".") || selectedTable.contains(":")) {
        fireEvent(NotificationEvent.newBuilder().error("DestinationTableNameInvalid").build());
        return false;
      }
      String selectedEntityType = getView().getSelectedEntityType();
      if(Strings.isNullOrEmpty(selectedEntityType) || selectedEntityType.trim().isEmpty()) {
        fireEvent(NotificationEvent.newBuilder().error("DestinationTableEntityTypeRequired").build());
        return false;
      }
      return validateDestinationTableIsNotView();
    }
    return true;
  }

  private boolean validateDestinationTableIsNotView() {
    if(datasource.getViewArray() == null) return true;

    String tableName = getView().getSelectedTable();

    for(int j = 0; j < datasource.getViewArray().length(); j++) {
      if(datasource.getViewArray().get(j).equals(tableName)) {
        fireEvent(NotificationEvent.newBuilder().error("DestinationTableCannotBeView").build());
        return false;
      }
    }

    return true;
  }

  public void refreshDisplay() {
    getView().showTables(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat);
    refreshDatasource();
  }

  public void updateImportConfig(ImportConfig importConfig) {
    importConfig.setDestinationDatasourceName(destination);
    if(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat) {
      importConfig.setDestinationTableName(getView().getSelectedTable());
      importConfig.setDestinationEntityType(getView().getSelectedEntityType());
    } else {
      importConfig.setDestinationTableName(null);
    }
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
    refreshDisplay();
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends View {

    void setDatasource(DatasourceDto datasource);

    void setTable(String name);

    void setEntityType(String entityType);

    String getSelectedTable();

    String getSelectedEntityType();

    void showTables(boolean visible);

    void setTableSelectionHandler(TableSelectionHandler handler);
  }

  public interface TableSelectionHandler {
    void onTableSelected(String datasource, String table);
  }
}
