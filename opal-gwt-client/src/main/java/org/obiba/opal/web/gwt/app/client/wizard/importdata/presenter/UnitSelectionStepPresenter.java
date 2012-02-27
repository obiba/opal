/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.wizard.WizardStepDisplay;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class UnitSelectionStepPresenter extends WidgetPresenter<UnitSelectionStepPresenter.Display> {

  public interface Display extends WidgetDisplay, WizardStepDisplay {

    boolean isIdentifierAsIs();

    void setIdentifierAsIs(boolean checked);

    boolean isIdentifierSharedWithUnit();

    void setIdentifierSharedWithUnit(boolean checked);

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

    void setSelectedUnit(String unit);

    HandlerRegistration addIdentifierAsIsClickHandler(ClickHandler handler);

    HandlerRegistration addIdentifierSharedWithUnitClickHandler(ClickHandler handler);

    void setUnitEnabled(boolean enabled);

    /** Allows the identity (unit) section of the form to be enabled and disabled. */
    void setIdentityEnabled(boolean enabled);

  }

  @Inject
  public UnitSelectionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    initUnits();
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addIdentifierAsIsClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().setUnitEnabled(false);
      }
    }));
    super.registerHandler(getDisplay().addIdentifierSharedWithUnitClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        getDisplay().setUnitEnabled(true);
      }
    }));
  }

  public void updateImportData(ImportData importData) {
    importData.setIdentifierAsIs(getDisplay().isIdentifierAsIs());
    importData.setIdentifierSharedWithUnit(getDisplay().isIdentifierSharedWithUnit());
    if(getDisplay().isIdentifierSharedWithUnit()) {
      importData.setUnit(getDisplay().getSelectedUnit());
    } else {
      importData.setUnit(null);
    }
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

  public void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>> newBuilder().forResource("/functional-units").get().withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
      @Override
      public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
        getDisplay().setUnits(units);
      }
    }).send();
  }

}
