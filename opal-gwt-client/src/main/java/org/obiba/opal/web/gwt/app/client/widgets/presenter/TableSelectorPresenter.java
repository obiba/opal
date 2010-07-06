/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.DatasourceDto;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class TableSelectorPresenter extends WidgetPresenter<TableSelectorPresenter.Display> {

  //
  // Instance Variables
  //

  private TableSelectionType tableSelectionType = TableSelectionType.MULTIPLE;

  private JsArray<DatasourceDto> datasources;

  private JsArray<TableDto> tables;

  //
  // Constructors
  //

  @Inject
  public TableSelectorPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  @Override
  public void refreshDisplay() {
    refreshDatasources();
  }

  private void refreshDatasources() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        datasources = resource;
        getDisplay().setDatasources(resource);
        refreshTables(resource.get(0).getName());
      }

    }).send();
  }

  private void refreshTables(String datasourceName) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasourceName + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        tables = resource;
        getDisplay().setTables(resource);
      }

    }).send();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void setTableSelectionType(TableSelectionType tableSelectionType) {
    this.tableSelectionType = tableSelectionType;
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(TableSelectionRequiredEvent.getType(), new TableSelectionRequiredEvent.Handler() {

      public void onTableSelectionRequired(TableSelectionRequiredEvent event) {
        setTableSelectionType(event.getTableSelectionType());
        refreshDisplay();
        revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().getDatasourceList().addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        // get selected datasource name
        int datasourceIndex = getDisplay().getSelectedDatasourceIndex();
        if(datasourceIndex == -1) {
          // TODO
        } else {
          DatasourceDto selection = datasources.get(datasourceIndex);
          refreshTables(selection.getName());
        }
      }

    }));

    super.registerHandler(getDisplay().getSelectButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        // System.out.println(getDisplay().getSelectedDatasourceIndex());
        // System.out.println(getDisplay().getSelectedTableIndices());
      }
    }));
  }

  //
  // Inner Classes / Interfaces
  //

  public enum TableSelectionType {
    SINGLE, MULTIPLE
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    HasChangeHandlers getDatasourceList();

    HasClickHandlers getSelectButton();

    int getSelectedDatasourceIndex();

    List<Integer> getSelectedTableIndices();

    void setTableSelectionType(TableSelectionType mode);

    void setDatasources(JsArray<DatasourceDto> datasources);

    void setTables(JsArray<TableDto> tables);

  }
}
