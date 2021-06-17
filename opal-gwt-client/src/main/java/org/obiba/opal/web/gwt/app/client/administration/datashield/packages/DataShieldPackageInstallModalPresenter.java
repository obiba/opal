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

import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;
import org.obiba.opal.web.model.client.opal.r.RServerClusterDto;

public class DataShieldPackageInstallModalPresenter extends ModalPresenterWidget<DataShieldPackageInstallModalPresenter.Display>
    implements DataShieldPackageInstallModalUiHandlers {

  private RServerClusterDto cluster;

  @Inject
  public DataShieldPackageInstallModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  public void setCluster(RServerClusterDto cluster) {
    this.cluster = cluster;
  }

  /**
   * Setup the dialog for creating a method
   */
  public void addNewPackage() {
    getView().clear();
  }

  private String packageR(String name) {
    return UriBuilder.create().segment("datashield", "package", "{name}").query("profile", cluster.getName()).build(name);
  }

  @Override
  public void installCRANPackage(String name) {
    doInstallPackage(name, "");
  }

  @Override
  public void installGithubPackage(String name, String ref) {
    doInstallPackage(name, Strings.isNullOrEmpty(ref.trim()) ? "master" : ref.trim());
  }

  //
  // Inner classes and interfaces
  //

  private void doInstallPackage(String name, String ref) {
    getView().setInProgress(true);
    ResponseCodeCallback createCallback = new CreatePackageCallBack(name, ref);
    ResourceRequestBuilderFactory.<RPackageDto>newBuilder()
        .forResource(packageR(name))
        .get()
        .withCallback(new AlreadyExistPackageCallBack())
        .withCallback(Response.SC_NOT_FOUND, createCallback).send();
  }

  private class AlreadyExistPackageCallBack implements ResourceCallback<RPackageDto> {

    @Override
    public void onResource(Response response, RPackageDto resource) {
      getView().setInProgress(false);
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("DataShieldPackageAlreadyExistWithTheSpecifiedName").build());
    }
  }

  private class CreatePackageCallBack implements ResponseCodeCallback {

    private HandlerRegistration unhandledExceptionHandler;

    private final String name;

    private final String ref;

    private CreatePackageCallBack(String name, String ref) {
      this.name = name;
      this.ref = ref;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      postMethod(getDataShieldPackageDto(name));
    }

    private void postMethod(RPackageDto dto) {

      unhandledExceptionHandler = addHandler(UnhandledResponseEvent.getType(),
          new UnhandledResponseEvent.Handler() {
            @Override
            public void onUnhandledResponse(UnhandledResponseEvent e) {
              // since an invalid or not found package is not flagged, silently close the dialog
              e.setConsumed(true);
              unhandledExceptionHandler.removeHandler();
              getView().hideDialog();
            }
          });

      ResponseCodeCallback callbackHandler = new CreateOrUpdatePackageCallBack(dto);
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(packagesR(name, ref)).post()
          .withResourceBody(RPackageDto.stringify(dto))
          .withCallback(Response.SC_OK, callbackHandler)
          .withCallback(Response.SC_CREATED, callbackHandler)
          .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
    }

    private String packagesR(String name, String reference) {
      UriBuilder builder = UriBuilder.create().segment("datashield", "packages")
          .query("name", name)
          .query("profile", cluster.getName());
      if (Strings.isNullOrEmpty(reference))
        return builder.query("manager", "cran").build();
      else
        return builder
            .query("ref", reference)
            .query("manager", "github")
            .build();
    }

    private RPackageDto getDataShieldPackageDto(String name) {
      RPackageDto dto = RPackageDto.create();
      dto.setName(name);
      return dto;
    }
  }

  private class CreateOrUpdatePackageCallBack implements ResponseCodeCallback {

    RPackageDto dto;

    private CreateOrUpdatePackageCallBack(RPackageDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if (response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new DataShieldPackageCreatedEvent(cluster.getName(), dto));
        getEventBus().fireEvent(new DataShieldMethodUpdatedEvent(cluster.getName()));
      } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("RPackageInstalledButNotFound").build());
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataShieldPackageInstallModalUiHandlers> {

    String DATASHIELD_ALL_PKG = "datashield";

    void hideDialog();

    void clear();

    void setInProgress(boolean progress);
  }

}
