package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class IdentifiersAdministrationPresenter extends
    ItemAdministrationPresenter<IdentifiersAdministrationPresenter.Display, IdentifiersAdministrationPresenter.Proxy> implements
IdentifiersAdministrationUiHandlers {

  private final IdentifiersTablePresenter identifiersTablePresenter;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public IdentifiersAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy,
      IdentifiersTablePresenter identifiersTablePresenter,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.identifiersTablePresenter = identifiersTablePresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test access to the identifiers tables
    String uri = UriBuilder.create().segment("identifiers","tables").build();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(uri).get()
        .authorize(authorizer).send();
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
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    String uri = UriBuilder.create().segment("identifiers","tables").query("counts", "true").build();
    ResourceRequestBuilderFactory.<JsArray<TableDto>>newBuilder() //
        .forResource(uri) //
        .withCallback(new ResourceCallback<JsArray<TableDto>>() {
          @Override
          public void onResource(Response response, JsArray<TableDto> resource) {
            JsArray<TableDto> tables = JsArrays.toSafeArray(resource);
            getView().showIdentifiersTables(tables);
          }
        }) //
        .get().send();
  }

  @Override
  public void onSelection(TableDto identifiersTable) {
    identifiersTablePresenter.showIdentifiersTable(identifiersTable);
  }

  @ProxyStandard
  @NameToken(Places.IDENTIFIERS)
  public interface Proxy extends ProxyPlace<IdentifiersAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<IdentifiersAdministrationUiHandlers> {

    void showIdentifiersTables(JsArray<TableDto> identifiersTables);

  }

}
