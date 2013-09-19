/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.server.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.GeneralConf;

import com.google.gwt.core.client.JsArrayString;
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

public class ServerPresenter extends ItemAdministrationPresenter<ServerPresenter.Display, ServerPresenter.Proxy>
    implements ServerUiHandlers {

  @ProxyStandard
  @NameToken(Places.SERVER)
  public interface Proxy extends ProxyPlace<ServerPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<ServerUiHandlers> {

    void renderProperties(GeneralConf resource);

    String getName();

    String getDefaultCharSet();

    JsArrayString getLanguages();
  }

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public ServerPresenter(Display display, EventBus eventBus, Proxy proxy, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/conf/general").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ServerAuthorization())).send();
  }

  @Override
  public String getName() {
    return "Configuration";
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    initGeneralConfig();
  }

  @Override
  protected void onHide() {
    super.onHide();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/conf/general").post()
        .authorize(authorizer).send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageServerConfigurationTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  @Override
  public void save() {
    GeneralConf dto = GeneralConf.create();
    dto.setName(getView().getName());
    dto.setDefaultCharSet(getView().getDefaultCharSet());
    dto.setLanguagesArray(getView().getLanguages());

    ResourceRequestBuilderFactory.<GeneralConf>newBuilder().forResource("/system/conf/general")
        .withResourceBody(GeneralConf.stringify(dto)).withCallback(new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        if(response.getStatusCode() == Response.SC_OK) {
          initGeneralConfig();
          getEventBus().fireEvent(NotificationEvent.Builder.newNotification().info("GeneralConfigSaved").build());
        } else {
          getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
        }
      }
    }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_NOT_FOUND).put().send();
  }

  @Override
  public void cancel() {
    initGeneralConfig();
  }

  private void initGeneralConfig() {
    ResourceRequestBuilderFactory.<GeneralConf>newBuilder()//
        .forResource("/system/conf/general").withCallback(new ResourceCallback<GeneralConf>() {

      @Override
      public void onResource(Response response, GeneralConf resource) {
        getView().renderProperties(resource);
      }
    }).get().send();
  }

  private final class ServerAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      ResourceRequestBuilderFactory.<GeneralConf>newBuilder()//
          .forResource("/system/conf/general").withCallback(new ResourceCallback<GeneralConf>() {

        @Override
        public void onResource(Response response, GeneralConf resource) {
          getView().renderProperties(resource);
        }
      }).get().send();
    }

    @Override
    public void unauthorized() {
    }
  }

}
