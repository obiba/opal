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
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SplitPaneWorkbenchPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.app.client.wizard.mapidentifiers.presenter.IdentifiersMapPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.syncidentifiers.presenter.IdentifiersSyncPresenter;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class FunctionalUnitPresenter
    extends SplitPaneWorkbenchPresenter<FunctionalUnitPresenter.Display, FunctionalUnitPresenter.Proxy> {

  public interface Display extends View, BreadcrumbDisplay {

    HandlerRegistration addFunctionalUnitClickHandler(ClickHandler handler);

    HandlerRegistration addExportIdentifiersClickHandler(ClickHandler handler);

    HandlerRegistration addImportIdentifiersClickHandler(ClickHandler handler);

    HandlerRegistration addSyncIdentifiersClickHandler(ClickHandler handler);

    HasAuthorization getAddFunctionalUnitAuthorizer();

    HasAuthorization getExportIdentifiersAuthorizer();

    HasAuthorization getImportIdentifiersAuthorizer();

    HasAuthorization getSyncIdentifiersAuthorizer();
  }

  @ProxyStandard
  @NameToken(Places.units)
  public interface Proxy extends ProxyPlace<FunctionalUnitPresenter> {}

  final FunctionalUnitDetailsPresenter functionalUnitDetailsPresenter;

  final FunctionalUnitListPresenter functionalUnitListPresenter;

  final FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter;

  @Inject
  public FunctionalUnitPresenter(Display display, EventBus eventBus, Proxy proxy,
      FunctionalUnitDetailsPresenter FunctionalUnitDetailsPresenter,
      FunctionalUnitListPresenter FunctionalUnitListPresenter,
      FunctionalUnitUpdateDialogPresenter FunctionalUnitUpdateDialogPresenter) {
    super(eventBus, display, proxy);
    functionalUnitDetailsPresenter = FunctionalUnitDetailsPresenter;
    functionalUnitListPresenter = FunctionalUnitListPresenter;
    functionalUnitUpdateDialogPresenter = FunctionalUnitUpdateDialogPresenter;
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  public String getTitle() {
    return translations.pageFunctionalUnitTitle();
  }

  @Override
  protected PresenterWidget<?> getDefaultPresenter(SplitPaneWorkbenchPresenter.Slot slot) {
    switch(slot) {
      case CENTER:
        return functionalUnitDetailsPresenter;
      case LEFT:
        return functionalUnitListPresenter;
    }
    return null;
  }

  @Override
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

  @Override
  protected void addHandlers() {
    registerHandler(getView().addFunctionalUnitClickHandler(new AddFunctionalUnitClickHandler()));
    registerHandler(getView().addExportIdentifiersClickHandler(new ExportIdentifiersClickHandler()));
    registerHandler(getView().addImportIdentifiersClickHandler(new ImportIdentifiersClickHandler()));
    registerHandler(getView().addSyncIdentifiersClickHandler(new SyncIdentifiersClickHandler()));
  }

  //
  // Inner classes
  //

  public class AddFunctionalUnitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      functionalUnitUpdateDialogPresenter.setDialogMode(Mode.CREATE);
      functionalUnitUpdateDialogPresenter.getView().clear();
      addToPopupSlot(functionalUnitUpdateDialogPresenter);
    }

  }

  public class ExportIdentifiersClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      getEventBus().fireEvent(new FileDownloadEvent("/functional-units/entities/csv"));
    }

  }

  private final class ImportIdentifiersClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent arg0) {
      getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersMapPresenter.WizardType));
    }
  }

  private final class SyncIdentifiersClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent arg0) {
      getEventBus().fireEvent(new WizardRequiredEvent(IdentifiersSyncPresenter.WizardType));
    }
  }

}
