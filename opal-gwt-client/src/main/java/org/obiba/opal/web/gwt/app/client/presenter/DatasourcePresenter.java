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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class DatasourcePresenter extends WidgetPresenter<DatasourcePresenter.Display> {

  private String datasourceName;

  private JsArray<TableDto> tables;

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
    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionHandler()));
    super.registerHandler(eventBus.addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionHandler()));
    super.registerHandler(getDisplay().addSpreadSheetClickHandler(new SpreadSheetClickHandler()));
    super.registerHandler(getDisplay().addNextClickHandler(new NextClickHandler()));
    super.registerHandler(getDisplay().addPreviousClickHandler(new PreviousClickHandler()));
    super.registerHandler(eventBus.addHandler(SiblingTableSelectionEvent.getType(), new SiblingTableSelectionHandler()));
    super.getDisplay().setTableNameFieldUpdater(new TableNameFieldUpdater());
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
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  private void displayDatasource(DatasourceDto datasourceDto) {
    displayDatasource(datasourceDto, null);
  }

  private void displayDatasource(DatasourceDto datasourceDto, TableDto tableDto) {
    if(datasourceName == null || !isCurrentDatasource(datasourceDto)) {
      datasourceName = datasourceDto.getName();
      getDisplay().setDatasourceName(datasourceName);
      updateTable(tableDto != null ? tableDto.getName() : null);
    } else if(tableDto != null) {
      selectTable(tableDto.getName());
    }
  }

  private boolean isCurrentDatasource(DatasourceDto datasourceDto) {
    return datasourceDto.getName().equals(datasourceName);
  }

  private void selectTable(String tableName) {
    int index = getTableIndex(tableName);
    getDisplay().setTableSelection(tables.get(index), index);
  }

  private void updateTable(final String tableName) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasourceName + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        tables = resource;
        getDisplay().renderRows(resource);
        selectTable(tableName);
      }

    }).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append("/datasource/").append(datasource).append("/variables/excel").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
  }

  //
  // Interfaces and classes
  //

  /**
   *
   */
  class TableNameFieldUpdater implements FieldUpdater<TableDto, String> {
    @Override
    public void update(int index, TableDto tableDto, String value) {
      eventBus.fireEvent(new TableSelectionChangeEvent(tableDto));
    }
  }

  private final class DatasourceSelectionHandler implements DatasourceSelectionChangeEvent.Handler {
    @Override
    public void onNavigatorSelectionChanged(DatasourceSelectionChangeEvent event) {
      displayDatasource(event.getSelection());
    }
  }

  class NavigatorSelectionHandler implements NavigatorSelectionChangeEvent.Handler {
    @Override
    public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
      if(event.getSelection().getParentItem() == null) {
        displayDatasource((DatasourceDto) event.getSelection().getUserObject());
      } else {
        // sync the table selection in the tree with the table selection in the tables list
        displayDatasource((DatasourceDto) event.getSelection().getParentItem().getUserObject(), (TableDto) event.getSelection().getUserObject());
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
      eventBus.fireEvent(new TableSelectionChangeEvent(siblingSelection));
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
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
        @Override
        public void onResource(Response response, JsArray<DatasourceDto> resource) {
          if(resource != null) {
            for(int i = 0; i < resource.length(); i++) {
              if(resource.get(i).getName().equals(datasourceName)) {
                if(i != 0) {
                  eventBus.fireEvent(new DatasourceSelectionChangeEvent(resource.get(i - 1)));
                }
                break;
              }
            }
          }
        }

      }).send();
    }
  }

  class NextClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
        @Override
        public void onResource(Response response, JsArray<DatasourceDto> resource) {
          if(resource != null) {
            for(int i = 0; i < resource.length(); i++) {
              if(resource.get(i).getName().equals(datasourceName)) {
                if(i < resource.length() - 1) {
                  eventBus.fireEvent(new DatasourceSelectionChangeEvent(resource.get(i + 1)));
                }
                break;
              }
            }
          }
        }

      }).send();
    }
  }

  public interface Display extends WidgetDisplay {

    void setTableSelection(TableDto variable, int index);

    void renderRows(JsArray<TableDto> rows);

    void setDatasourceName(String name);

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    HandlerRegistration addSpreadSheetClickHandler(ClickHandler handler);

    void setTableNameFieldUpdater(FieldUpdater<TableDto, String> updater);
  }
}
