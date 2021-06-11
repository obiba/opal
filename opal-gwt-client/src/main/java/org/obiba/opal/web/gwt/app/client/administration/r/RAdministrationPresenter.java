/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.r;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.r.profiles.RClusterPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RSessionsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RWorkspacesPresenter;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import static com.google.gwt.http.client.Response.SC_OK;

public class RAdministrationPresenter
    extends ItemAdministrationPresenter<RAdministrationPresenter.Display, RAdministrationPresenter.Proxy>
    implements RAdministrationUiHandlers {

  private final RSessionsPresenter rSessionsPresenter;

  private final RWorkspacesPresenter rWorkspacesPresenter;

  private final Provider<RClusterPresenter> rClusterPresenterProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public RAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                  RSessionsPresenter rSessionsPresenter, RWorkspacesPresenter rWorkspacesPresenter,
                                  Provider<RClusterPresenter> rClusterPresenterProvider, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                  DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.rSessionsPresenter = rSessionsPresenter;
    this.rWorkspacesPresenter = rWorkspacesPresenter;
    this.rClusterPresenterProvider = rClusterPresenterProvider;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.breadcrumbsHelper = breadcrumbsHelper;
    getView().setUiHandlers(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    authorize(event.getHasAuthorization());
  }

  @Override
  public String getName() {
    return "R";
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageRConfigTitle();
  }

  @Override
  protected void onBind() {
    setInSlot(Display.Slots.RSessions, rSessionsPresenter);
    setInSlot(Display.Slots.RWorkspaces, rWorkspacesPresenter);
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_R.create().build()).post()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    getView().clearClusters();
    ResourceRequestBuilderFactory.<JsArray<RServerClusterDto>>newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTERS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<RServerClusterDto>>() {
          @Override
          public void onResource(Response response, JsArray<RServerClusterDto> resource) {
            if (response.getStatusCode() == SC_OK) {
              for (RServerClusterDto cluster : JsArrays.toIterable(resource)) {
                RClusterPresenter presenter = rClusterPresenterProvider.get();
                presenter.setCluster(cluster);
                setInSlot(cluster.getName(), presenter);
              }
            }
          }
        })
        .get().send();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test r on default cluster
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.R_SESSIONS.create().build())
        .post().authorize(authorizer)
        .send();
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(Display.Slots.Permissions);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter
          .initialize(ResourcePermissionType.R, ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_R);

      setInSlot(Display.Slots.Permissions, resourcePermissionsPresenter);
    }
  }

  @ProxyStandard
  @NameToken(Places.R)
  public interface Proxy extends ProxyPlace<RAdministrationPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<RAdministrationUiHandlers> {

    enum Slots {
      RSessions,
      RWorkspaces,
      Permissions
    }

    void clearClusters();

    HasAuthorization getPermissionsAuthorizer();

  }

}
