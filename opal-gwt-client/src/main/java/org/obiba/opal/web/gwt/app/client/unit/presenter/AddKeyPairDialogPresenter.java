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

import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;
import org.obiba.opal.web.model.client.opal.KeyForm;
import org.obiba.opal.web.model.client.opal.KeyType;
import org.obiba.opal.web.model.client.opal.PrivateKeyForm;
import org.obiba.opal.web.model.client.opal.PublicKeyForm;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

    HasCloseHandlers<DialogBox> getDialog();

    HasText getAlias();

    HasValue<Boolean> isKeyPair();

    HasValue<Boolean> isCertificate();

    HasText getCertificatePem();

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

    void setKeyTypeValidationHandler(ValidationHandler handler);

    void setCertificateStepHandler(ValidationHandler handler);

    void setPrivateKeyValidationHandler(ValidationHandler handler);

    void setPublicKeyValidationHandler(ValidationHandler handler);

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
    getDisplay().setKeyTypeValidationHandler(new KeyTypeValidationHandler(eventBus));
    getDisplay().setCertificateStepHandler(new CertificateStepValidationHandler(eventBus));
    getDisplay().setPrivateKeyValidationHandler(new PrivateKeyValidationHandler(eventBus));
    getDisplay().setPublicKeyValidationHandler(new PublicKeyValidationHandler(eventBus));
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        KeyForm form = null;
        if(getDisplay().isKeyPair().getValue()) {
          form = createKeyPair();
        } else if(getDisplay().isCertificate().getValue()) {
          form = createCertificate();
        } else {
          throw new IllegalStateException("unknown key type");
        }

        CreateKeyPairCallBack callbackHandler = new CreateKeyPairCallBack(form);
        ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/keys").post().withResourceBody(KeyForm.stringify(form)).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
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

  private KeyForm createCertificate() {
    KeyForm dto = KeyForm.create();
    dto.setKeyType(KeyType.CERTIFICATE);
    dto.setAlias(getDisplay().getAlias().getText());
    dto.setPublicImport(getDisplay().getCertificatePem().getText());
    return dto;
  }

  private KeyForm createKeyPair() {
    KeyForm dto = KeyForm.create();
    dto.setKeyType(KeyType.KEY_PAIR);
    dto.setAlias(getDisplay().getAlias().getText());
    setPrivateKey(dto);
    setPublicKey(dto);
    return dto;
  }

  private void setPrivateKey(KeyForm dto) {
    if(getDisplay().isPrivateKeyCreate().getValue()) {
      PrivateKeyForm pkForm = PrivateKeyForm.create();
      pkForm.setAlgo(getDisplay().getAlgorithm().getText());
      pkForm.setSize(Integer.parseInt(getDisplay().getKeySize().getText()));
      dto.setPrivateForm(pkForm);
    } else {
      dto.setPrivateImport(getDisplay().getPrivateKeyImport().getText());
    }
  }

  private void setPublicKey(KeyForm dto) {
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
  }

  private class CreateKeyPairCallBack implements ResponseCodeCallback {

    KeyForm KeyForm;

    public CreateKeyPairCallBack(KeyForm KeyForm) {
      this.KeyForm = KeyForm;
    }

    @Override
    public void onResponseCode(Request request, Response response) {

      if(response.getStatusCode() == Response.SC_CREATED) {
        eventBus.fireEvent(new KeyPairCreatedEvent(functionalUnit, KeyForm.getAlias()));
        getDisplay().hideDialog();
      } else {
        ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
        eventBus.fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).build());
      }
    }
  }

  private class KeyTypeValidationHandler extends AbstractValidationHandler {

    KeyTypeValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getAlias(), "KeyPairAliasIsRequired"));
      return validators;
    }
  }

  private class CertificateStepValidationHandler extends AbstractValidationHandler {
    CertificateStepValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getDisplay().getCertificatePem(), "KeyPairPublicKeyPEMIsRequired"));
      return validators;
    }
  }

  private class PrivateKeyValidationHandler extends AbstractValidationHandler {

    public PrivateKeyValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyCreate(), new RequiredTextValidator(getDisplay().getAlgorithm(), "KeyPairAlgorithmIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyCreate(), new RequiredTextValidator(getDisplay().getKeySize(), "KeyPairKeySizeIsRequired")));
      validators.add(new ConditionalValidator(getDisplay().isPrivateKeyImport(), new RequiredTextValidator(getDisplay().getPrivateKeyImport(), "KeyPairPrivateKeyPEMIsRequired")));
      return validators;
    }

  }

  private class PublicKeyValidationHandler extends AbstractValidationHandler {

    public PublicKeyValidationHandler(EventBus eventBus) {
      super(eventBus);
    }

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
