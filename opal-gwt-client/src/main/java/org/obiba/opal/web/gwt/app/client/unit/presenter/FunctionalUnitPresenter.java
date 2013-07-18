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

import org.obiba.opal.web.gwt.app.client.administration.presenter.BreadcrumbDisplay;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitCreatedEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.FunctionalUnitDeletedEvent;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.presenter.IdentifiersSyncPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FunctionalUnitPresenter extends Presenter<FunctionalUnitPresenter.Display, FunctionalUnitPresenter.Proxy>
    implements FunctionalUnitsUiHandlers {


  public interface Display extends View, HasUiHandlers<FunctionalUnitsUiHandlers>, BreadcrumbDisplay {
    HasAuthorization getAddFunctionalUnitAuthorizer();
    HasAuthorization getExportIdentifiersAuthorizer();
    HasAuthorization getImportIdentifiersAuthorizer();
    HasAuthorization getSyncIdentifiersAuthorizer();
    void setFunctionalUnits(JsArray<FunctionalUnitDto> templates);
  }

  protected static final Translations translations = GWT.create(Translations.class);

  @ProxyStandard
  @NameToken(Places.units)
  public interface Proxy extends ProxyPlace<FunctionalUnitPresenter> {}

  final FunctionalUnitDetailsPresenter functionalUnitDetailsPresenter;
  final FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter;
  private final PlaceManager placeManager;

  @Inject
  public FunctionalUnitPresenter(Display display, EventBus eventBus, Proxy proxy,
      FunctionalUnitDetailsPresenter FunctionalUnitDetailsPresenter,
      FunctionalUnitUpdateDialogPresenter FunctionalUnitUpdateDialogPresenter,
      PlaceManager placeManager) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
    getView().setUiHandlers(this);
    functionalUnitDetailsPresenter = FunctionalUnitDetailsPresenter;
    functionalUnitUpdateDialogPresenter = FunctionalUnitUpdateDialogPresenter;
    this.placeManager = placeManager;
  }

  @Override
  public void addUnit() {
    functionalUnitUpdateDialogPresenter.setDialogMode(Mode.CREATE);
    functionalUnitUpdateDialogPresenter.getView().clear();
    addToPopupSlot(functionalUnitUpdateDialogPresenter);
  }

  @Override
  public void exportIdentifiers() {
    getEventBus().fireEvent(new FileDownloadEvent("/functional-units/entities/csv"));
  }

  @Override
  public void importIdentifiers() {
    getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersMapPresenter.WizardType));
  }

  @Override
  public void synchronizeIdentifiers() {
    getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersSyncPresenter.WizardType));
  }

  @Override
  public void selectUnit(FunctionalUnitDto dto) {
    PlaceRequest.Builder requestBuilder = new PlaceRequest.Builder();
    requestBuilder.nameToken(Places.unit).with("name", dto.getName());
    placeManager.revealPlace(requestBuilder.build());

//    getEventBus().fireEvent(new PlaceChangeEvent(Places.unitPlace.addParam()));
  }

  @TitleFunction
  public String getPageTitle(PlaceRequest request) {
    return translations.pageFunctionalUnitTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    authorize();
    refreshFunctionalUnits();
  }

  @Override
  protected void onBind() {
    super.onBind();
    registerHandler(getEventBus().addHandler(FunctionalUnitDeletedEvent.getType(), new FunctionalUnitDeletedHandler()));
    registerHandler(getEventBus().addHandler(FunctionalUnitCreatedEvent.getType(), new FunctionalUnitCreatedHandler()));
  }

//  @Override
  public String getTitle() {
    return translations.pageFunctionalUnitTitle();
  }

  @Override
  protected void onReset() {
    super.onReset();
  }


  //  @Override
  protected void authorize() {
    // create unit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").post()
        .authorize(getView().getAddFunctionalUnitAuthorizer()).send();
    // export all identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units/entities/csv").get()
        .authorize(getView().getExportIdentifiersAuthorizer()).send();
    // map identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units/entities/table").get()//
        .authorize(CascadingAuthorizer.newBuilder()//
            .and("/functional-units/entities/identifiers/map/units", HttpMethod.GET)//
            .authorize(getView().getImportIdentifiersAuthorizer()).build())//
        .send();
    // sync identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units/entities/sync").post()
        .authorize(getView().getSyncIdentifiersAuthorizer()).send();
  }

  private void refreshFunctionalUnits() {
    ResourceRequestBuilderFactory.<JsArray<FunctionalUnitDto>>newBuilder().forResource("/functional-units").get()
        .withCallback(new FunctionalUnitsResourceCallback()).send();
  }

  //
  // Inner classes
  //
  private class FunctionalUnitsResourceCallback implements ResourceCallback<JsArray<FunctionalUnitDto>> {

    @Override
    public void onResource(Response response, JsArray<FunctionalUnitDto> resource) {
      JsArray<FunctionalUnitDto> units = JsArrays.toSafeArray(resource);
      getView().setFunctionalUnits(units);
    }
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
      // Get back to units to show the remaining units
      placeManager.revealPlace(new PlaceRequest.Builder().nameToken(Places.units).build());
    }

  }

}
