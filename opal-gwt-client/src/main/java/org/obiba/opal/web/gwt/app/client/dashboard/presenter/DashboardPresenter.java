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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class DashboardPresenter extends Presenter<DashboardPresenter.Display, DashboardPresenter.Proxy>
    implements HasPageTitle {

  private static final Translations translations = GWT.create(Translations.class);

  @ProxyStandard
  @NameToken(Places.DASHBOARD)
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
    authorize();
    ResponseCodeCallback noOp = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
      }
    };
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/datasources").get()
        .authorize(getView().getDatasourcesAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/functional-units").get()
        .authorize(getView().getUnitsAuthorizer()).send();
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/report-templates").get()
        .authorize(getView().getReportsAuthorizer()).send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    //
    // Authorization
    //

    HasAuthorization getUnitsAuthorizer();

    HasAuthorization getDatasourcesAuthorizer();

    HasAuthorization getFilesAuthorizer();

    HasAuthorization getReportsAuthorizer();

  }

}
