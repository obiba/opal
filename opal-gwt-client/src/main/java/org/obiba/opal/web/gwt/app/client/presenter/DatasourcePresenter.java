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
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class DatasourcePresenter extends WidgetPresenter<DatasourcePresenter.Display> {

  public interface Display extends WidgetDisplay {

    void setTableSelection(TableDto variable, int index);

    void renderRows(JsArray<TableDto> rows);

    HasText getDatasourceNameLabel();

    HasClickHandlers getSpreadsheetIcon();

    HasFieldUpdater<TableDto, String> getTableNameColumn();
  }

  private String datasource;

  private JsArray<TableDto> tables;

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
    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        event.getSelection().getUserObject();
        if(event.getSelection().getParentItem() == null) {
          displayDatasource((DatasourceDto) event.getSelection().getUserObject());
        }
      }
    }));

    super.registerHandler(eventBus.addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

      @Override
      public void onNavigatorSelectionChanged(DatasourceSelectionChangeEvent event) {
        displayDatasource(event.getSelection());
      }
    }));

    super.registerHandler(getDisplay().getSpreadsheetIcon().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        downloadMetadata(datasource);
      }
    }));

    super.getDisplay().getTableNameColumn().setFieldUpdater(new FieldUpdater<TableDto, String>() {

      @Override
      public void update(int index, TableDto tableDto, String value) {

        eventBus.fireEvent(new TableSelectionChangeEvent(tableDto));

      }

    });

    super.registerHandler(eventBus.addHandler(SiblingTableSelectionEvent.getType(), new SiblingTableSelectionEvent.Handler() {

      @Override
      public void onSiblingTableSelection(SiblingTableSelectionEvent event) {
        TableDto siblingSelection = event.getCurrentSelection();

        // Look for the table and its position in the list by its name.
        // Having an position of the current variable would be more efficient.
        int siblingIndex = 0;
        for(int i = 0; i < tables.length(); i++) {
          if(tables.get(i).getName().equals(event.getCurrentSelection().getName())) {
            if(event.getDirection().equals(SiblingTableSelectionEvent.Direction.NEXT) && i < tables.length() - 1) {
              siblingIndex = i + 1;
            } else if(event.getDirection().equals(SiblingTableSelectionEvent.Direction.PREVIOUS) && i != 0) {
              siblingIndex = i - 1;
            } else {
              siblingIndex = i;
            }
            break;
          }
        }
        siblingSelection = tables.get(siblingIndex);

        getDisplay().setTableSelection(siblingSelection, siblingIndex);
        eventBus.fireEvent(new TableSelectionChangeEvent(siblingSelection));
      }
    }));
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
    if(!datasourceDto.getName().equals(datasource)) {
      datasource = datasourceDto.getName();
      getDisplay().getDatasourceNameLabel().setText(datasource);
      updateTable(datasource);
    }
  }

  private void updateTable(final String datasource) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasource + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        tables = resource;
        getDisplay().renderRows(resource);
      }

    }).send();
  }

  private void downloadMetadata(String datasource) {
    String downloadUrl = new StringBuilder(GWT.getModuleBaseURL().replace(GWT.getModuleName() + "/", "ws")).append("/datasource/").append(datasource).append("/variables/excel").toString();
    eventBus.fireEvent(new FileDownloadEvent(downloadUrl));
  }
}
