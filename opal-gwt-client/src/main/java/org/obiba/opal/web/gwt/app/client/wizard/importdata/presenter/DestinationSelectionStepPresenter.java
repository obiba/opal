/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportFormat;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

public class DestinationSelectionStepPresenter extends PresenterWidget<DestinationSelectionStepPresenter.Display> {

  private ImportFormat importFormat;

  JsArray<DatasourceDto> datasources;

  @Inject
  public DestinationSelectionStepPresenter(final EventBus eventBus, final Display display) {
    super(eventBus, display);
  }

  public void setImportFormat(ImportFormat importFormat) {
    this.importFormat = importFormat;
    refreshDisplay();
  }

  @Override
  protected void onBind() {
    super.onBind();
    getView().setTableSelectionHandler(new TableSelectionHandler() {

      @Override
      public void onTableSelected(String datasource, String table) {
        UriBuilder ub = UriBuilder.create().segment("datasource", datasource, "table", table);
        ResourceRequestBuilderFactory.<TableDto>newBuilder()
            .forResource(ub.build()).get()//
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

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> resource) {
            datasources = JsArrays.toSafeArray(resource);

            for(int i = 0; i < datasources.length(); i++) {
              DatasourceDto d = datasources.get(i);
              d.setTableArray(JsArrays.toSafeArray(d.getTableArray()));
              d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
            }

            getView().setDatasources(datasources);
          }
        }).send();
  }

  public boolean validate() {
    if(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat) {
      // table cannot be empty and cannot be a view
      if(getView().getSelectedTable().trim().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("DestinationTableRequired").build());
        return false;
      }
      if(getView().getSelectedTable().contains(".") || getView().getSelectedTable().contains(":")) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("DestinationTableNameInvalid").build());
        return false;
      }
      if(getView().getSelectedEntityType().trim().isEmpty()) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("DestinationTableEntityTypeRequired").build());
        return false;
      }
      return validateDestinationTableIsNotView();
    }
    return true;
  }

  private boolean validateDestinationTableIsNotView() {
    String dsName = getView().getSelectedDatasource();
    String tableName = getView().getSelectedTable();
    for(int i = 0; i < datasources.length(); i++) {
      DatasourceDto ds = datasources.get(i);
      if(ds.getName().equals(dsName) && ds.getViewArray() != null) {
        for(int j = 0; j < ds.getViewArray().length(); j++) {
          if(ds.getViewArray().get(j).equals(tableName)) {
            getEventBus().fireEvent(NotificationEvent.newBuilder().error("DestinationTableCannotBeView").build());
            return false;
          }
        }
        return true;
      }
    }

    return true;
  }

  public void refreshDisplay() {
    getView().showTables(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat);
    refreshDatasources();
  }

  public void updateImportData(ImportData importData) {
    importData.setDestinationDatasourceName(getView().getSelectedDatasource());
    if(ImportFormat.CSV == importFormat || ImportFormat.EXCEL == importFormat) {
      importData.setDestinationTableName(getView().getSelectedTable());
      importData.setDestinationEntityType(getView().getSelectedEntityType());
    } else {
      importData.setDestinationTableName(null);
    }
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends View {

    void setDatasources(JsArray<DatasourceDto> datasources);

    void setTable(String name);

    void setEntityType(String entityType);

    String getSelectedDatasource();

    String getSelectedTable();

    String getSelectedEntityType();

    void showTables(boolean visible);

    void setTableSelectionHandler(TableSelectionHandler handler);

  }

  public interface TableSelectionHandler {
    public void onTableSelected(String datasource, String table);
  }
}
