/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.idproviders.list;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.edit.IDProviderPresenter;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.event.IDProvidersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.IDProviderDto;

import java.util.List;

public class IDProvidersAdministrationPresenter extends
    ItemAdministrationPresenter<IDProvidersAdministrationPresenter.Display, IDProvidersAdministrationPresenter.Proxy>
    implements IDProvidersAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.ID_PROVIDERS)
  public interface Proxy extends ProxyPlace<IDProvidersAdministrationPresenter> {
  }

  private Runnable removeConfirmation;

  private final ModalProvider<IDProviderPresenter> modalProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public IDProvidersAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                            ModalProvider<IDProviderPresenter> modalProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.modalProvider = modalProvider.setContainer(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.ID_PROVIDERS.create().build()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListIDProvidersAuthorization())) //
        .get().send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void onAddProvider() {
    modalProvider.get().setDialogMode(IDProviderPresenter.Mode.CREATE);
  }

  @Override
  protected void onBind() {
    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
          removeConfirmation.run();
          removeConfirmation = null;
        }
      }
    }));

    registerHandler(getEventBus().addHandler(IDProvidersRefreshEvent.getType(), new IDProvidersRefreshEvent.IDProvidersRefreshHandler() {
      @Override
      public void onIDProvidersRefresh(IDProvidersRefreshEvent event) {
        refreshIDProviders();
      }
    }));

    getView().getActions().setActionHandler(new ActionHandler<IDProviderDto>() {
      @Override
      public void doAction(IDProviderDto object, String actionName) {
        if (ActionsColumn.REMOVE_ACTION.equals(actionName)) {
          removeConfirmation = new RemoveRunnable(object);
          String title = translations.removeIDProvider();
          String message = translationMessages.confirmRemoveIDProvider(object.getName());
          fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, title, message));
        } else if (ActionsColumn.EDIT_ACTION.equals(actionName)) {
          IDProviderPresenter presenter = modalProvider.get();
          presenter.setDialogMode(IDProviderPresenter.Mode.UPDATE);
          presenter.setIDProvider(object);
        } else if (Display.DISABLE_ACTION.equals(actionName)) {
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.ID_PROVIDER_ENABLE.create().build(object.getName())) //
              .withCallback(Response.SC_OK, new ResponseCodeCallback() {
                @Override
                public void onResponseCode(Request request, Response response) {
                  refreshIDProviders();
                }
              }).delete().send();
        } else if (Display.ENABLE_ACTION.equals(actionName)) {
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.ID_PROVIDER_ENABLE.create().build(object.getName())) //
              .withCallback(Response.SC_OK, new ResponseCodeCallback() {
                @Override
                public void onResponseCode(Request request, Response response) {
                  refreshIDProviders();
                }
              }).put().send();
        } else if (Display.DUPLICATE_ACTION.equals(actionName)) {
          IDProviderPresenter presenter = modalProvider.get();
          presenter.setDialogMode(IDProviderPresenter.Mode.CREATE);
          presenter.setIDProvider(object);
        }
      }
    });
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {

  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageIDProvidersTitle();
  }

  private void refreshIDProviders() {
    // Fetch all providers
    ResourceRequestBuilderFactory.<JsArray<IDProviderDto>>newBuilder() //
        .forResource(UriBuilders.ID_PROVIDERS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<IDProviderDto>>() {
          @Override
          public void onResource(Response response, JsArray<IDProviderDto> resource) {
            getView().renderIDProviders(JsArrays.toList(resource));
          }
        }) //
        .get().send();
  }

  private class RemoveRunnable implements Runnable {

    private final IDProviderDto provider;

    private RemoveRunnable(IDProviderDto provider) {
      this.provider = provider;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.ID_PROVIDER.create().build(provider.getName())) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              refreshIDProviders();
            }
          }).delete().send();
    }
  }

  private final class ListIDProvidersAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      refreshIDProviders();
    }

    @Override
    public void unauthorized() {
    }
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<IDProvidersAdministrationUiHandlers> {

    String ENABLE_ACTION = "Enable";

    String DISABLE_ACTION = "Disable";

    String DUPLICATE_ACTION = "Duplicate";

    void renderIDProviders(List<IDProviderDto> rows);

    HasActionHandler<IDProviderDto> getActions();

    void clear();
  }
}
