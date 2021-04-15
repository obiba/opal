/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import com.gwtplatform.mvp.client.HasUiHandlers;
import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.permissions.support.AclRequest;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class DataShieldConfigPresenter
    extends ItemAdministrationPresenter<DataShieldConfigPresenter.Display, DataShieldConfigPresenter.Proxy>
    implements DataShieldConfigUiHandlers {

  @ProxyStandard
  @NameToken(Places.DATASHIELD)
  public interface Proxy extends ProxyPlace<DataShieldConfigPresenter> {}

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<DataShieldConfigUiHandlers> {

    HasAuthorization getPermissionsAuthorizer();

  }

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  public static final Object PackageSlot = new Object();

  public static final Object AggregateEnvironmentSlot = new Object();

  public static final Object AssignEnvironmentSlot = new Object();

  public static final Object PermissionSlot = new Object();

  public static final Object OptionsSlot = new Object();

  private final DataShieldPackageAdministrationPresenter packagePresenter;

  private final DataShieldAdministrationPresenter aggregatePresenter;

  private final DataShieldAdministrationPresenter assignPresenter;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  private final DataShieldROptionsPresenter dataShieldROptionsPresenter;

  private static final String DATASHIELD_NAME = "DataSHIELD";

  @Inject
  public DataShieldConfigPresenter(Display display, EventBus eventBus, Proxy proxy,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      Provider<DataShieldAdministrationPresenter> adminPresenterProvider,
      Provider<DataShieldROptionsPresenter> dataShieldROptionsProvider,
      DataShieldPackageAdministrationPresenter packagePresenter, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    getView().setUiHandlers(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.packagePresenter = packagePresenter;
    dataShieldROptionsPresenter = dataShieldROptionsProvider.get();
    aggregatePresenter = adminPresenterProvider.get();
    assignPresenter = adminPresenterProvider.get();
    aggregatePresenter.setEnvironment(DataShieldEnvironment.AGGREGATE);
    assignPresenter.setEnvironment(DataShieldEnvironment.ASSIGN);
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    aggregatePresenter.authorize(event.getHasAuthorization());
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
    AclRequest.newResourceAuthorizationRequestBuilder()
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())).send();
  }

  @Override
  public void onDownloadLogs() {
    fireEvent(new FileDownloadRequestEvent("/system/log/datashield.log"));
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    // TODO: Need to test both environments
    aggregatePresenter.authorize(authorizer);
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageDataShieldTitle();
  }

  @Override
  protected void onBind() {
    addToSlot(PackageSlot, packagePresenter);
    setInSlot(OptionsSlot, dataShieldROptionsPresenter);
    addToSlot(AggregateEnvironmentSlot, aggregatePresenter);
    addToSlot(AssignEnvironmentSlot, assignPresenter);
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

  public interface DataShieldEnvironment {

    String ASSIGN = "assign";

    String AGGREGATE = "aggregate";
  }
}
