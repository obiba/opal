/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.ValuesTablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DatasourceValuesStepPresenter.Display.Slots;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class DatasourceValuesStepPresenter extends PresenterWidget<DatasourceValuesStepPresenter.Display> {

  private final ValuesTablePresenter valuesTablePresenter;

  private JsArray<TableDto> tables;

  @Inject
  public DatasourceValuesStepPresenter(Display display, final EventBus eventBus, ValuesTablePresenter valuesTablePresenter) {
    super(eventBus, display);
    this.valuesTablePresenter = valuesTablePresenter;
  }

  public void setDatasource(DatasourceDto datasource) {
    setDatasource(datasource.getName());
  }

  public void setDatasource(String datasource) {
    ResourceRequestBuilderFactory.<JsArray<TableDto>> newBuilder().forResource("/datasource/" + datasource + "/tables").get().withCallback(new ResourceCallback<JsArray<TableDto>>() {

      @Override
      public void onResource(Response response, JsArray<TableDto> resource) {
        tables = JsArrays.toSafeArray(resource);
        getView().setTables(tables);
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
      }
    });
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends View, WizardStepDisplay {

    enum Slots {
      Values;
    }

    void setTables(JsArray<TableDto> resource);

    void setTableSelectionHandler(TableSelectionHandler handler);

  }

  public interface TableSelectionHandler {

    public void onTableSelection(TableDto table);

  }

}
