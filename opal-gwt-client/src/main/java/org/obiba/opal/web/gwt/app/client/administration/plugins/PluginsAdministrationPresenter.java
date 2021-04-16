/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.plugins;

import com.google.gwt.core.client.GWT;
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
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileSelectionRequestEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.PluginDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;

import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class PluginsAdministrationPresenter extends ItemAdministrationPresenter<PluginsAdministrationPresenter.Display, PluginsAdministrationPresenter.Proxy>
    implements PluginsAdministrationUiHandlers {

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final ModalProvider<PluginServiceConfigurationModalPresenter> pluginServiceConfigurationModalPresenterModalProvider;

  @Inject
  public PluginsAdministrationPresenter(EventBus eventBus, Display display, Proxy proxy,
                                        DefaultBreadcrumbsBuilder breadcrumbsHelper,
                                        ModalProvider<PluginServiceConfigurationModalPresenter> pluginServiceConfigurationModalPresenterModalProvider) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.pluginServiceConfigurationModalPresenterModalProvider = pluginServiceConfigurationModalPresenterModalProvider.setContainer(this);
    getView().setUiHandlers(this);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pagePluginsTitle();
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(FileSelectionEvent.getType(), new FileSelectionEvent.Handler() {
      @Override
      public void onFileSelection(FileSelectionEvent event) {
        if (!PluginsAdministrationPresenter.this.equals(event.getSource())) return;
        installPluginArchive(event.getSelectedFile().getSelectionPath());
      }
    });
  }

  @Override
  protected void onReveal() {
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    getView().refresh();
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
            fireEvent(NotificationEvent.newBuilder().error("PluginUpdateSiteError").build());
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
            fireEvent(NotificationEvent.newBuilder().error("PluginUpdateSiteError").build());
          }
        }) //
        .get().send();
  }

  @Override
  public void onUninstall(final String name) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGIN.create().build(name))
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NO_CONTENT)
                  fireEvent(NotificationEvent.newBuilder().info("PluginRemoved").args(name).build());
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginRemovalFailed").build());
                getInstalledPlugins();
              }
            },
            Response.SC_OK, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND)
        .delete().send();
  }

  @Override
  public void onCancelUninstall(final String name) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGIN.create().build(name))
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NO_CONTENT)
                  fireEvent(NotificationEvent.newBuilder().info("PluginReinstated").args(name).build());
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginReinstateFailed").build());
                getInstalledPlugins();
              }
            },
            Response.SC_OK, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND)
        .put().send();
  }

  @Override
  public void onInstall(final String name, final String version) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGINS.create()
        .query("name", name)
        .query("version", version).build())
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK)
                  fireEvent(NotificationEvent.newBuilder().info("PluginInstalled").args(name, version).build());
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginInstallationFailed").build());
                getInstalledPlugins();
              }
            },
            Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND) //
        .post().send();
  }

  @Override
  public void onRestart(final String name) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGIN_SERVICE.create().build(name))
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NO_CONTENT)
                  onStart(name);
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginStopFailed").build());
              }
            },
            Response.SC_OK, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND)
        .delete().send();
  }

  private void onStart(final String name) {
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGIN_SERVICE.create().build(name))
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK || response.getStatusCode() == Response.SC_NO_CONTENT)
                  fireEvent(NotificationEvent.newBuilder().info("PluginRestarted").args(name).build());
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginStartFailed").build());
              }
            },
            Response.SC_OK, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND)
        .put().send();
  }

  @Override
  public void onConfigure(final String name) {
    ResourceRequestBuilderFactory.<PluginDto>newBuilder() //
        .forResource(UriBuilders.PLUGIN.create().build(name)) //
        .withCallback(new ResourceCallback<PluginDto>() {
          @Override
          public void onResource(Response response, PluginDto resource) {
            PluginServiceConfigurationModalPresenter p = pluginServiceConfigurationModalPresenterModalProvider.get();
            p.initialize(name, resource.getSiteProperties());
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

  private void installPluginArchive(final String file) {
    if (!file.endsWith("-dist.zip")) {
      GWT.log("file=" + file);
      fireEvent(NotificationEvent.newBuilder().error("NotPluginArchive").build());
      return;
    }
    ResourceRequestBuilderFactory.newBuilder().forResource(UriBuilders.PLUGINS.create()
        .query("file", file).build())
        .withCallback(
            new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK)
                  fireEvent(NotificationEvent.newBuilder().info("PluginPackageInstalled").args(file).build());
                else
                  fireEvent(NotificationEvent.newBuilder().error("PluginInstallationFailed").build());
                getInstalledPlugins();
              }
            },
            Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR,//
            Response.SC_NOT_FOUND) //
        .post().send();
  }

  @Override
  public void onPluginFileSelection() {
    fireEvent(new FileSelectionRequestEvent(this, FileSelectorPresenter.FileSelectionType.FILE));
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
