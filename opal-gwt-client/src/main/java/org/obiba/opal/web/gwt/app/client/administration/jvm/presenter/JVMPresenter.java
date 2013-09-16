/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.jvm.presenter;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.OpalEnv;
import org.obiba.opal.web.model.client.opal.OpalStatus;

import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class JVMPresenter extends ItemAdministrationPresenter<JVMPresenter.Display, JVMPresenter.Proxy> {

  private static final int DELAY_MILLIS = 2000;

  private Timer timer;

  @ProxyStandard
  @NameToken(Places.JVM)
  public interface Proxy extends ProxyPlace<JVMPresenter> {}

  public interface Display extends View, HasBreadcrumbs {

    void renderProperties(OpalEnv env);

    void renderStatus(OpalStatus status);

    void initCharts();
  }

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public JVMPresenter(Display display, EventBus eventBus, Proxy proxy, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    this.breadcrumbsHelper = breadcrumbsHelper;
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/system/env").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListEnvironmentAuthorization())).send();
  }

  @Override
  public String getName() {
    return "JVM";
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    getView().initCharts();

    timer = new Timer() {
      @Override
      public void run() {
        pollStatus();
      }
    };
    timer.scheduleRepeating(DELAY_MILLIS);
  }

  @Override
  protected void onHide() {
    super.onHide();
    timer.cancel();
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

      // Fetch all system environment properties
      ResourceRequestBuilderFactory.<OpalEnv>newBuilder()//
          .forResource("/system/env").withCallback(new ResourceCallback<OpalEnv>() {

        @Override
        public void onResource(Response response, OpalEnv resource) {
          getView().renderProperties(resource);
        }
      }).get().send();
    }

    @Override
    public void unauthorized() {
      timer.cancel();
    }
  }

  private void pollStatus() {
    // Fetch system status
    ResourceRequestBuilderFactory.<OpalStatus>newBuilder()//
        .forResource("/system/status").withCallback(new ResourceCallback<OpalStatus>() {

      @Override
      public void onResource(Response response, OpalStatus resource) {
        if(response.getStatusCode() == Response.SC_OK) {
          getView().renderStatus(resource);
        }
      }
    }).get().send();
  }
}
