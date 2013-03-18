/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldPackageCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class DataShieldPackageCreatePresenter extends PresenterWidget<DataShieldPackageCreatePresenter.Display> {

  private PackageValidationHandler packageValidationHandler;

  @Inject
  public DataShieldPackageCreatePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    registerHandler(getView().getInstallButton().addClickHandler(new CreatePackageClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

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

  private String packagesR(String name) {
    return UriBuilder.create().segment("datashield", "packages").query("name", name).build(name);
  }

  private String packagesR(String name, String reference) {
    return UriBuilder.create().segment("datashield", "packages").query("name", name).query("ref", reference)
        .build(name, reference);
  }

  private void createPackage() {
    if(packageValidationHandler.validate()) {
      getView().setInstallButtonEnabled(false);
      getView().setCancelButtonEnabled(false);
      ResponseCodeCallback createCallback = new CreatePackageCallBack();
      ResourceCallback alreadyExistCallback = new AlreadyExistMethodCallBack();
      ResourceRequestBuilderFactory.<RPackageDto>newBuilder().forResource(packageR(getView().getName().getText()))
          .get()//
          .withCallback(alreadyExistCallback)//
          .withCallback(Response.SC_NOT_FOUND, createCallback).send();
    }
  }

  private void postMethod(RPackageDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdatePackageCallBack(dto);

    if(getView().getReference().getText().isEmpty()) {
      ResourceRequestBuilderFactory.newBuilder().forResource(packagesR(getView().getName().getText())).post()//
          .withResourceBody(RPackageDto.stringify(dto))//
          .withCallback(Response.SC_OK, callbackHandler)//
          .withCallback(Response.SC_CREATED, callbackHandler)//
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    } else {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(packagesR(getView().getName().getText(), getView().getReference().getText())).post()//
          .withResourceBody(RPackageDto.stringify(dto))//
          .withCallback(Response.SC_OK, callbackHandler)//
          .withCallback(Response.SC_CREATED, callbackHandler)//
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

  private RPackageDto getDataShieldPackageDto() {
    RPackageDto dto = RPackageDto.create();
    dto.setName(getView().getName().getText());

    return dto;
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
      getView().setInstallButtonEnabled(true);
      getView().setCancelButtonEnabled(true);
      getEventBus()
          .fireEvent(NotificationEvent.newBuilder().error("DataShieldPackageAlreadyExistWithTheSpecifiedName").build());
    }
  }

  private class CreatePackageCallBack implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      postMethod(getDataShieldPackageDto());
    }
  }

  public class CreatePackageClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      createPackage();
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
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getInstallButton();

    HasClickHandlers getCancelButton();

    void setName(String name);

    HasText getName();

    HasText getReference();

    void clear();

    void setInstallButtonEnabled(boolean b);

    void setCancelButtonEnabled(boolean b);

  }

}
