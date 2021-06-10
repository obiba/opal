/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield;

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
import org.obiba.opal.web.gwt.app.client.administration.datashield.packages.DataShieldPackagesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.DataShieldProfilePresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
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
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.List;

import static com.google.gwt.http.client.Response.SC_OK;

public class DataShieldAdministrationPresenter
    extends ItemAdministrationPresenter<DataShieldAdministrationPresenter.Display, DataShieldAdministrationPresenter.Proxy>
    implements DataShieldAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.DATASHIELD)
  public interface Proxy extends ProxyPlace<DataShieldAdministrationPresenter> {
  }

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<DataShieldAdministrationUiHandlers> {

    HasAuthorization getPermissionsAuthorizer();

    void clearClusters();

  }

  private final Provider<DataShieldPackagesPresenter> packagesPresenterProvider;

  private final Provider<DataShieldProfilePresenter> profilePresenterProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  public static final Object PermissionSlot = new Object();

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private static final String DATASHIELD_NAME = "DataSHIELD";

  @Inject
  public DataShieldAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                           Provider<DataShieldPackagesPresenter> packagesPresenterProvider,
                                           Provider<DataShieldProfilePresenter> profilePresenterProvider,
                                           Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                           DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    getView().setUiHandlers(this);
    this.packagesPresenterProvider = packagesPresenterProvider;
    this.profilePresenterProvider = profilePresenterProvider;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    authorize(event.getHasAuthorization());
  }

  @Override
  public String getName() {
    return DATASHIELD_NAME;
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();

    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_DATASHIELD.create().build()).post()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();

    getView().clearClusters();
    ResourceRequestBuilderFactory.<JsArray<RServerClusterDto>>newBuilder().forResource(UriBuilders.SERVICE_R_CLUSTERS.create().build())
        .withCallback(new ResourceCallback<JsArray<RServerClusterDto>>() {
          @Override
          public void onResource(Response response, JsArray<RServerClusterDto> resource) {
            if (response.getStatusCode() == SC_OK) {
              final List<RServerClusterDto> clusters = JsArrays.toList(resource);
              for (RServerClusterDto cluster : clusters) {
                DataShieldPackagesPresenter packagesPresenter = packagesPresenterProvider.get();
                packagesPresenter.setCluster(cluster);
                addToSlot(new PackagesSlot(cluster), packagesPresenter);
              }
              ResourceRequestBuilderFactory.<JsArray<DataShieldProfileDto>>newBuilder().forResource(UriBuilders.DATASHIELD_PROFILES.create().build())
                  .withCallback(new ResourceCallback<JsArray<DataShieldProfileDto>>() {
                    @Override
                    public void onResource(Response response, JsArray<DataShieldProfileDto> resource) {
                      if (response.getStatusCode() == SC_OK) {
                        for (DataShieldProfileDto profile : JsArrays.toIterable(resource)) {
                          DataShieldProfilePresenter profilePesenter = profilePresenterProvider.get();
                          RServerClusterDto profileCluster = null;
                          for (RServerClusterDto cluster : clusters) {
                            if (cluster.getName().equals(profile.getCluster())) {
                              profileCluster = cluster;
                              break;
                            }
                          }
                          profilePesenter.initialize(profile, profileCluster);
                          addToSlot(new ProfilesSlot(profile, profileCluster), profilePesenter);
                        }
                      }
                    }
                  })
                  .get().send();
            }
          }
        })
        .get().send();
  }

  @Override
  public void onDownloadLogs() {
    fireEvent(new FileDownloadRequestEvent("/system/log/datashield.log"));
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // test r on default cluster
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource(UriBuilders.DATASHIELD_PROFILES.create().build())
        .post().authorize(authorizer)
        .send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageDataShieldTitle();
  }

  private final class PermissionsUpdate implements HasAuthorization {
    @Override
    public void unauthorized() {
      clearSlot(PermissionSlot);
    }

    @Override
    public void beforeAuthorization() {

    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.DATASHIELD, ResourcePermissionRequestPaths.UriBuilders.SYSTEM_PERMISSIONS_DATASHIELD);
      setInSlot(PermissionSlot, resourcePermissionsPresenter);
    }
  }

  public class PackagesSlot {
    private final RServerClusterDto cluster;

    public PackagesSlot(RServerClusterDto cluster) {
      this.cluster = cluster;
    }

    @Override
    public String toString() {
      return cluster.getName();
    }
  }

  public class ProfilesSlot {

    private final DataShieldProfileDto profile;

    private final RServerClusterDto cluster;

    public ProfilesSlot(DataShieldProfileDto profile, RServerClusterDto cluster) {
      this.profile = profile;
      this.cluster = cluster;
    }

    public DataShieldProfileDto getProfile() {
      return profile;
    }

    public boolean hasCluster() {
      return cluster != null;
    }

    @Override
    public String toString() {
      return profile.getName();
    }
  }
}
