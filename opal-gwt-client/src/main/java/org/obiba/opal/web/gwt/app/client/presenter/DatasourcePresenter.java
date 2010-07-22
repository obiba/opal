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
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

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

  private JsArray<DatasourceDto> datasources;

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
    initDatasources();
  }

  @Override
  public void revealDisplay() {
  }

  private void displayDatasource(DatasourceDto datasourceDto) {
    displayDatasource(datasourceDto, null);
  }

  private void displayDatasource(final DatasourceDto datasourceDto, final TableDto tableDto) {
    if(datasourceName == null || !isCurrentDatasource(datasourceDto)) {
      datasourceName = datasourceDto.getName();
      getDisplay().setDatasourceName(datasourceName);
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
    }
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
    int index = getTableIndex(tableName);
    getDisplay().setTableSelection(tables.get(index), index);
  }

  private void updateTable(final String tableName) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasourceName + "/tables").get().withCallback(new TablesResourceCallback(datasourceName, tableName)).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append("/datasource/").append(datasource).append("/variables/excel").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
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

  public interface Display extends WidgetDisplay {

    void setTableSelection(TableDto variable, int index);

    void beforeRenderRows();

    void renderRows(JsArray<TableDto> rows);

    void afterRenderRows();

    void setDatasourceName(String name);

    void setPreviousName(String name);

    void setNextName(String name);

    HandlerRegistration addNextClickHandler(ClickHandler handler);

    HandlerRegistration addPreviousClickHandler(ClickHandler handler);

    HandlerRegistration addSpreadSheetClickHandler(ClickHandler handler);

    void setTableNameFieldUpdater(FieldUpdater<TableDto, String> updater);
  }
}
