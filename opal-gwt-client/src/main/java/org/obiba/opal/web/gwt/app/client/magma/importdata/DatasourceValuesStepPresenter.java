/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata;

import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.importdata.DatasourceValuesStepPresenter.Display.Slots;
import org.obiba.opal.web.gwt.app.client.magma.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 *
 */
public class DatasourceValuesStepPresenter extends PresenterWidget<DatasourceValuesStepPresenter.Display> {

  private enum State {
    VALID,
    INVALID
  };


  private final ValuesTablePresenter valuesTablePresenter;

  private JsArray<TableDto> tables;

  private State state = State.VALID;

  @Inject
  public DatasourceValuesStepPresenter(Display display, EventBus eventBus, ValuesTablePresenter valuesTablePresenter) {
    super(eventBus, display);
    this.valuesTablePresenter = valuesTablePresenter;
    valuesTablePresenter.setViewMode(ValuesTablePresenter.ViewMode.SIMPLE_MODE);
    eventBus.addHandlerToSource(NotificationEvent.getType(), valuesTablePresenter, new NotificationEvent.Handler() {
      @Override
      public void onUserMessage(NotificationEvent event) {
        if(event.getNotificationType() == NotificationPresenter.NotificationType.ERROR) {
          state = State.INVALID;
          fireEvent(event);
        }
      }
    });
  }

  public void setDatasource(String datasource, final Collection<String> tableNames) {
    state = State.VALID;
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
        .forResource(UriBuilders.DATASOURCE_TABLES.create().query("counts", "true").build(datasource)) //
        .get() //
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {

          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            tables = JsArrays.toSafeArray(resource);
            if(tableNames == null || tableNames.isEmpty()) {
              getView().setTables(tables);
            } else {
              JsArray<TableDto> filteredTables = JsArrays.create();
              for(TableDto table : JsArrays.toIterable(tables)) {
                if(tableNames.contains(table.getName())) {
                  filteredTables.push(table);
                }
              }
              getView().setTables(filteredTables);
            }
          }

        }).send();
  }

  //
  // Widget presenter methods
  //

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Slots.Values, valuesTablePresenter);
    getView().setTableSelectionHandler(new TableSelectionHandler() {

      @Override
      public void onTableSelection(TableDto table) {
        valuesTablePresenter.setTable(table);
        valuesTablePresenter.updateValuesDisplay("");
      }
    });
  }

  public boolean isValid() {
    return state == State.VALID;
  }

  //
  // Inner classes and interfaces
  //
  public interface Display extends com.gwtplatform.mvp.client.View {

    enum Slots {
      Values
    }

    void setTables(JsArray<TableDto> resource);

    void setTableSelectionHandler(TableSelectionHandler handler);

    void showErrors(ClientErrorDto errorDto);

    void hideErrors();

  }

  public interface TableSelectionHandler {

    void onTableSelection(TableDto table);

  }

}
