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
import org.obiba.opal.web.model.client.DatasourceDTO;
import org.obiba.opal.web.model.client.VariableDTO;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.gen2.table.event.client.HasRowSelectionHandlers;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;

/**
 *
 */
public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasSelectionHandlers<TreeItem> getTree();

    void setItems(List<TreeItem> items);

    HasRowSelectionHandlers getTable();

    void renderRows(Iterable<VariableDTO> rows);
  }

  private JsArray<VariableDTO> variables;

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
        VariableDTO variable = variables.get(row.getRowIndex());
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
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8080/datasource/" + datasource + "/table/" + table + "/variables");
    builder.setHeader("Accept", "application/json");
    try {
      builder.sendRequest(null, new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          GWT.log(exception.getMessage());
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          variables = JsonUtils.unsafeEval(response.getText());
          getDisplay().renderRows(JsArrays.toIterable(variables));
        }

      });
    } catch(RequestException e) {
      GWT.log(e.getMessage());
    }
  }

  private void updateTree() {
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "http://localhost:8080/datasources");
    builder.setHeader("Accept", "application/json");
    try {
      builder.sendRequest(null, new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
          JsArray<DatasourceDTO> datasources = JsonUtils.unsafeEval(response.getText());
          ArrayList<TreeItem> items = new ArrayList<TreeItem>(datasources.length());
          for(int i = 0; i < datasources.length(); i++) {
            DatasourceDTO ds = datasources.get(i);
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

        @Override
        public void onError(Request request, Throwable exception) {
          GWT.log(exception.getMessage());

        }
      });
    } catch(RequestException e) {
      GWT.log(e.getMessage());
    }
  }

}
