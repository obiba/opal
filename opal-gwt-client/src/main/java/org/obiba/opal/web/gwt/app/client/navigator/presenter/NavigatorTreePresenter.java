/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.presenter;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourcesRefreshEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

/**
 * Presenter for a Tree displaying Opal datasources and tables.
 */
public class NavigatorTreePresenter extends Presenter<NavigatorTreePresenter.Display, NavigatorTreePresenter.Proxy> {

  @Inject
  public NavigatorTreePresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy);
  }

  @Override
  protected void revealInParent() {
    RevealContentEvent.fire(this, NavigatorPresenter.LEFT_PANE, this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getView().getTree().addSelectionHandler(new TreeSelectionHandler()));

    registerHandler(
        getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

          @Override
          public void onTableSelectionChanged(TableSelectionChangeEvent event) {
            getView().selectTable(event.getSelection().getDatasourceName(), event.getSelection().getName(), false);
          }

        }));

    registerHandler(getEventBus()
        .addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

          @Override
          public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
            if(getView().hasDatasource(event.getSelection().getName())) {
              getView().selectDatasource(event.getSelection().getName(), false);
            } else {
              updateTree(event.getSelection().getName(), false);
            }
          }

        }));

    registerHandler(getEventBus().addHandler(DatasourceUpdatedEvent.getType(), new DatasourceUpdatedEvent.Handler() {

      @Override
      public void onDatasourceUpdated(DatasourceUpdatedEvent event) {
        updateTree(event.getDatasourceName(), true);
      }

    }));

    registerHandler(getEventBus().addHandler(DatasourcesRefreshEvent.getType(), new DatasourcesRefreshEvent.Handler() {

      @Override
      public void onRefresh(DatasourcesRefreshEvent event) {
        updateTree(null, false);
      }

    }));

    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

          @Override
          public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
            getView().selectTable(event.getTable().getDatasourceName(), event.getTable().getName(), false);
          }
        }));

    updateTree(null, false);
  }

  @Override
  protected void onReveal() {
    updateTree(null, true);
  }

  private void updateTree(final String datasourceName, final boolean keepCurrentSelection) {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
        .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
          @Override
          public void onResource(Response response, JsArray<DatasourceDto> datasources) {
            if(datasources != null) {
              ArrayList<TreeItem> items = new ArrayList<TreeItem>(datasources.length());
              addDatasources(datasources, items);
              getView().setItems(items);
              if(!keepCurrentSelection) {
                if(datasourceName != null) {
                  getView().selectDatasource(datasourceName, true);
                } else {
                  getView().selectFirstDatasource(true);
                }
              }
            }
          }

          private void addDatasources(JsArray<DatasourceDto> datasources, ArrayList<TreeItem> items) {
            for(int i = 0; i < datasources.length(); i++) {
              DatasourceDto ds = datasources.get(i);
              TreeItem dsItem = new TreeItem(ds.getName());
              dsItem.addStyleName("magma-datasource");
              dsItem.setUserObject(ds);
              addTables(ds, dsItem);
              items.add(dsItem);
            }
          }

          private void addTables(DatasourceDto ds, TreeItem dsItem) {
            JsArrayString array = ds.getTableArray();
            if(array != null) {
              for(int j = 0; j < array.length(); j++) {
                String tableName = array.get(j);
                TreeItem tItem = dsItem.addItem(tableName);
                tItem.addStyleName("magma-table");
                if(isView(ds, tableName)) {
                  tItem.addStyleName("magma-view");
                }
              }
            }
          }
        }).send();
  }

  private boolean isView(DatasourceDto ds, String tableName) {
    JsArrayString array = ds.getViewArray();
    if(array == null) return false;
    for(int j = 0; j < array.length(); j++) {
      if(tableName.equals(array.get(j))) return true;
    }
    return false;
  }

  //
  // Interfaces and classes
  //

  class TreeSelectionHandler implements SelectionHandler<TreeItem> {
    @Override
    public void onSelection(SelectionEvent<TreeItem> event) {
      TreeItem item = event.getSelectedItem();

      if(item.getParentItem() == null) {
        fireDatasourceSelectionChangeEvent(item);
      } else {
        fireTableSelectionChangeEvent(item);
      }
    }

    private void fireDatasourceSelectionChangeEvent(TreeItem item) {
      UriBuilder ub = UriBuilder.create().segment("datasource", item.getText());
      ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
          .withCallback(new ResourceCallback<DatasourceDto>() {
            @Override
            public void onResource(Response response, DatasourceDto resource) {
              getEventBus().fireEvent(new DatasourceSelectionChangeEvent(resource));
            }
          }).send();
    }

    private void fireTableSelectionChangeEvent(final TreeItem item) {
      UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}");
      ResourceRequestBuilderFactory.<TableDto>newBuilder()
          .forResource(ub.build(item.getParentItem().getText(), item.getText())).get()
          .withCallback(new ResourceCallback<TableDto>() {
            @Override
            public void onResource(Response response, TableDto resource) {
              TreeItem parentItem = item.getParentItem();
              int index = parentItem.getChildIndex(item);
              String previous = null;
              if(index > 0) {
                previous = item.getParentItem().getChild(index - 1).getText();
              }
              String next = null;
              if(index < parentItem.getChildCount() - 1) {
                next = item.getParentItem().getChild(index + 1).getText();
              }
              getEventBus()
                  .fireEvent(new TableSelectionChangeEvent(NavigatorTreePresenter.this, resource, previous, next));
            }
          }).send();
    }
  }

  @ProxyStandard
  @NameToken(Places.navigator)
  public interface Proxy extends ProxyPlace<NavigatorTreePresenter> {

  }

  public interface Display extends View {

    HasSelectionHandlers<TreeItem> getTree();

    void setItems(List<TreeItem> items);

    void clear();

    void selectFirstDatasource(boolean fireEvent);

    void selectTable(String datasourceName, String tableName, boolean fireEvent);

    void selectDatasource(String datasourceName, boolean fireEvent);

    boolean hasDatasource(String datasourceName);

  }
}
