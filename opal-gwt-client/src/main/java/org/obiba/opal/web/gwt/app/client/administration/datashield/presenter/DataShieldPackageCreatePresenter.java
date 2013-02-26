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

  private Mode dialogMode;

  public enum Mode {
    CREATE, UPDATE
  }

  private MethodValidationHandler methodValidationHandler;

  @Inject
  public DataShieldPackageCreatePresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);

    registerHandler(getView().getInstallButton().addClickHandler(new CreateOrUpdateMethodClickHandler()));

    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    methodValidationHandler = new MethodValidationHandler(getEventBus());

  }

  private void setDialogMode(Mode dialogMode) {
    this.dialogMode = dialogMode;
    getView().setDialogMode(dialogMode);
  }

  /**
   * Setup the dialog for creating a method
   */
  public void addNewPackage() {
    setDialogMode(Mode.CREATE);
    getView().clear();
  }

  /**
   * Setup the dialog for updating an existing method
   *
   * @param dto method to update
   */
//  public void showPackage(RPackageDto dto) {
//    setDialogMode(Mode.UPDATE);
//    displayMethod(dto.getName(), dto);
//  }

  /**
   * Setup the dialog for copying an existing method
   *
   * @param dto method to copy
   */
//  public void copyMethod(RPackageDto dto) {
//    setDialogMode(Mode.CREATE);
//    displayMethod("copy_of_" + dto.getName(), dto);
//  }

//  private void displayMethod(String name, RPackageDto dto) {
//    getView().setName(name);
//  }
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

//  private void showPackage() {
//    if(methodValidationHandler.validate()) {
//      putMethod(getDataShieldPackageDto());
//    }
//  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void createPackage() {
    if(methodValidationHandler.validate()) {
      ResponseCodeCallback createCallback = new CreatePackageCallBack();
      ResourceCallback alreadyExistCallback = new AlreadyExistMethodCallBack();
      ResourceRequestBuilderFactory.<RPackageDto>newBuilder().forResource(packageR(getView().getName().getText()))
          .get()//
          .withCallback(alreadyExistCallback)//
          .withCallback(Response.SC_NOT_FOUND, createCallback).send();
    }
  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private void postMethod(RPackageDto dto) {
    ResponseCodeCallback callbackHandler = new CreateOrUpdatePackageCallBack(dto);

    if(!getView().getReference().getText().isEmpty()) {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(packagesR(getView().getName().getText(), getView().getReference().getText())).post()//
          .withResourceBody(RPackageDto.stringify(dto))//
          .withCallback(Response.SC_OK, callbackHandler)//
          .withCallback(Response.SC_CREATED, callbackHandler)//
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    } else {
      ResourceRequestBuilderFactory.newBuilder().forResource(packagesR(getView().getName().getText())).post()//
          .withResourceBody(RPackageDto.stringify(dto))//
          .withCallback(Response.SC_OK, callbackHandler)//
          .withCallback(Response.SC_CREATED, callbackHandler)//
          .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
    }
  }

//  private void putMethod(RPackageDto dto) {
//    CreateOrUpdatePackageCallBack callbackHandler = new CreateOrUpdatePackageCallBack(dto);
//    ResourceRequestBuilderFactory.newBuilder().forResource(name(getView().getName().getText())).put()//
//        .withResourceBody(DataShieldMethodDto.stringify(dto))//
//        .withCallback(Response.SC_OK, callbackHandler)//
//        .withCallback(Response.SC_CREATED, callbackHandler)//
//        .withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
//  }

  @SuppressWarnings("MethodOnlyUsedFromInnerClass")
  private RPackageDto getDataShieldPackageDto() {
    RPackageDto dto = RPackageDto.create();
    dto.setName(getView().getName().getText());

    return dto;
  }

  //
  // Inner classes and interfaces
  //

  private class MethodValidationHandler extends AbstractValidationHandler {

    MethodValidationHandler(EventBus eventBus) {
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

  public class CreateOrUpdateMethodClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      if(dialogMode == Mode.CREATE) {
        createPackage();
      }
//      else if(dialogMode == Mode.UPDATE) {
//        showPackage();
//      }
    }

  }

  private class CreateOrUpdatePackageCallBack implements ResponseCodeCallback {

    RPackageDto dto;

    public CreateOrUpdatePackageCallBack(RPackageDto dto) {
      this.dto = dto;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      getView().hideDialog();
      if(response.getStatusCode() == Response.SC_OK) {
        getEventBus().fireEvent(new DataShieldPackageCreatedEvent(dto));
//      } else if(response.getStatusCode() == Response.SC_CREATED) {
//        getEventBus().fireEvent(new DataShieldMethodCreatedEvent(dto));
      } else {
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
      }
    }
  }

  public interface Display extends PopupView {

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    HasClickHandlers getInstallButton();

    HasClickHandlers getCancelButton();

    void setName(String name);

    HasText getName();

    HasText getReference();

    void clear();

  }

}
