/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.users.edit;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.users.PasswordFieldValidators;
import org.obiba.opal.web.gwt.app.client.administration.users.SubjectCredentialsRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
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
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

public class SubjectCredentialsPresenter extends ModalPresenterWidget<SubjectCredentialsPresenter.Display>
    implements SubjectCredentialsUiHandlers {

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
              .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()) //
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
      PasswordFieldValidators passValidators = new PasswordFieldValidators(getView().getPassword(),
          getView().getConfirmPassword(), Display.FormField.PASSWORD.name());
      validators.addAll(passValidators.getValidators());
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
