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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.inject.Inject;

public class FunctionalUnitListPresenter extends WidgetPresenter<FunctionalUnitListPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void setFunctionalUnits(JsArray<FunctionalUnitDto> templates);

    FunctionalUnitDto getSelectedFunctionalUnit();

    HandlerRegistration addSelectFunctionalUnitHandler(SelectionChangeHandler handler);

  }

  @Inject
  public FunctionalUnitListPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
    refreshFunctionalUnits();
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    refreshFunctionalUnits();
    addHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void refreshFunctionalUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new FunctionalUnitsResourceCallback()).send();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addSelectFunctionalUnitHandler(new FunctionalUnitSelectionChangeHandler()));
    super.registerHandler(eventBus.addHandler(FunctionalUnitDeletedEvent.getType(), new FunctionalUnitDeletedHandler()));
    super.registerHandler(eventBus.addHandler(FunctionalUnitCreatedEvent.getType(), new FunctionalUnitCreatedHandler()));
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

  private class FunctionalUnitSelectionChangeHandler implements SelectionChangeHandler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      FunctionalUnitDto selectedFunctionalUnit = getDisplay().getSelectedFunctionalUnit();
      eventBus.fireEvent(new FunctionalUnitSelectedEvent(selectedFunctionalUnit));
    }

  }

  private class FunctionalUnitsResourceCallback implements ResourceCallback<JsArray<FunctionalUnitDto>> {

    @Override
    public void onResource(Response response, JsArray<FunctionalUnitDto> resource) {
      JsArray<FunctionalUnitDto> units = JsArrays.toSafeArray(resource);
      getDisplay().setFunctionalUnits(units);
    }
  }

}
