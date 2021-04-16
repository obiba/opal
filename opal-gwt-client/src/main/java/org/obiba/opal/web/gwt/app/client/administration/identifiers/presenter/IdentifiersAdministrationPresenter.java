/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.event.IdentifiersTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class IdentifiersAdministrationPresenter extends
    ItemAdministrationPresenter<IdentifiersAdministrationPresenter.Display, IdentifiersAdministrationPresenter.Proxy>
    implements IdentifiersAdministrationUiHandlers {

  private final IdentifiersTablePresenter identifiersTablePresenter;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final ModalProvider<IdentifiersTableModalPresenter> identifiersTableModalProvider;

  @Inject
  public IdentifiersAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy,
      IdentifiersTablePresenter identifiersTablePresenter,
      ModalProvider<IdentifiersTableModalPresenter> identifiersTableModalProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.identifiersTablePresenter = identifiersTablePresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.identifiersTableModalProvider = identifiersTableModalProvider;
    this.identifiersTableModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test access to the identifiers tables
    String uri = UriBuilders.IDENTIFIERS_TABLES.create().build();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).get().authorize(authorizer).send();
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    authorize(event.getHasAuthorization());
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageIdentifiersMappingTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot("Table", identifiersTablePresenter);
    addRegisteredHandler(IdentifiersTableSelectionEvent.getType(),
        new IdentifiersTableSelectionEvent.IdentifiersTableSelectionHandler() {
          @Override
          public void onIdentifiersTableSelection(IdentifiersTableSelectionEvent event) {
            refreshAndSelect(event.getDto().getEntityType());
          }
        });
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    refresh();
  }

  @Override
  public void onSelection(TableDto identifiersTable) {
    identifiersTablePresenter.showIdentifiersTable(identifiersTable);
  }

  @Override
  public void onAddIdentifiersTable() {
    identifiersTableModalProvider.get();
  }

  //
  // Private methods
  //

  private void refresh() {
    refreshAndSelect(null);
  }

  private void refreshAndSelect(final String entityType) {
    String uri = UriBuilders.IDENTIFIERS_TABLES.create().query("counts", "true").build();
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
            getView().showIdentifiersTables(tables, entityType);
          }
        }) //
        .get().send();
  }

  @ProxyStandard
  @NameToken(Places.IDENTIFIERS)
  public interface Proxy extends ProxyPlace<IdentifiersAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<IdentifiersAdministrationUiHandlers> {

    void showIdentifiersTables(JsArray<TableDto> identifiersTables, String selectEntityType);

  }

}
