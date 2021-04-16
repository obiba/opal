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

import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.r.event.RPackageInstalledEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;

import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;

public class RPackageInstallModalPresenter extends ModalPresenterWidget<RPackageInstallModalPresenter.Display> implements RPackageInstallModalUiHandlers {

  @Inject
  public RPackageInstallModalPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  @Override
  public void installPackage(String name) {
    doInstallPackage(name, null, "cran");
  }

  @Override
  public void installGithubPackage(final String name, String ref) {
    doInstallPackage(name, ref, "gh");
  }

  @Override
  public void installBiocPackage(String name) {
    doInstallPackage(name, null, "bioc");
  }

  private void doInstallPackage(final String name, String ref, String manager) {
    UriBuilder builder = UriBuilders.SERVICE_R_CLUSTER_PACKAGES.create().query("name", name);
    if (!Strings.isNullOrEmpty(ref)) {
      builder.query("ref", ref);
    }
    if (!Strings.isNullOrEmpty(manager)) {
      builder.query("manager", manager);
    }
    getView().setInProgress(true);
    ResourceRequestBuilderFactory.newBuilder() //
        .forResource(builder.build("default")) //
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hideDialog();
            fireEvent(new RPackageInstalledEvent(name));
          }
        }, SC_CREATED, SC_INTERNAL_SERVER_ERROR) //
        .post().send();
  }

  public interface Display extends PopupView, HasUiHandlers<RPackageInstallModalUiHandlers> {

    void hideDialog();

    void setInProgress(boolean progress);
  }

}
