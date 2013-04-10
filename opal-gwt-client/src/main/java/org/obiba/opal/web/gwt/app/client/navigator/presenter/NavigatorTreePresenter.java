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

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
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
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

    // datasource selection
    registerHandler(getEventBus()
        .addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

          @Override
          public void onDatasourceSelectionChanged(DatasourceSelectionChangeEvent event) {
            getView().selectDatasource(event.getSelection(), false);
          }

        }));

    // table selection
    registerHandler(
        getEventBus().addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

          @Override
          public void onTableSelectionChanged(TableSelectionChangeEvent event) {
            getView().selectTable(event.getDatasourceName(), event.getTableName(), false);
          }

        }));

    // variable selection
    registerHandler(
        getEventBus().addHandler(VariableSelectionChangeEvent.getType(), new VariableSelectionChangeEvent.Handler() {

          @Override
          public void onVariableSelectionChanged(VariableSelectionChangeEvent event) {
            getView().selectVariable(event.getTable(), event.getSelection(), false);

          }
        }));

    getView().setDatasourceClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UriBuilder ub = UriBuilder.create().segment("datasource", getView().getDatasourceName());
        ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<DatasourceDto>() {
              @Override
              public void onResource(Response response, DatasourceDto resource) {
                getEventBus().fireEvent(new DatasourceSelectionChangeEvent(resource));
              }
            }).send();
      }
    });

    getView().setTableClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UriBuilder ub = UriBuilder.create().segment("datasource", getView().getDatasourceName());
        ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().forResource(ub.build()).get()
            .withCallback(new ResourceCallback<DatasourceDto>() {
              @Override
              public void onResource(Response response, DatasourceDto resource) {
                fireTableSelection(resource);
              }
            }).send();
      }

      private void fireTableSelection(final DatasourceDto datasource) {
        final String tName = getView().getTableName();
        UriBuilder ub = UriBuilder.create().segment("datasource", "{}", "table", "{}");
        ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(ub.build(datasource.getName(), tName)).get()
            .withCallback(new ResourceCallback<TableDto>() {
              @Override
              public void onResource(Response response, TableDto resource) {
                int index = -1;
                JsArrayString tables = JsArrays.toSafeArray(datasource.getTableArray());
                for(int i = 0; index < tables.length(); i++) {
                  if(tables.get(i).equals(tName)) {
                    index = i;
                    break;
                  }
                }
                String previous = null;
                if(index > 0) {
                  previous = tables.get(index - 1);
                }
                String next = null;
                if(index < tables.length() - 1) {
                  next = tables.get(index + 1);
                }
                getEventBus()
                    .fireEvent(new TableSelectionChangeEvent(resource, previous, next));
              }
            }).send();
      }
    });
  }

  //
  // Interfaces and classes
  //

  @ProxyStandard
  @NameToken(Places.navigator)
  public interface Proxy extends ProxyPlace<NavigatorTreePresenter> {

  }

  public interface Display extends View {

    void clear();

    void selectVariable(TableDto table, VariableDto variable, boolean fireEvent);

    void selectTable(String datasource, String table, boolean fireEvent);

    void selectDatasource(String datasource, boolean fireEvent);

    void setDatasourceClickHandler(ClickHandler handler);

    void setTableClickHandler(ClickHandler handler);

    String getDatasourceName();

    String getTableName();

  }
}
