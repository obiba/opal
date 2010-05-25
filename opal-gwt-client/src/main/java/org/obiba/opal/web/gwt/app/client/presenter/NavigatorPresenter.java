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

import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.inject.Inject;

/**
 *
 */
public class NavigatorPresenter extends WidgetPresenter<NavigatorPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HasSelectionHandlers<TreeItem> getTree();

    void setItems(List<TreeItem> items);

    SelectionModel<VariableDto> getTableSelection();

    void renderRows(JsArray<VariableDto> rows);
  }

  final private ResourceRequestBuilderFactory factory;

  private JsArray<VariableDto> variables;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public NavigatorPresenter(final Display display, final EventBus eventBus, final ResourceRequestBuilderFactory factory) {
    super(display, eventBus);
    this.factory = factory;
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

    // FIXME: this HandlerRegistration is not of the same type, so we can't pass it to our parent for unbinding.
    getDisplay().getTableSelection().addSelectionChangeHandler(new SelectionChangeHandler() {

      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        // VariableDto variable = getDisplay().getTableSelection();
        eventBus.fireEvent(new VariableSelectionChangeEvent(null));
      }
    });

    super.registerHandler(eventBus.addHandler(NavigatorSelectionChangeEvent.getType(), new NavigatorSelectionChangeEvent.Handler() {
      @Override
      public void onNavigatorSelectionChanged(NavigatorSelectionChangeEvent event) {
        if(event.getSelection().getParentItem() != null) {
          String datasource = event.getSelection().getParentItem().getText();
          String table = event.getSelection().getText();
          updateTable(datasource, table);
        }
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

  private void updateTable(String datasource, String table) {
    factory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + datasource + "/table/" + table + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {
      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        variables = resource;
        getDisplay().renderRows(variables);
      }

    }).send();
  }

  private void updateTree() {
    factory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> datasources) {
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
    }).send();
  }

}
