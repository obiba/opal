package org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.event.IdentifiersTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class IdentifiersAdministrationPresenter extends
    ItemAdministrationPresenter<IdentifiersAdministrationPresenter.Display, IdentifiersAdministrationPresenter.Proxy>
    implements IdentifiersAdministrationUiHandlers {

  private final IdentifiersTablePresenter identifiersTablePresenter;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final ModalProvider<IdentifiersTableModalPresenter> identifiersTableModalProvider;

  private final ModalProvider<ImportSystemIdentifiersModalPresenter> importSystemIdentifiersModalProvider;

  private TableDto selectedTable;

  private Runnable removeConfirmation;

  @Inject
  public IdentifiersAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy,
      IdentifiersTablePresenter identifiersTablePresenter,
      ModalProvider<IdentifiersTableModalPresenter> identifiersTableModalProvider,
      ModalProvider<ImportSystemIdentifiersModalPresenter> importSystemIdentifiersModalProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.identifiersTablePresenter = identifiersTablePresenter;
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.identifiersTableModalProvider = identifiersTableModalProvider;
    this.importSystemIdentifiersModalProvider = importSystemIdentifiersModalProvider;
    this.identifiersTableModalProvider.setContainer(this);
    this.importSystemIdentifiersModalProvider.setContainer(this);
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
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());
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
    selectedTable = identifiersTable;
    identifiersTablePresenter.showIdentifiersTable(identifiersTable);
  }

  @Override
  public void onAddIdentifiersTable() {
    identifiersTableModalProvider.get();
  }

  @Override
  public void onDeleteIdentifiersTable() {
    removeConfirmation = new RemoveRunnable();

    ConfirmationRequiredEvent event = ConfirmationRequiredEvent
        .createWithKeys(removeConfirmation, "removeIdentifiersTable", "confirmRemoveIdentifiersTable");

    fireEvent(event);
  }

  @Override
  public void onImportSystemIdentifiers() {
    if(selectedTable != null) {
      ImportSystemIdentifiersModalPresenter p = importSystemIdentifiersModalProvider.get();
      p.initialize(selectedTable);
    }
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

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }

  private class RemoveRunnable implements Runnable {
    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            refresh();
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      String uri = UriBuilders.IDENTIFIERS_TABLE.create().build(selectedTable.getName());
      ResourceRequestBuilderFactory.newBuilder().forResource(uri).delete().withCallback(SC_OK, callbackHandler)
          .withCallback(SC_FORBIDDEN, callbackHandler).withCallback(SC_INTERNAL_SERVER_ERROR, callbackHandler)
          .withCallback(SC_NOT_FOUND, callbackHandler).send();
    }

  }

  @ProxyStandard
  @NameToken(Places.IDENTIFIERS)
  public interface Proxy extends ProxyPlace<IdentifiersAdministrationPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<IdentifiersAdministrationUiHandlers> {

    void showIdentifiersTables(JsArray<TableDto> identifiersTables, String selectEntityType);

  }

}
