/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.gwt.client.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.client.gwt.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.client.gwt.client.event.NavigatorSelectionChangeEventHandler;
import org.obiba.opal.web.client.gwt.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.client.gwt.client.js.JsArrays;
import org.obiba.opal.web.client.gwt.client.rest.ResourceCallback;
import org.obiba.opal.web.client.gwt.client.rest.ResourceRequest;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.gen2.table.event.client.HasRowSelectionHandlers;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 */
public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasSelectionHandlers<TreeItem> getTree();

    void setItems(List<TreeItem> items);

    HasRowSelectionHandlers getTable();

    void renderRows(Iterable<VariableDto> rows);
  }

  private JsArray<VariableDto> variables;

  /**
   * @param display
   * @param eventBus
   */
  public NavigatorPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        eventBus.fireEvent(new NavigatorSelectionChangeEvent(event.getSelectedItem()));
      }
    });

    getDisplay().getTable().addRowSelectionHandler(new RowSelectionHandler() {
      @Override
      public void onRowSelection(RowSelectionEvent event) {
        Row row = event.getSelectedRows().iterator().next();
        VariableDto variable = variables.get(row.getRowIndex());
        eventBus.fireEvent(new VariableSelectionChangeEvent(variable));
      }
    });

    eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEventHandler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        if(event.getSelection().getParentItem() != null) {
          String datasource = event.getSelection().getParentItem().getText();
          String table = event.getSelection().getText();
          updateTable(datasource, table);
        }
      }
    });
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
    updateTree();
  }

  @Override
  public void revealDisplay() {
  }

  private void updateTable(String datasource, String table) {
    ResourceRequest<JsArray<VariableDto>> rr = new ResourceRequest<JsArray<VariableDto>>("/datasource/" + datasource + "/table/" + table + "/variables");
    rr.get(new ResourceCallback<JsArray<VariableDto>>() {
      @Override
      public void onResource(JsArray<VariableDto> resource) {
        variables = resource;
        getDisplay().renderRows(JsArrays.toIterable(variables));
      }

    });
  }

  private void updateTree() {
    ResourceRequest<JsArray<DatasourceDto>> rr = new ResourceRequest<JsArray<DatasourceDto>>("/datasources");
    rr.get(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(JsArray<DatasourceDto> datasources) {
        ArrayList<TreeItem> items = new ArrayList<TreeItem>(datasources.length());
        for(int i = 0; i < datasources.length(); i++) {
          DatasourceDto ds = datasources.get(i);
          TreeItem dsItem = new TreeItem(ds.getName());
          dsItem.setUserObject(ds);
          JsArrayString array = ds.getTableArray();
          for(int j = 0; j < array.length(); j++) {
            array.get(j);
            dsItem.addItem(array.get(j));
          }
          items.add(dsItem);
        }
        getDisplay().setItems(items);
      }
    });
  }

}
