/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.profiles;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileResetEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldMethodsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.profiles.config.DataShieldROptionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;

import static com.google.gwt.http.client.Response.*;

public class DataShieldProfilePresenter
    extends PresenterWidget<DataShieldProfilePresenter.Display>
    implements DataShieldProfileUiHandlers {

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  public static final Object AggregateEnvironmentSlot = new Object();

  public static final Object AssignEnvironmentSlot = new Object();

  public static final Object OptionsSlot = new Object();

  public static Object PermissionsSlot = new Object();

  private final Provider<DataShieldMethodsPresenter> methodsPresenterProvider;

  private final Provider<DataShieldROptionsPresenter> optionsProvider;

  private DataShieldProfileDto profile;

  private ResourcePermissionsPresenter resourcePermissionsPresenter;

  @Inject
  public DataShieldProfilePresenter(Display display, EventBus eventBus,
                                    Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                    Provider<DataShieldMethodsPresenter> methodsPresenterProvider,
                                    Provider<DataShieldROptionsPresenter> optionsProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.methodsPresenterProvider = methodsPresenterProvider;
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.optionsProvider = optionsProvider;
  }

  @Override
  public void onProfileReset() {
    fireEvent(new DataShieldProfileResetEvent(profile));
  }

  public void setProfile(DataShieldProfileDto profile) {
    this.profile = profile;
    DataShieldMethodsPresenter assignPresenter = methodsPresenterProvider.get();
    assignPresenter.setEnvironment(DataShieldEnvironment.ASSIGN);
    assignPresenter.setProfile(profile);
    addToSlot(AssignEnvironmentSlot, assignPresenter);

    DataShieldMethodsPresenter aggregatePresenter = methodsPresenterProvider.get();
    aggregatePresenter.setEnvironment(DataShieldEnvironment.AGGREGATE);
    aggregatePresenter.setProfile(profile);
    addToSlot(AggregateEnvironmentSlot, aggregatePresenter);

    DataShieldROptionsPresenter optionsPresenter = optionsProvider.get();
    optionsPresenter.setProfile(profile);
    addToSlot(OptionsSlot, optionsPresenter);

    resourcePermissionsPresenter = resourcePermissionsProvider.get();
    resourcePermissionsPresenter.initialize(ResourcePermissionType.DATASHIELD_PROFILE,
        ResourcePermissionRequestPaths.UriBuilders.DATASHIELD_PROFILE_PERMISSIONS, profile.getName());
    addToSlot(PermissionsSlot, resourcePermissionsPresenter);

    getView().renderProfile(profile);
  }

  @Override
  public void onProfileEnable(final boolean enabled) {
    ResourceRequestBuilder<?> request = ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_PROFILE_ENABLE.create().build(profile.getName()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == SC_OK) {
              profile.setEnabled(enabled);
            }
            getView().renderProfile(profile);
          }
        }, SC_OK, SC_NOT_FOUND, SC_BAD_REQUEST, SC_BAD_GATEWAY, SC_INTERNAL_SERVER_ERROR);
    if (enabled)
      request.put().send();
    else
      request.delete().send();
  }

  @Override
  public void onProfileRestrictAccess(final boolean restricted) {
    ResourceRequestBuilder<?> request = ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.DATASHIELD_PROFILE_ACCESS.create().build(profile.getName()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == SC_OK) {
              profile.setRestrictedAccess(restricted);
            }
            getView().renderProfile(profile);
            resourcePermissionsPresenter.refreshPermissions();
          }
        }, SC_OK, SC_NOT_FOUND, SC_BAD_REQUEST, SC_BAD_GATEWAY, SC_INTERNAL_SERVER_ERROR);
    if (restricted)
      request.put().send();
    else
      request.delete().send();
  }

  @Override
  protected void onBind() {
  }

  public interface DataShieldEnvironment {

    String ASSIGN = "assign";

    String AGGREGATE = "aggregate";
  }

  public interface Display extends View, HasUiHandlers<DataShieldProfileUiHandlers> {

    void renderProfile(DataShieldProfileDto profile);
  }
}
