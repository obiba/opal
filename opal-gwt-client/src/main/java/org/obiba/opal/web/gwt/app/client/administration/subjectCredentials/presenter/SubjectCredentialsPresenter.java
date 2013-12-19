/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.presenter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.SubjectCredentialsDtos;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.event.SubjectCredentialsRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.validator.ConditionValidator;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.HasBooleanValue;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ValidationHandler;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

public class SubjectCredentialsPresenter extends ModalPresenterWidget<SubjectCredentialsPresenter.Display>
    implements SubjectCredentialsUiHandlers {

  private static final int MIN_PASSWORD_LENGTH = 6;

  private final TranslationMessages translationMessages;

  protected ValidationHandler validationHandler;

  private Mode dialogMode;

  private SubjectCredentialsDto.AuthenticationType authenticationType;

  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public SubjectCredentialsPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  @Override
  public void onBind() {
    validationHandler = new SubjectCredentialsValidationHandler();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save() {

    getView().clearErrors();

    if(validationHandler.validate()) {

      ResponseCodeCallback successCallback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          getEventBus().fireEvent(new SubjectCredentialsRefreshedEvent());
          getView().hideDialog();
        }
      };

      SubjectCredentialsDto dto = getDto();

      switch(dialogMode) {
        case CREATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.USER.create().build()) //
              .withResourceBody(SubjectCredentialsDto.stringify(dto)) //
              .withCallback(SC_OK, successCallback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .post().send();
          break;
        case UPDATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.SUBJECT_CREDENTIAL.create().build(dto.getName())) //
              .withResourceBody(SubjectCredentialsDto.stringify(dto)) //
              .withCallback(SC_OK, successCallback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .put().send();
          break;
      }
    }
  }

  private SubjectCredentialsDto getDto() {
    SubjectCredentialsDto dto = SubjectCredentialsDto.create();
    dto.setAuthenticationType(authenticationType);
    dto.setName(getView().getName().getText());
    dto.setPassword(getView().getPassword().getText());
    dto.setCertificate(getView().getCertificate().getText());
    dto.setEnabled(true);
    dto.setGroupsArray(JsArrays.fromIterable(getView().getGroups().getValue()));
    return dto;
  }

  public void setSubjectCredentials(SubjectCredentialsDto dto) {
    setAuthenticationType(dto.getAuthenticationType());
    getView().getName().setText(dto.getName());
    getView().getGroups().setValue(JsArrays.toList(dto.getGroupsArray()));
    getView().setNamedEnabled(false);
    setTitle(translationMessages.editUserLabel(dto.getName()));
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
  }

  public void setAuthenticationType(SubjectCredentialsDto.AuthenticationType authenticationType) {
    this.authenticationType = authenticationType;
    getView().getPasswordGroupVisibility().setVisible(SubjectCredentialsDtos.isPassword(authenticationType));
    getView().getCertificateGroupVisibility().setVisible(SubjectCredentialsDtos.isCertificate(authenticationType));
  }

  public void setTitle(String title) {
    getView().setTitle(title);
  }

  private class SubjectCredentialsValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators != null) {
        return validators;
      }

      validators = new LinkedHashSet<FieldValidator>();
      if(dialogMode == Mode.CREATE) {
        validators.add(new RequiredTextValidator(getView().getName(), "SubjectCredentialNameIsRequired",
            Display.FormField.NAME.name()));
        if(SubjectCredentialsDtos.isPassword(authenticationType)) {
          addPasswordValidators();
        } else if(SubjectCredentialsDtos.isCertificate(authenticationType)) {
          validators.add(new RequiredTextValidator(getView().getCertificate(), "CertificateIsRequired",
              Display.FormField.CERTIFICATE.name()));
        }
      }
      return validators;
    }

    private void addPasswordValidators() {
      validators.add(
          new RequiredTextValidator(getView().getPassword(), "PasswordIsRequired", Display.FormField.PASSWORD.name()));
      ConditionValidator minLength = new ConditionValidator(minLengthCondition(getView().getPassword()),
          "PasswordLengthMin", Display.FormField.PASSWORD.name());
      minLength.setArgs(Arrays.asList(String.valueOf(MIN_PASSWORD_LENGTH)));
      validators.add(minLength);
      validators.add(
          new ConditionValidator(passwordsMatchCondition(getView().getPassword(), getView().getConfirmPassword()),
              "PasswordsMustMatch", Display.FormField.PASSWORD.name()));
    }

    private HasValue<Boolean> minLengthCondition(final HasText password) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return password.getText().isEmpty() || password.getText().length() >= MIN_PASSWORD_LENGTH;
        }
      };
    }

    private HasValue<Boolean> passwordsMatchCondition(final HasText password, final HasText confirmPassword) {
      return new HasBooleanValue() {
        @Override
        public Boolean getValue() {
          return password.getText().isEmpty() && confirmPassword.getText().isEmpty() ||
              password.getText().equals(confirmPassword.getText());
        }
      };
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }

  public interface Display extends PopupView, HasUiHandlers<SubjectCredentialsUiHandlers> {

    enum FormField {
      NAME, PASSWORD, CERTIFICATE
    }

    void hideDialog();

    void setTitle(String title);

    HasText getName();

    TakesValue<List<String>> getGroups();

    void setNamedEnabled(boolean enabled);

    HasVisibility getPasswordGroupVisibility();

    HasText getPassword();

    HasText getConfirmPassword();

    HasVisibility getCertificateGroupVisibility();

    HasText getCertificate();

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

}
