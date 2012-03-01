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

import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableListUpdateEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.widgets.event.TableSelectionRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

/**
 *
 */
public class TableListPresenter extends WidgetPresenter<TableListPresenter.Display> {

  //
  // Instance Variables
  //

  private List<TableDto> tables = new ArrayList<TableDto>();

  private Runnable actionRequiringConfirmation;

  private String confirmationTitleKey;

  private String confirmationMessageKey;

  //
  // Constructors
  //

  @Inject
  public TableListPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
    initializeSuggestions();
  }

  @Override
  protected void onUnbind() {
    clear();
  }

  @Override
  public void revealDisplay() {
    initializeSuggestions();
  }

  @Override
  public void refreshDisplay() {
    initializeSuggestions();
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

  public void setRemoveButtonConfirmation(String confirmationTitleKey, String confirmationMessageKey) {
    this.confirmationTitleKey = confirmationTitleKey;
    this.confirmationMessageKey = confirmationMessageKey;
  }

  public void clearRemoveButtonConfirmation() {
    this.confirmationTitleKey = null;
    this.confirmationMessageKey = null;
  }

  public void selectDatasourceTables(String datasourceName) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasourceName + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        if(resource != null) {
          for(int i = 0; i < resource.length(); i++) {
            updateTables(resource.get(i));
          }
        }
      }

    }).send();
  }

  public void selectTable(final TableDto table) {
    selectTable(table.getDatasourceName(), table.getName());
  }

  public void selectTable(String datasourceName, String tableName) {
    ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource("/datasource/" + datasourceName + "/table/" + tableName).get().withCallback(new ResourceCallback<TableDto>() {
      @Override
      public void onResource(Response response, TableDto resource) {
        if(resource != null) {
          updateTables(resource);
        }
      }

    }).send();
  }

  public void addTable(TableDto table) {
    updateTables(table);
  }

  private boolean updateTables(TableDto selectedTable) {
    if(selectedTable == null) return false;

    boolean updated = false;
    boolean found = false;
    for(TableDto table : getTables()) {
      if(table.getName().equals(selectedTable.getName()) && table.getDatasourceName().equals(selectedTable.getDatasourceName())) {
        found = true;
        break;
      }
    }
    if(!found) {
      getTables().add(selectedTable);
      getDisplay().addTable(selectedTable);
      updated = true;
    }
    return updated;
  }

  public List<TableDto> getTables() {
    return tables;
  }

  public void clear() {
    tables.clear();
    getDisplay().clear();
  }

  private void addEventHandlers() {
    super.registerHandler(eventBus.addHandler(TableSelectionEvent.getType(), new TableSelectionEvent.Handler() {

      @Override
      public void onTableSelection(TableSelectionEvent event) {
        if(TableListPresenter.this.equals(event.getSource())) {
          boolean updated = false;
          for(TableDto selectedTable : event.getSelectedTables()) {
            if(updateTables(selectedTable)) {
              updated = true;
            }
          }
          if(updated) {
            eventBus.fireEvent(new TableListUpdateEvent(TableListPresenter.this));
          }
        }
      }
    }));

    super.registerHandler(getDisplay().addAddClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new TableSelectionRequiredEvent(TableListPresenter.this));
      }
    }));

    super.registerHandler(eventBus.addHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler()));
    if(confirmationTitleKey != null && confirmationMessageKey != null) {
      super.registerHandler(getDisplay().addRemoveClickHandler(new RemoveWithConfirmationClickHandler()));
    } else {
      super.registerHandler(getDisplay().addRemoveClickHandler(new RemoveClickHandler()));
    }
  }

  private void removeButtonAction() {
    List<Integer> selectedIndices = getDisplay().getSelectedIndices();
    for(int i = selectedIndices.size() - 1; i >= 0; i--) {
      getTables().remove(selectedIndices.get(i).intValue());
      getDisplay().removeTable(selectedIndices.get(i));
    }
    if(selectedIndices.size() > 0) {
      getDisplay().unselectAll(selectedIndices.get(0));
      eventBus.fireEvent(new TableListUpdateEvent(TableListPresenter.this));
    }
  }

  private void initializeSuggestions() {
    // getDisplay().clearSuggestions();
    // ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>
    // newBuilder().forResource("/datasources").get().withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
    // @Override
    // public void onResource(Response response, JsArray<DatasourceDto> resource) {
    // if(resource != null) {
    // for(int i = 0; i < resource.length(); i++) {
    // initializeSuggestions(resource.get(i));
    // }
    // }
    // }
    //
    // }).send();
  }

  private void initializeSuggestions(DatasourceDto datasource) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource(datasource.getLink() + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {
      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        if(resource != null) {
          for(int i = 0; i < resource.length(); i++) {
            getDisplay().suggestTable(resource.get(i));
          }
        }
      }
    }).send();
  }

  //
  // Inner Classes / Interfaces
  //

  class RemoveClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      removeButtonAction();
    }
  }

  class RemoveWithConfirmationClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      actionRequiringConfirmation = new Runnable() {
        public void run() {
          removeButtonAction();
        }
      };
      eventBus.fireEvent(new ConfirmationRequiredEvent(actionRequiringConfirmation, confirmationTitleKey, confirmationMessageKey));
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) && event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  public interface Display extends WidgetDisplay {

    HandlerRegistration addAddClickHandler(ClickHandler handler);

    void suggestTable(TableDto tableDto);

    void clearSuggestions();

    HandlerRegistration addRemoveClickHandler(ClickHandler handler);

    void setListWidth(String width);

    void setListVisibleItemCount(int count);

    void clear();

    void removeTable(int index);

    void addTable(TableDto table);

    List<Integer> getSelectedIndices();

    public void unselectAll(int first);

  }

}
