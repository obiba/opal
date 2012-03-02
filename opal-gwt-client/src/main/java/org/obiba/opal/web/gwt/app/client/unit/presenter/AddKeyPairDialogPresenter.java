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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.unit.event.KeyPairCreatedEvent;
import org.obiba.opal.web.gwt.app.client.validator.AbstractValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ConditionalValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.IsNotEqualValidator;
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
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class AddKeyPairDialogPresenter extends PresenterWidget<AddKeyPairDialogPresenter.Display> {

  private FunctionalUnitDto functionalUnit;

  public interface Display extends PopupView {

    void hideDialog();

    HasCloseHandlers<PopupPanel> getDialog();

    HasText getAlias();

    // True when a KeyPair is being added
    HasValue<Boolean> isKeyPair();

    // True when a Certificate is being added
    HasValue<Boolean> isCertificate();

    // Contains the Certificate as PEM-format
    HasValue<String> getCertificatePem();

    // True when creating a new private key
    HasValue<Boolean> isPrivateKeyCreate();

    // True when creating importing a private key
    HasValue<Boolean> isPrivateKeyImport();

    HasText getAlgorithm();

    HasText getKeySize();

    HasValue<String> getPrivateKeyImport();

    String getDefaultPrivateKeyText();

    HasValue<Boolean> isPublicKeyCreate();

    HasValue<Boolean> isPublicKeyImport();

    HasValue<String> getPublicKeyImport();

    String getDefaultPublicKeyText();

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
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    super.onBind();
    addEventHandlers();
    addValidators();
  }

  private void addValidators() {
    getView().setKeyTypeValidationHandler(new KeyTypeValidationHandler());
    getView().setCertificateStepHandler(new CertificateStepValidationHandler());
    getView().setPrivateKeyValidationHandler(new PrivateKeyValidationHandler());
    getView().setPublicKeyValidationHandler(new PublicKeyValidationHandler());
  }

  private void addEventHandlers() {
    super.registerHandler(getView().addFinishClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        KeyForm form = null;
        if(getView().isKeyPair().getValue()) {
          form = createKeyPair();
        } else if(getView().isCertificate().getValue()) {
          form = createCertificate();
        } else {
          throw new IllegalStateException("unknown key type");
        }

        CreateKeyPairCallBack callbackHandler = new CreateKeyPairCallBack(form);
        ResourceRequestBuilderFactory.newBuilder().forResource("/functional-unit/" + functionalUnit.getName() + "/keys").post().withResourceBody(KeyForm.stringify(form)).withCallback(Response.SC_CREATED, callbackHandler).withCallback(Response.SC_BAD_REQUEST, callbackHandler).send();
      }

    }));

    super.registerHandler(getView().addCancelClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

  }

  public void setFunctionalUnit(FunctionalUnitDto functionalUnit) {
    this.functionalUnit = functionalUnit;
  }

  private KeyForm createCertificate() {
    KeyForm dto = KeyForm.create();
    dto.setKeyType(KeyType.CERTIFICATE);
    dto.setAlias(getView().getAlias().getText());
    dto.setPublicImport(getView().getCertificatePem().getValue());
    return dto;
  }

  private KeyForm createKeyPair() {
    KeyForm dto = KeyForm.create();
    dto.setKeyType(KeyType.KEY_PAIR);
    dto.setAlias(getView().getAlias().getText());
    setPrivateKey(dto);
    setPublicKey(dto);
    return dto;
  }

  private void setPrivateKey(KeyForm dto) {
    if(getView().isPrivateKeyCreate().getValue()) {
      PrivateKeyForm pkForm = PrivateKeyForm.create();
      pkForm.setAlgo(getView().getAlgorithm().getText());
      pkForm.setSize(Integer.parseInt(getView().getKeySize().getText()));
      dto.setPrivateForm(pkForm);
    } else {
      dto.setPrivateImport(getView().getPrivateKeyImport().getValue());
    }
  }

  private void setPublicKey(KeyForm dto) {
    if(getView().isPublicKeyCreate().getValue()) {
      PublicKeyForm pkForm = PublicKeyForm.create();
      pkForm.setName(getView().getFirstAndLastName().getText());
      pkForm.setOrganizationalUnit(getView().getOrganizationalUnit().getText());
      pkForm.setOrganization(getView().getOrganizationName().getText());
      pkForm.setLocality(getView().getCity().getText());
      pkForm.setState(getView().getState().getText());
      pkForm.setCountry(getView().getCountry().getText());
      dto.setPublicForm(pkForm);
    } else {
      dto.setPublicImport(getView().getPublicKeyImport().getValue());
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
        getEventBus().fireEvent(new KeyPairCreatedEvent(functionalUnit, KeyForm.getAlias()));
        getView().hideDialog();
      } else {
        ClientErrorDto error = (ClientErrorDto) JsonUtils.unsafeEval(response.getText());
        getEventBus().fireEvent(NotificationEvent.newBuilder().error(error.getStatus()).build());
      }
    }
  }

  private class KeyTypeValidationHandler extends AbstractValidationHandler {

    KeyTypeValidationHandler() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getAlias(), "KeyPairAliasIsRequired"));
      return validators;
    }
  }

  private class CertificateStepValidationHandler extends AbstractValidationHandler {
    CertificateStepValidationHandler() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new RequiredTextValidator(getView().getCertificatePem(), "KeyPairPublicKeyPEMIsRequired"));
      validators.add(new IsNotEqualValidator<String>(getView().getCertificatePem(), getView().getDefaultPublicKeyText(), "KeyPairPublicKeyPEMIsRequired"));
      return validators;
    }
  }

  private class PrivateKeyValidationHandler extends AbstractValidationHandler {

    public PrivateKeyValidationHandler() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ConditionalValidator(getView().isPrivateKeyCreate(), new RequiredTextValidator(getView().getAlgorithm(), "KeyPairAlgorithmIsRequired")));
      validators.add(new ConditionalValidator(getView().isPrivateKeyCreate(), new RequiredTextValidator(getView().getKeySize(), "KeyPairKeySizeIsRequired")));
      validators.add(new ConditionalValidator(getView().isPrivateKeyImport(), new RequiredTextValidator(getView().getPrivateKeyImport(), "KeyPairPrivateKeyPEMIsRequired")));
      validators.add(new ConditionalValidator(getView().isPrivateKeyImport(), new IsNotEqualValidator<String>(getView().getPrivateKeyImport(), getView().getDefaultPrivateKeyText(), "KeyPairPrivateKeyPEMIsRequired")));
      return validators;
    }
  }

  private class PublicKeyValidationHandler extends AbstractValidationHandler {

    public PublicKeyValidationHandler() {
      super(getEventBus());
    }

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getFirstAndLastName(), "KeyPairFirstAndLastNameIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getOrganizationalUnit(), "KeyPairOrganizationalUnitIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getOrganizationName(), "KeyPairOrganizationNameIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getCity(), "KeyPairCityNameIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getState(), "KeyPairStateNameIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyCreate(), new RequiredTextValidator(getView().getCountry(), "KeyPairCountryCodeIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyImport(), new RequiredTextValidator(getView().getPublicKeyImport(), "KeyPairPublicKeyPEMIsRequired")));
      validators.add(new ConditionalValidator(getView().isPublicKeyImport(), new IsNotEqualValidator<String>(getView().getPublicKeyImport(), getView().getDefaultPublicKeyText(), "KeyPairPrivateKeyPEMIsRequired")));
      return validators;
    }

  }

}
