/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.apps;

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
import org.obiba.opal.web.gwt.app.client.administration.apps.event.AppsTokenUpdateEvent;
import org.obiba.opal.web.gwt.app.client.administration.apps.event.RockAppConfigAddEvent;
import org.obiba.opal.web.gwt.app.client.administration.apps.event.RockAppConfigUpdateEvent;
import org.obiba.opal.web.gwt.app.client.administration.apps.rock.RockAppConfigModalPresenter;
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
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.AppDto;
import org.obiba.opal.web.model.client.opal.AppsConfigDto;
import org.obiba.opal.web.model.client.opal.RockAppConfigDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import static com.google.gwt.http.client.Response.*;

public class AppsAdministrationPresenter extends ItemAdministrationPresenter<AppsAdministrationPresenter.Display, AppsAdministrationPresenter.Proxy>
    implements AppsAdministrationUiHandlers {

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private Runnable confirmation;

  private AppsConfigDto appsConfig;

  private final ModalProvider<AppsTokenModalPresenter> appsTokenModalPresenterModalProvider;

  private final ModalProvider<RockAppConfigModalPresenter> rockAppConfigModalPresenterModalProvider;

  @Inject
  public AppsAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy,
                                     DefaultBreadcrumbsBuilder breadcrumbsHelper,
                                     ModalProvider<AppsTokenModalPresenter> appsTokenModalPresenterModalProvider,
                                     ModalProvider<RockAppConfigModalPresenter> rockAppConfigModalPresenterModalProvider) {
    super(eventBus, display, proxy);
    this.appsTokenModalPresenterModalProvider = appsTokenModalPresenterModalProvider.setContainer(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.rockAppConfigModalPresenterModalProvider = rockAppConfigModalPresenterModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageAppsTitle();
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (confirmation != null && event.getSource().equals(confirmation) && event.isConfirmed()) {
          confirmation.run();
          confirmation = null;
        }
      }
    });
    addRegisteredHandler(AppsTokenUpdateEvent.getType(), new AppsTokenUpdateEvent.AppsTokenUpdateHandler() {

      @Override
      public void onAppsTokenUpdate(AppsTokenUpdateEvent event) {
        onTokenUpdate(event.getToken());
      }
    });
    addRegisteredHandler(RockAppConfigAddEvent.getType(), new RockAppConfigHandler());
    addRegisteredHandler(RockAppConfigUpdateEvent.getType(), new RockAppConfigHandler());
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    onRefresh();
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {

  }

  @Override
  public void onRefresh() {
    ResourceRequestBuilderFactory.<JsArray<AppDto>>newBuilder().forResource(UriBuilders.APPS.create().build())
        .withCallback(new ResourceCallback<JsArray<AppDto>>() {
          @Override
          public void onResource(Response response, JsArray<AppDto> resource) {
            getView().renderApps(resource);
          }
        })
        .get().send();
    ResourceRequestBuilderFactory.<AppsConfigDto>newBuilder().forResource(UriBuilders.APPS_CONFIG.create().build())
        .withCallback(new ResourceCallback<AppsConfigDto>() {
          @Override
          public void onResource(Response response, AppsConfigDto resource) {
            setAppsConfig(resource);
            getView().renderAppsConfig(resource);
          }
        })
        .get().send();
  }

  @Override
  public void onUnregister(final AppDto app) {
    confirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.<RPackageDto>newBuilder()
            .forResource(UriBuilders.APP.create().build(app.getId()))
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                onRefresh();
              }
            }, SC_OK, SC_NO_CONTENT, SC_INTERNAL_SERVER_ERROR, SC_FORBIDDEN, SC_NOT_FOUND)
            .delete().send();
      }
    };
    String title = translations.removeApp();
    String message = translationMessages.confirmUnregisterApp(app.getName());
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onEditToken() {
    AppsTokenModalPresenter presenter = appsTokenModalPresenterModalProvider.create();
    presenter.setToken(appsConfig.getToken());
    appsTokenModalPresenterModalProvider.show();
  }

  public void onTokenUpdate(String token) {
    AppsConfigDto updatedAppsConfig = AppsConfigDto.parse(AppsConfigDto.stringify(appsConfig));
    updatedAppsConfig.setToken(token);
    doUpdate(updatedAppsConfig);
  }

  @Override
  public void onRockConfigRemove(final RockAppConfigDto rockConfig) {
    final AppsConfigDto updatedAppsConfig = AppsConfigDto.parse(AppsConfigDto.stringify(appsConfig));
    confirmation = new Runnable() {
      @Override
      public void run() {
        JsArray<RockAppConfigDto> configs = JsArrays.create();
        for (RockAppConfigDto config : JsArrays.toIterable(updatedAppsConfig.getRockConfigsArray())) {
          if (!config.getHost().equals(rockConfig.getHost())) {
            configs.push(config);
          }
        }
        updatedAppsConfig.setRockConfigsArray(configs);
        doUpdate(updatedAppsConfig);
      }
    };
    String title = translations.removeRockConfig();
    String message = translationMessages.confirmRockConfigRemoval(rockConfig.getHost());
    fireEvent(ConfirmationRequiredEvent.createWithMessages(confirmation, title, message));
  }

  @Override
  public void onRockConfigEdit(RockAppConfigDto rockConfig) {
    RockAppConfigModalPresenter presenter = rockAppConfigModalPresenterModalProvider.create();
    presenter.setConfig(rockConfig);
    rockAppConfigModalPresenterModalProvider.show();
  }

  @Override
  public void onRockConfigAdd() {
    RockAppConfigModalPresenter presenter = rockAppConfigModalPresenterModalProvider.create();
    rockAppConfigModalPresenterModalProvider.show();
  }

  private void doUpdate(AppsConfigDto config) {
    ResourceRequestBuilderFactory.<AppsConfigDto>newBuilder().forResource(UriBuilders.APPS_CONFIG.create().build())
        .withResourceBody(AppsConfigDto.stringify(config))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(ConfirmationTerminatedEvent.create());
            onRefresh();
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_SERVICE_UNAVAILABLE)
        .put().send();
  }

  private void setAppsConfig(AppsConfigDto appsConfig) {
    this.appsConfig = appsConfig;
  }

  @ProxyStandard
  @NameToken(Places.APPS)
  public interface Proxy extends ProxyPlace<AppsAdministrationPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<AppsAdministrationUiHandlers> {

    void renderApps(JsArray<AppDto> apps);

    void renderAppsConfig(AppsConfigDto config);
  }

  private class RockAppConfigHandler implements RockAppConfigAddEvent.RockAppConfigAddHandler, RockAppConfigUpdateEvent.RockAppConfigUpdateHandler {

    @Override
    public void onRockAppConfigAdd(RockAppConfigAddEvent event) {
      handleRockAppConfig(event.getConfig());
    }

    @Override
    public void onRockAppConfigUpdate(RockAppConfigUpdateEvent event) {
      handleRockAppConfig(event.getConfig());
    }

    private void handleRockAppConfig(RockAppConfigDto dto) {
      JsArray<RockAppConfigDto> configs = JsArrays.create();
      for (RockAppConfigDto config : JsArrays.toIterable(appsConfig.getRockConfigsArray()))
        if (!config.getHost().equals(dto.getHost())) {
          configs.push(config);
        }
      configs.push(dto);
      appsConfig.setRockConfigsArray(configs);
      doUpdate(appsConfig);
    }
  }

}
