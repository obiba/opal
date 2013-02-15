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

import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportConfig;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class UnitSelectionStepPresenter extends PresenterWidget<UnitSelectionStepPresenter.Display> {

  @Inject
  public UnitSelectionStepPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
    initUnits();
  }

  protected void addEventHandlers() {
    registerHandler(getView().addIdentifierAsIsClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().setUnitEnabled(false);
        getView().setIncrementalEnabled(true);
      }
    }));
    registerHandler(getView().addIdentifierSharedWithUnitClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().setUnitEnabled(true);
        getView().setIncrementalEnabled(false);
      }
    }));
  }

  public void updateImportData(ImportConfig importConfig) {
    boolean withUnit = getView().isIdentifierSharedWithUnit();
    importConfig.setIdentifierSharedWithUnit(withUnit);
    importConfig.setIdentifierAsIs(!withUnit);
    importConfig.setUnit(withUnit ? getView().getSelectedUnit() : null);
    importConfig.setIncremental(!withUnit && getView().isIncremental());
  }

  public void initUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource("/functional-units").get()
        .withCallback(new ResourceCallback<JsArray<FunctionalUnitDto>>() {
          @Override
          public void onResource(Response response, JsArray<FunctionalUnitDto> units) {
            getView().setUnits(units);
          }
        }).send();
  }

  public interface Display extends View {

    boolean isIdentifierSharedWithUnit();

    void setUnits(JsArray<FunctionalUnitDto> units);

    String getSelectedUnit();

    HandlerRegistration addIdentifierAsIsClickHandler(ClickHandler handler);

    HandlerRegistration addIdentifierSharedWithUnitClickHandler(ClickHandler handler);

    void setUnitEnabled(boolean enabled);

    boolean isIncremental();

    void setIncrementalEnabled(boolean enabled);
  }

}
