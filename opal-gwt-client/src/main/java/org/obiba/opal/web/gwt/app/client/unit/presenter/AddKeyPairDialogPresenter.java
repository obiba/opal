/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.unit.presenter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyPairForm;
import org.obiba.opal.web.model.client.opal.PrivateKeyForm;
import org.obiba.opal.web.model.client.opal.PublicKeyForm;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class AddKeyPairDialogPresenter extends WidgetPresenter<AddKeyPairDialogPresenter.Display> {

  private FunctionalUnitDto functionalUnit;

  public interface Display extends WidgetDisplay {
    void showDialog();

    void hideDialog();

    HasClickHandlers getAddButton();

    HasClickHandlers getCancelButton();

    HasCloseHandlers<DialogBox> getDialog();

    HasText getAlias();

    HasValue<Boolean> isPrivateKeyCreate();

    HasValue<Boolean> isPrivateKeyImport();

    HasText getAlgorithm();

    HasText getKeySize();

    HasText getPrivateKeyImport();

    HasValue<Boolean> isPublicKeyCreate();

    HasValue<Boolean> isPublicKeyImport();

    HasText getPublicKeyImport();

    HasText getFirstAndLastName();

    HasText getOrganizationalUnit();

    HasText getOrganizationName();

    HasText getCity();

    HasText getState();

    HasText getCountry();

    void clear();

    HandlerRegistration addFinishClickHandler(ClickHandler handler);

    HandlerRegistration addCancelClickHandler(ClickHandler handler);

    void setPrivateKeyValidationHandler(ValidationHandler handler);

    void setPublicKeyValidationHandler(ValidationHandler handler);

  }

  public interface ValidationHandler {

    public boolean validate();

  }

  @Inject
  public AddKeyPairDialogPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    addValidators();
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    getDisplay().showDialog();
  }

  private void addValidators() {
    getDisplay().setPrivateKeyValidationHandler(new PrivateKeyValidationHandler());
    getDisplay().setPublicKeyValidationHandler(new PublicKeyValidationHandler());
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        createKeyPair();
      }

    }));

    super.registerHandler(getDisplay().addCancelClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getDisplay().hideDialog();
      }
    }));

    super.registerHandler(getDisplay().getDialog().addCloseHandler(new CloseHandler<DialogBox>() {
      @Override
      public void onClose(CloseEvent<DialogBox> event) {
        unbind();
      }
    }));

  }

  public void setFunctionalUnit(FunctionalUnitDto functionalUnit) {
    this.functionalUnit = functionalUnit;
  }

  private void createKeyPair() {
    KeyPairForm dto = getKeyPairForm();
    CreateKeyPairCallBack callbackHandler = new CreateKeyPairCallBack(dto);
    ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/keys").post().withResourceBody(KeyPairForm.stringify(dto)).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
  }

  private KeyPairForm getKeyPairForm() {
    KeyPairForm dto = KeyPairForm.create();
    dto.setAlias(getDisplay().getAlias().getText());

    if(getDisplay().isPrivateKeyCreate().getValue()) {
      PrivateKeyForm pkForm = PrivateKeyForm.create();
      pkForm.setAlgo(getDisplay().getAlgorithm().getText());
      pkForm.setSize(Integer.parseInt(getDisplay().getKeySize().getText()));
      dto.setPrivateForm(pkForm);
    } else {
      dto.setPrivateImport(getDisplay().getPrivateKeyImport().getText());
    }

    if(getDisplay().isPublicKeyCreate().getValue()) {
      PublicKeyForm pkForm = PublicKeyForm.create();
      pkForm.setName(getDisplay().getFirstAndLastName().getText());
      pkForm.setOrganizationalUnit(getDisplay().getOrganizationalUnit().getText());
      pkForm.setOrganization(getDisplay().getOrganizationName().getText());
      pkForm.setLocality(getDisplay().getCity().getText());
      pkForm.setState(getDisplay().getState().getText());
      pkForm.setCountry(getDisplay().getCountry().getText());
      dto.setPublicForm(pkForm);
    } else {
      dto.setPublicImport(getDisplay().getPublicKeyImport().getText());
    }

    return dto;
  }

  private class CreateKeyPairCallBack implements ResponseCodeCallback {

    KeyPairForm keyPairForm;

    public CreateKeyPairCallBack(KeyPairForm keyPairForm) {
      this.keyPairForm = keyPairForm;
    }

    @Override
    public void onResponseCode(Request request, Response response) {

      if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new KeyPairCreatedEvent(functionalUnit, keyPairForm.getAlias()));
        getDisplay().hideDialog();
      } else {
        ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, error.getStatus(), null));
      }
    }
  }

  //
  // Validation
  //

  private abstract class AbstractValidationHandler implements ValidationHandler {

    protected abstract Set<FieldValidator> getValidators();

    @Override
    public boolean validate() {
      List<String> messages = new ArrayList<String>();
      String message;
      for(FieldValidator validator : getValidators()) {
        message = validator.validate();
        if(message != null) {
          messages.add(message);
        }
      }

      if(messages.size() > 0) {
        eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, messages, null));
        return false;
      } else {
        return true;
      }
    }

  }

  private class PrivateKeyValidationHandler extends AbstractValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getAlias(), "KeyPairAliasIsRequired"));
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyCreate(), new RequiredTextValidator(getDisplay().getAlgorithm(), "KeyPairAlgorithmIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyCreate(), new RequiredTextValidator(getDisplay().getKeySize(), "KeyPairKeySizeIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyImport(), new RequiredTextValidator(getDisplay().getPrivateKeyImport(), "KeyPairPrivateKeyPEMIsRequired")));
      return validators;
    }

  }

  private class PublicKeyValidationHandler extends AbstractValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getFirstAndLastName(), "KeyPairFirstAndLastNameIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getOrganizationalUnit(), "KeyPairOrganizationalUnitIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getOrganizationName(), "KeyPairOrganizationNameIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getCity(), "KeyPairCityNameIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getState(), "KeyPairStateNameIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyCreate(), new RequiredTextValidator(getDisplay().getCountry(), "KeyPairCountryCodeIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPublicKeyImport(), new RequiredTextValidator(getDisplay().getPublicKeyImport(), "KeyPairPublicKeyPEMIsRequired")));
      return validators;
    }

  }

}
