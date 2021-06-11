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

import com.google.common.collect.Maps;
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
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileAddedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.packages.DataShieldPackagesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.DataShieldProfileModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.DataShieldProfilePresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    void clearProfiles();

  }

  private final ModalProvider<DataShieldProfileModalPresenter> dataShieldProfilePresenterModalProvider;

  private final Provider<DataShieldPackagesPresenter> packagesPresenterProvider;

  private final Provider<DataShieldProfilePresenter> profilePresenterProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  public static final Object PermissionSlot = new Object();

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private static final String DATASHIELD_NAME = "DataSHIELD";

  private List<RServerClusterDto> clusters;

  @Inject
  public DataShieldAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
                                           ModalProvider<DataShieldProfileModalPresenter> dataShieldProfilePresenterModalProvider, Provider<DataShieldPackagesPresenter> packagesPresenterProvider,
                                           Provider<DataShieldProfilePresenter> profilePresenterProvider,
                                           Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                           DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    getView().setUiHandlers(this);
    this.dataShieldProfilePresenterModalProvider = dataShieldProfilePresenterModalProvider.setContainer(this);
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
  protected void onBind() {
    super.onBind();
    addRegisteredHandler(DataShieldProfileDeletedEvent.getType(), new DataShieldProfileDeletedEvent.DataShieldProfileDeletedHandler() {
      @Override
      public void onDataShieldProfileDeleted(DataShieldProfileDeletedEvent event) {
        refreshProfiles();
      }
    });
    addRegisteredHandler(DataShieldProfileAddedEvent.getType(), new DataShieldProfileAddedEvent.DataShieldProfileAddedHandler() {
      @Override
      public void onDataShieldProfileAdded(DataShieldProfileAddedEvent event) {
        fireEvent(NotificationEvent.newBuilder()
            .info(translationMessages.dataShieldProfileAddedInfo(event.getProfile().getName(), event.getProfile().getCluster())).build());
        refreshProfiles(event.getProfile());
      }
    });
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
              clusters = JsArrays.toList(resource);
              for (RServerClusterDto cluster : clusters) {
                DataShieldPackagesPresenter packagesPresenter = packagesPresenterProvider.get();
                packagesPresenter.setCluster(cluster);
                addToSlot(new PackagesSlot(cluster), packagesPresenter);
              }
              refreshProfiles();
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
  public void onAddProfile() {
    DataShieldProfileModalPresenter presenter = dataShieldProfilePresenterModalProvider.get();
    presenter.initialize(clusters);

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

  private void refreshProfiles() {
    refreshProfiles(null);
  }

  private void refreshProfiles(final DataShieldProfileDto selectProfile) {
    getView().clearProfiles();
    ResourceRequestBuilderFactory.<JsArray<DataShieldProfileDto>>newBuilder().forResource(UriBuilders.DATASHIELD_PROFILES.create().build())
        .withCallback(new ResourceCallback<JsArray<DataShieldProfileDto>>() {
          @Override
          public void onResource(Response response, JsArray<DataShieldProfileDto> resource) {
            if (response.getStatusCode() == SC_OK) {
              Map<String, RServerClusterDto> clusterMap = Maps.newHashMap();
              for (RServerClusterDto cluster : clusters) {
                clusterMap.put(cluster.getName(), cluster);
              }
              List<DataShieldProfileDto> profiles = JsArrays.toList(resource);
              Collections.sort(profiles, new Comparator<DataShieldProfileDto>() {
                @Override
                public int compare(DataShieldProfileDto p1, DataShieldProfileDto p2) {
                  return toKey(p1).compareTo(toKey(p2));
                }

                // trick to have the primary clusters before their secondaries
                private String toKey(DataShieldProfileDto p) {
                  return p.getCluster() + "." + (p.getCluster().equals(p.getName()) ? "." : "") + p.getCluster();
                }

              });
              for (DataShieldProfileDto profile : profiles) {
                DataShieldProfilePresenter profilePesenter = profilePresenterProvider.get();
                profilePesenter.initialize(profile, clusterMap.get(profile.getCluster()));
                boolean selected = selectProfile != null && selectProfile.getName().equals(profile.getName());
                addToSlot(new ProfilesSlot(profile, clusterMap.get(profile.getCluster()), selected), profilePesenter);
              }
            }
          }
        })
        .get().send();
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

    private final boolean selected;

    public ProfilesSlot(DataShieldProfileDto profile, RServerClusterDto cluster, boolean selected) {
      this.profile = profile;
      this.cluster = cluster;
      this.selected = selected;
    }

    public DataShieldProfileDto getProfile() {
      return profile;
    }

    public boolean hasCluster() {
      return cluster != null;
    }

    public boolean isSelected() {
      return selected;
    }

    @Override
    public String toString() {
      return profile.getName();
    }
  }
}
