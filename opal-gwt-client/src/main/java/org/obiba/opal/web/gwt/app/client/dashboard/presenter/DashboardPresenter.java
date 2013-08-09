/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.dashboard.presenter;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ApplicationPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.HasPageTitle;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;

public class DashboardPresenter extends Presenter<DashboardPresenter.Display, DashboardPresenter.Proxy> implements
    HasPageTitle {

  private static final Translations translations = GWT.create(Translations.class);

  @ProxyStandard
  @NameToken(Places.dashboard)
  public interface Proxy extends ProxyPlace<DashboardPresenter> {}

  @Inject
  public DashboardPresenter(Display display, EventBus eventBus, Proxy proxy) {
    super(eventBus, display, proxy, ApplicationPresenter.WORKBENCH);
  }

  @Override
  public String getTitle() {
    return translations.pageDashboardTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();

    authorize();

    ResponseCodeCallback noOp = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/participants/count").get()
        .withCallback(Response.SC_OK, new ResponseCodeCallback() {

          @Override
          public void onResponseCode(Request request, Response response) {
            getView().setParticipantCount(Integer.parseInt(response.getText()));
          }
        })//
        .withCallback(Response.SC_FORBIDDEN, noOp)//
        .withCallback(Response.SC_METHOD_NOT_ALLOWED, noOp)//
        .send();

  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get()
        .authorize(getView().getDatasourcesAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").get()
        .authorize(getView().getUnitsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get()
        .authorize(getView().getReportsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/shell/commands").get()
        .authorize(getView().getJobsAuthorizer()).send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {
    void setParticipantCount(int count);

    //
    // Authorization
    //

    HasAuthorization getUnitsAuthorizer();

    HasAuthorization getDatasourcesAuthorizer();

    HasAuthorization getFilesAuthorizer();

    HasAuthorization getJobsAuthorizer();

    HasAuthorization getReportsAuthorizer();

  }

}
