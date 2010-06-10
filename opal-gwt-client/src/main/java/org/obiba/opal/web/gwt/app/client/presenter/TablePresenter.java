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

import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.TableDto;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

public class TablePresenter extends WidgetPresenter<TablePresenter.Display> {

  public interface Display extends WidgetDisplay {

    SelectionModel<VariableDto> getTableSelection();

    void renderRows(JsArray<VariableDto> rows);

    void clear();

    Label getTableName();

    Label getVariableCountLabel();

    Label getEntityTypeLabel();
  }

  private JsArray<VariableDto> variables;

  /**
   * @param display
   * @param eventBus
   */
  @Inject
  public TablePresenter(final Display display, final EventBus eventBus) {
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
        if(event.getSelection().getParentItem() != null) {
          String datasource = event.getSelection().getParentItem().getText();
          String table = event.getSelection().getText();
          getDisplay().getTableName().setText(table);
          updateEntityType(datasource, table);
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
  }

  @Override
  public void revealDisplay() {
  }

  private void updateTable(String datasource, String table) {
    ResourceRequestBuilderFactory.<JsArray<VariableDto>> newBuilder().forResource("/datasource/" + datasource + "/table/" + table + "/variables").get().withCallback(new ResourceCallback<JsArray<VariableDto>>() {
      @Override
      public void onResource(Response response, JsArray<VariableDto> resource) {
        variables = resource;
        getDisplay().renderRows(variables);
        getDisplay().getVariableCountLabel().setText(Integer.toString(variables.length()));
      }

    }).send();
  }

  private void updateEntityType(String datasource, String table) {
    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/datasource/" + datasource + "/table/" + table).get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        getDisplay().getEntityTypeLabel().setText(resource.getEntityType());
      }

    }).send();
  }

}
