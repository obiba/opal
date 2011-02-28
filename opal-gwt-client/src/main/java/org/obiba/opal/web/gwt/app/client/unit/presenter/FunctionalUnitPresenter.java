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

import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;
import org.obiba.opal.web.gwt.app.client.unit.presenter.FunctionalUnitUpdateDialogPresenter.Mode;
import org.obiba.opal.web.gwt.app.client.wizard.WizardType;
import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;
import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CascadingAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class FunctionalUnitPresenter extends WidgetPresenter<FunctionalUnitPresenter.Display> {

  FunctionalUnitDetailsPresenter functionalUnitDetailsPresenter;

  FunctionalUnitListPresenter functionalUnitListPresenter;

  FunctionalUnitUpdateDialogPresenter functionalUnitUpdateDialogPresenter;

  public interface Display extends WidgetDisplay {
    ScrollPanel getFunctionalUnitDetailsPanel();

    ScrollPanel getFunctionalUnitListPanel();

    HandlerRegistration addFunctionalUnitClickHandler(ClickHandler handler);

    HandlerRegistration addExportIdentifiersClickHandler(ClickHandler handler);

    HandlerRegistration addImportIdentifiersClickHandler(ClickHandler handler);

    HasAuthorization getAddFunctionalUnitAuthorizer();

    HasAuthorization getExportIdentifiersAuthorizer();

    HasAuthorization getImportIdentifiersAuthorizer();
  }

  @Inject
  public FunctionalUnitPresenter(final Display display, final EventBus eventBus, FunctionalUnitDetailsPresenter FunctionalUnitDetailsPresenter, FunctionalUnitListPresenter FunctionalUnitListPresenter, FunctionalUnitUpdateDialogPresenter FunctionalUnitUpdateDialogPresenter) {
    super(display, eventBus);
    this.functionalUnitDetailsPresenter = FunctionalUnitDetailsPresenter;
    this.functionalUnitListPresenter = FunctionalUnitListPresenter;
    this.functionalUnitUpdateDialogPresenter = FunctionalUnitUpdateDialogPresenter;
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    authorize();
  }

  private void authorize() {
    // create unit
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").post().authorize(getDisplay().getAddFunctionalUnitAuthorizer()).send();
    // export all identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units/entities/csv").get().authorize(getDisplay().getExportIdentifiersAuthorizer()).send();
    // map identifiers
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units/entities/table").get()//
    .authorize(CascadingAuthorizer.newBuilder().request("/files/meta", HttpMethod.GET)//
    .request("/functional-units/entities/identifiers/map/units", HttpMethod.GET)//
    .authorize(getDisplay().getImportIdentifiersAuthorizer()).build())//
    .send();
  }

  @Override
  protected void onBind() {
    addHandlers();
    initDisplayComponents();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addFunctionalUnitClickHandler(new AddFunctionalUnitClickHandler()));
    super.registerHandler(getDisplay().addExportIdentifiersClickHandler(new ExportIdentifiersClickHandler()));
    super.registerHandler(getDisplay().addImportIdentifiersClickHandler(new ImportIdentifiersClickHandler()));
  }

  protected void initDisplayComponents() {

    getDisplay().getFunctionalUnitDetailsPanel().add(functionalUnitDetailsPresenter.getDisplay().asWidget());
    getDisplay().getFunctionalUnitListPanel().add(functionalUnitListPresenter.getDisplay().asWidget());

    functionalUnitListPresenter.bind();
    functionalUnitDetailsPresenter.bind();
  }

  @Override
  protected void onUnbind() {
    getDisplay().getFunctionalUnitDetailsPanel().remove(functionalUnitDetailsPresenter.getDisplay().asWidget());
    getDisplay().getFunctionalUnitListPanel().remove(functionalUnitListPresenter.getDisplay().asWidget());

    functionalUnitListPresenter.unbind();
    functionalUnitDetailsPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Inner classes
  //

  public class AddFunctionalUnitClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      functionalUnitUpdateDialogPresenter.bind();
      functionalUnitUpdateDialogPresenter.setDialogMode(Mode.CREATE);
      functionalUnitUpdateDialogPresenter.getDisplay().clear();
      // functionalUnitUpdateDialogPresenter.getDisplay().setEnabledFunctionalUnitName(true);
      functionalUnitUpdateDialogPresenter.revealDisplay();
    }

  }

  public class ExportIdentifiersClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      String url = new StringBuilder("/functional-units/entities/csv").toString();
      eventBus.fireEvent(new FileDownloadEvent(url));
    }

  }

  private final class ImportIdentifiersClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WizardRequiredEvent(WizardType.MAP_IDENTIFIERS));
    }
  }

}
