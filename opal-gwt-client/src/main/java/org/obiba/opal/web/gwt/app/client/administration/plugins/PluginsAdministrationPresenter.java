/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

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
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class PluginsAdministrationPresenter extends ItemAdministrationPresenter<PluginsAdministrationPresenter.Display, PluginsAdministrationPresenter.Proxy>
    implements PluginsAdministrationUiHandlers {

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private Runnable actionRequiringConfirmation;

  @Inject
  public PluginsAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pagePluginsTitle();
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    getView().refresh();
  }

  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/plugins").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new HasAuthorization() {
          @Override
          public void beforeAuthorization() {

          }

          @Override
          public void authorized() {

          }

          @Override
          public void unauthorized() {

          }
        })).send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {

  }

  @Override
  public void getInstalledPlugins() {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder() //
        .forResource(UriBuilders.PLUGINS.create().build()) //
        .withCallback(new ResourceCallback<PluginPackagesDto>() {
          @Override
          public void onResource(Response response, PluginPackagesDto resource) {
            getView().showInstalledPackages(resource);
          }
        }) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().error("PluginsServiceError").build());
          }
        }) //
        .get().send();
  }

  @Override
  public void getAvailablePlugins() {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder() //
        .forResource(UriBuilders.PLUGINS_AVAILABLE.create().build()) //
        .withCallback(new ResourceCallback<PluginPackagesDto>() {
          @Override
          public void onResource(Response response, PluginPackagesDto resource) {
            getView().showAvailablePackages(resource);
          }
        }) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().error("PluginsServiceError").build());
          }
        }) //
        .get().send();
  }

  @Override
  public void getUpdatablePlugins() {
    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder() //
        .forResource(UriBuilders.PLUGINS_UPDATES.create().build()) //
        .withCallback(new ResourceCallback<PluginPackagesDto>() {
          @Override
          public void onResource(Response response, PluginPackagesDto resource) {
            getView().showUpdatablePackages(resource);
          }
        }) //
        .withCallback(SC_INTERNAL_SERVER_ERROR, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().error("PluginsServiceError").build());
          }
        }) //
        .get().send();
  }

  @Override
  public void onUninstall(String name) {

  }

  @Override
  public void onCancelUninstall(String name) {

  }

  @Override
  public void onInstall(String name, String version) {

  }

  @Override
  public void onInstall(String file) {

  }

  @ProxyStandard
  @NameToken(Places.PLUGINS)
  public interface Proxy extends ProxyPlace<PluginsAdministrationPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<PluginsAdministrationUiHandlers> {

    void showInstalledPackages(PluginPackagesDto pluginPackagesDto);

    void showAvailablePackages(PluginPackagesDto pluginPackagesDto);

    void showUpdatablePackages(PluginPackagesDto pluginPackagesDto);

    void refresh();
  }

}
