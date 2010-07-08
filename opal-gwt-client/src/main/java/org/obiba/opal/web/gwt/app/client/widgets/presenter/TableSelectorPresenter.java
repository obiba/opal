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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.SelectionType;
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

  private SelectionType selectionType = SelectionType.MULTIPLE;

  private List<DatasourceDto> datasources;

  private Object callSource;

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
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        initDatasource(resource);

        getDisplay().setSelectionType(selectionType);
        getDisplay().renderDatasources(datasources);
        getDisplay().showDialog();
      }

    }).send();
  }

  @Override
  public void refreshDisplay() {
    ResourceRequestBuilderFactory.<JsArray<DatasourceDto>> newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
      @Override
      public void onResource(Response response, JsArray<DatasourceDto> resource) {
        initDatasource(resource);

        getDisplay().setSelectionType(selectionType);
        getDisplay().renderDatasources(datasources);
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

  public void setSelectionType(SelectionType tableSelectionType) {
    this.selectionType = tableSelectionType;
  }

  private void initDatasource(JsArray<DatasourceDto> resource) {
    datasources = new ArrayList<DatasourceDto>();
    for(int i = 0; i < resource.length(); i++) {
      DatasourceDto d = resource.get(i);
      if(d.getTableArray().length() > 0) {
        datasources.add(d);
      }
    }
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(TableSelectionRequiredEvent.getType(), new TableSelectionRequiredEvent.Handler() {

      public void onTableSelectionRequired(TableSelectionRequiredEvent event) {
        callSource = event.getSource();
        setSelectionType(event.getSelectionType());
        revealDisplay();
      }
    }));

    super.registerHandler(getDisplay().getDatasourceList().addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        // get selected datasource name
        getDisplay().renderTables(datasources.get(getDisplay().getSelectedDatasourceIndex()));
      }

    }));

    super.registerHandler(getDisplay().getSelectButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        DatasourceDto selectedDatasource = datasources.get(getDisplay().getSelectedDatasourceIndex());
        if(getDisplay().getSelectedTableIndices().size() > 0) {
          ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + selectedDatasource.getName() + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
            @Override
            public void onResource(Response response, JsArray<TableDto> resource) {
              List<TableDto> selectedTables = new ArrayList<TableDto>();
              // table names and table dtos are both in alphabetical order
              for(Integer idx : getDisplay().getSelectedTableIndices()) {
                selectedTables.add(resource.get(idx));
              }
              eventBus.fireEvent(new TableSelectionEvent(callSource, selectedTables));
            }

          }).send();
        }
      }
    }));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void showDialog();

    void hideDialog();

    HasChangeHandlers getDatasourceList();

    HasClickHandlers getSelectButton();

    int getSelectedDatasourceIndex();

    List<Integer> getSelectedTableIndices();

    void setSelectionType(SelectionType mode);

    void renderDatasources(List<DatasourceDto> datasources);

    void renderTables(DatasourceDto datasource);

  }
}
