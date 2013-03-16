/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.wizard.variablestoview.presenter;

import java.util.Set;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.event.CopyVariablesToViewEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;
import org.obiba.opal.web.model.client.opal.VariableCopyDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class VariablesToViewPresenter extends PresenterWidget<VariablesToViewPresenter.Display> {

  private TableDto table;

  private Set<VariableDto> variables;

  JsArray<DatasourceDto> datasources;

  @Inject
  public VariablesToViewPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

//  public void revealDisplay() {
//    getView().show();
//  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
  }

  private void addEventHandlers() {
    registerHandler(
        getEventBus().addHandler(CopyVariablesToViewEvent.getType(), new CopyVariablesToViewEventHandler()));

    registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // nothing yet
      }
    }));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));
  }

  class CopyVariablesToViewEventHandler implements CopyVariablesToViewEvent.Handler {

    private void refreshDatasources() {
      ResourceRequestBuilderFactory.<JsArray<DatasourceDto>>newBuilder().forResource("/datasources").get()
          .withCallback(new ResourceCallback<JsArray<DatasourceDto>>() {
            @Override
            public void onResource(Response response, JsArray<DatasourceDto> resource) {
              datasources = JsArrays.toSafeArray(resource);
              refreshDatasources(datasources);
            }
          }).send();
    }

    private void refreshDatasources(
        @SuppressWarnings("ParameterHidesMemberVariable") JsArray<DatasourceDto> datasources) {
      for(int i = 0; i < datasources.length(); i++) {
        DatasourceDto d = datasources.get(i);
        d.setTableArray(JsArrays.toSafeArray(d.getTableArray()));
        d.setViewArray(JsArrays.toSafeArray(d.getViewArray()));
      }

      getView().setDatasources(datasources, table.getDatasourceName());
    }

    @Override
    public void onVariableCopy(CopyVariablesToViewEvent event) {
      table = event.getTable();
      variables = event.getSelection();

      refreshDatasources();

      // Prepare the array of variableDto
      JsArray<VariableCopyDto> vars = JsArrays.create();
      for(VariableDto v : variables) {
        VariableCopyDto copy = VariableCopyDto.create();
        copy.setVariable(v.getName());
        copy.setTable(table.getName());
        copy.setDatasource(table.getDatasourceName());
        vars.push(copy);
      }
      getView().renderRows(vars);
      getView().showDialog();
    }
  }

  public interface Display extends PopupView {

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void showDialog();

    void hideDialog();

    void setDatasources(JsArray<DatasourceDto> datasources, String name);

    void renderRows(JsArray<VariableCopyDto> rows);

//    void setTableSelectionHandler(TableSelectionHandler handler);

  }

  public interface TableSelectionHandler {
    void onTableSelected(String datasource, String table);
  }

}
