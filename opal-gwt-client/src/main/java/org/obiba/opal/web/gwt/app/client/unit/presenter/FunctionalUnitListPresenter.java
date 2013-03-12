/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.presenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class FunctionalUnitListPresenter extends PresenterWidget<FunctionalUnitListPresenter.Display> {

  public interface Display extends View {
    void setFunctionalUnits(JsArray<FunctionalUnitDto> templates);

    FunctionalUnitDto getSelectedFunctionalUnit();

    HandlerRegistration addSelectFunctionalUnitHandler(SelectionChangeEvent.Handler handler);

  }

  @Inject
  public FunctionalUnitListPresenter(final Display display, final EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  public void onReveal() {
    super.onReveal();
    refreshFunctionalUnits();
  }

  @Override
  protected void onBind() {
    super.onBind();
    refreshFunctionalUnits();
    addHandlers();
  }

  private void refreshFunctionalUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource("/functional-units").get()
        .withCallback(new FunctionalUnitsResourceCallback()).send();
  }

  private void addHandlers() {
    super.registerHandler(getView().addSelectFunctionalUnitHandler(new FunctionalUnitSelectionChangeHandler()));
    super.registerHandler(
        getEventBus().addHandler(FunctionalUnitDeletedEvent.getType(), new FunctionalUnitDeletedHandler()));
    super.registerHandler(
        getEventBus().addHandler(FunctionalUnitCreatedEvent.getType(), new FunctionalUnitCreatedHandler()));
  }

  private class FunctionalUnitCreatedHandler implements FunctionalUnitCreatedEvent.Handler {

    @Override
    public void onFunctionalUnitCreated(FunctionalUnitCreatedEvent event) {
      refreshFunctionalUnits();
    }

  }

  private class FunctionalUnitDeletedHandler implements FunctionalUnitDeletedEvent.Handler {

    @Override
    public void onFunctionalUnitDeleted(FunctionalUnitDeletedEvent event) {
      refreshFunctionalUnits();
    }

  }

  private class FunctionalUnitSelectionChangeHandler implements SelectionChangeEvent.Handler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      FunctionalUnitDto selectedFunctionalUnit = getView().getSelectedFunctionalUnit();
      getEventBus().fireEvent(new FunctionalUnitSelectedEvent(selectedFunctionalUnit));
    }

  }

  private class FunctionalUnitsResourceCallback implements ResourceCallback<JsArray<FunctionalUnitDto>> {

    @Override
    public void onResource(Response response, JsArray<FunctionalUnitDto> resource) {
      JsArray<FunctionalUnitDto> units = JsArrays.toSafeArray(resource);
      getView().setFunctionalUnits(units);
    }
  }

}
