/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.java.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.user.presenter.UserPresenter;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class JVMPresenter extends ItemAdministrationPresenter<JVMPresenter.Display, JVMPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.JVM)
  public interface Proxy extends ProxyPlace<JVMPresenter> {}

  private final ModalProvider<UserPresenter> userModalProvider;

  private Runnable removeConfirmation;

  public interface Display extends View, HasBreadcrumbs {}

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public JVMPresenter(Display display, EventBus eventBus, Proxy proxy, ModalProvider<UserPresenter> userModalProvider,
      DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.userModalProvider = userModalProvider.setContainer(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/env").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListEnvironmentAuthorization())).send();
  }

  @Override
  public String getName() {
    return translations.indicesLabel();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
//    getView().getUsersTable().setVisibleRange(0, 10);
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/env").post().authorize(authorizer)
        .send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageJVMTitle();
  }

  @Override
  protected void onBind() {
    super.onBind();
  }

  private final class ListEnvironmentAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {

      // Fetch all users
      ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
          .forResource("/system/env").withCallback(new ResourceCallback<JsArray<UserDto>>() {

        @Override
        public void onResource(Response response, JsArray<UserDto> resource) {
//          getView().renderUserRows(resource);
        }
      }).get().send();
    }

    @Override
    public void unauthorized() {
    }
  }

}
