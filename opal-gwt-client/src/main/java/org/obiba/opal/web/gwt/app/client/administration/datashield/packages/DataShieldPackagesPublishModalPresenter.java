/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.packages;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldProfileUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.datashield.DataShieldPackageMethodsDto;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import java.util.List;

import static com.google.gwt.http.client.Response.*;

public class DataShieldPackagesPublishModalPresenter extends ModalPresenterWidget<DataShieldPackagesPublishModalPresenter.Display>
    implements DataShieldPackagesPublishModalUiHandlers {

  private List<RPackageDto> packages;

  private DataShieldProfileDto profile;

  @Inject
  public DataShieldPackagesPublishModalPresenter(EventBus eventBus, Display view) {
    super(eventBus, view);
    getView().setUiHandlers(this);
  }

  public void initialize(List<RPackageDto> packages, DataShieldProfileDto profile) {
    this.packages = packages;
    this.profile = profile;
    List<String> names = Lists.newArrayList();
    for (RPackageDto pkg : packages) {
      if (!names.contains(pkg.getName()))
        names.add(pkg.getName());
    }
    getView().renderPackages(names, profile);
  }

  @Override
  public void publishPackages(List<String> packages) {
    getView().setInProgress(true);
    UriBuilder builder = UriBuilders.DATASHIELD_PACKAGES_PUBLISH.create();
    for (String name : packages)
      builder.query("name", name);
    builder.query("profile", profile.getName());

    ResourceRequestBuilderFactory.<DataShieldPackageMethodsDto>newBuilder()
        .forResource(builder.build())
        .put()
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(NotificationEvent.newBuilder().info("DataShieldProfileInit").args(profile.getName()).build());
            fireEvent(new DataShieldProfileUpdatedEvent(profile));
            getView().hideDialog();
          }
        }, SC_OK, SC_NOT_FOUND, SC_BAD_REQUEST, SC_BAD_GATEWAY, SC_INTERNAL_SERVER_ERROR).send();
  }

  //
  // Inner classes and interfaces
  //

  public interface Display extends PopupView, HasUiHandlers<DataShieldPackagesPublishModalUiHandlers> {

    void renderPackages(List<String> packages, DataShieldProfileDto profile);

    void hideDialog();

    void setInProgress(boolean progress);

  }
}
