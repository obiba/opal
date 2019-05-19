/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldMethodUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class DataShieldPackageCreatePresenter extends ModalPresenterWidget<DataShieldPackageCreatePresenter.Display>
    implements DataShieldPackageCreateUiHandlers {

  private PackageValidationHandler packageValidationHandler;


  @Inject
  public DataShieldPackageCreatePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    packageValidationHandler = new PackageValidationHandler(getEventBus());
  }

  /**
   * Setup the dialog for creating a method
   */
  public void addNewPackage() {
    getView().clear();
  }

  private String packageR(String name) {
    return UriBuilder.create().segment("datashield", "package", "{name}").build(name);
  }

  @Override
  public void installPackage() {
    if(packageValidationHandler.validate()) {
      getView().setInProgress(true);
      ResponseCodeCallback createCallback = new CreatePackageCallBack();
      ResourceCallback alreadyExistCallback = new AlreadyExistMethodCallBack();
      ResourceRequestBuilderFactory.<RPackageDto>newBuilder().forResource(packageR(getView().getName().getText()))
          .get()//
          .withCallback(alreadyExistCallback)//
          .withCallback(Response.SC_NOT_FOUND, createCallback).send();
    }
  }

  //
  // Inner classes and interfaces
  //

  private class PackageValidationHandler extends AbstractValidationHandler {

    PackageValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();
        validators.add(new RequiredTextValidator(getView().getName(), "DataShieldPackageNameIsRequired"));
      }
      return validators;
    }

  }

  private class AlreadyExistMethodCallBack implements ResourceCallback<RPackageDto> {

    @Override
    public void onResource(Response response, RPackageDto resource) {
      getView().setInProgress(false);
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("DataShieldPackageAlreadyExistWithTheSpecifiedName").build());
    }
  }

  private class CreatePackageCallBack implements ResponseCodeCallback {

    private HandlerRegistration unhandledExceptionHandler;

    @Override
    public void onResponseCode(Request request, Response response) {
      postMethod(getDataShieldPackageDto());
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

      if(getView().getReference().getText().isEmpty()) {
        ResourceRequestBuilderFactory.newBuilder().forResource(packagesR(getView().getName().getText())).post()//
            .withResourceBody(RPackageDto.stringify(dto))//
            .withCallback(Response.SC_OK, callbackHandler)//
            .withCallback(Response.SC_NOT_FOUND, callbackHandler)//
            .withCallback(Response.SC_CREATED, callbackHandler).send();
      } else {
        ResourceRequestBuilderFactory.newBuilder()
            .forResource(packagesR(getView().getName().getText(), getView().getReference().getText())).post()//
            .withResourceBody(RPackageDto.stringify(dto))//
            .withCallback(Response.SC_OK, callbackHandler)//
            .withCallback(Response.SC_CREATED, callbackHandler)//
            .withCallback(Response.SC_NOT_FOUND, callbackHandler).send();
      }
    }

    private String packagesR(String name) {
      return UriBuilder.create().segment("datashield", "packages").query("name", name).build(name);
    }

    private String packagesR(String name, String reference) {
      return UriBuilder.create().segment("datashield", "packages").query("name", name).query("ref", reference)
          .build(name, reference);
    }

    private RPackageDto getDataShieldPackageDto() {
      RPackageDto dto = RPackageDto.create();
      dto.setName(getView().getName().getText());

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
      if(response.getStatusCode() == Response.SC_CREATED) {
        getEventBus().fireEvent(new DataShieldPackageCreatedEvent(dto));
        getEventBus().fireEvent(new DataShieldMethodUpdatedEvent());
      } else if(response.getStatusCode() == Response.SC_NOT_FOUND) {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error("RPackageInstalledButNotFound").build());
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataShieldPackageCreateUiHandlers> {

    String DATASHIELD_ALL_PKG = "datashield";

    void hideDialog();

    void setName(String name);

    HasText getName();

    HasText getReference();

    void clear();

    void setInProgress(boolean progress);
  }

}
