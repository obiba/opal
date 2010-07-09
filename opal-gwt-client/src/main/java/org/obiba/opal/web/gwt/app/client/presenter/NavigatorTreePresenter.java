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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionExpiredEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.inject.Inject;

/**
 * Presenter for a Tree displaying Opal datasources and tables.
 */
public class NavigatorTreePresenter extends WidgetPresenter<NavigatorTreePresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasSelectionHandlers<TreeItem> getTree();

    void setItems(List<TreeItem> items);

    void clear();

    void selectFirstDatasource();

    void selectTable(String datasourceName, String tableName);

    void selectDatasource(String datasourceName);

  }

  @Inject
  public NavigatorTreePresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    super.registerHandler(getDisplay().getTree().addSelectionHandler(new SelectionHandler<TreeItem>() {
      @Override
      public void onSelection(SelectionEvent<TreeItem> event) {
        eventBus.fireEvent(new NavigatorSelectionChangeEvent(event.getSelectedItem()));
      }
    }));

    super.registerHandler(eventBus.addHandler(SessionCreatedEvent.getType(), new SessionCreatedEvent.Handler() {
      @Override
      public void onSessionCreated(SessionCreatedEvent event) {
        refreshDisplay();
      }
    }));

    super.registerHandler(eventBus.addHandler(SessionExpiredEvent.getType(), new SessionExpiredEvent.Handler() {
      @Override
      public void onSessionExpired(SessionExpiredEvent event) {
        getDisplay().clear();
      }
    }));

    super.registerHandler(eventBus.addHandler(TableSelectionChangeEvent.getType(), new TableSelectionChangeEvent.Handler() {

      @Override
      public void onNavigatorSelectionChanged(TableSelectionChangeEvent event) {
        getDisplay().selectTable(event.getSelection().getDatasourceName(), event.getSelection().getName());
      }

    }));

    super.registerHandler(eventBus.addHandler(DatasourceSelectionChangeEvent.getType(), new DatasourceSelectionChangeEvent.Handler() {

      @Override
      public void onNavigatorSelectionChanged(DatasourceSelectionChangeEvent event) {
        getDisplay().selectDatasource(event.getSelection().getName());
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
    updateTree();
  }

  @Override
  public void revealDisplay() {
    updateTree();
  }

  private void updateTree() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
        if(datasources != null) {
          ArrayList<TreeItem> items = new ArrayList<TreeItem>(datasources.length());
          for(int i = 0; i < datasources.length(); i++) {
            DatasourceDto ds = datasources.get(i);
            TreeItem dsItem = new TreeItem(ds.getName());
            dsItem.setUserObject(ds);
            JsArrayString array = ds.getTableArray();
            if(array != null) {
              for(int j = 0; j < array.length(); j++) {
                dsItem.addItem(array.get(j));
              }
            }
            items.add(dsItem);
          }
          getDisplay().setItems(items);
          getDisplay().selectFirstDatasource();
        }
      }
    }).send();
  }
}
