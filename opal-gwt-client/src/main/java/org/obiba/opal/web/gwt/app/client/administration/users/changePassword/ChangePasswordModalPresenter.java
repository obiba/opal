/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.users.changePassword;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.users.PasswordFieldValidators;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.PasswordDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class ChangePasswordModalPresenter extends ModalPresenterWidget<ChangePasswordModalPresenter.Display>
    implements ChangePasswordModalUiHandlers {

  private final Translations translations;

  private String principal;

  @Inject
  public ChangePasswordModalPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    getView().clearErrors();
    if(new ViewValidator().validate()) {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(UriBuilders.SUBJECT_CREDENTIAL_PASSWORD_UPDATE.create().build()) //
          .withResourceBody(PasswordDto.stringify(getDto())) //
          .withCallback(SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getView().close();
              fireEvent(NotificationEvent.newBuilder().info(translations.passwordChanged()).build());
            }
          }) //
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
              getView().showError(errorDto.getStatus(), JsArrays.toList(errorDto.getArgumentsArray()));
            }
          }, SC_BAD_REQUEST, SC_NOT_FOUND) //
          .put().send();
    }
  }

  public void setPrincipal(String name) {
    principal = name;
  }

  public interface Display extends PopupView, HasUiHandlers<ChangePasswordModalUiHandlers> {

    enum FormField {
      OLD_PASSWORD,
      NEW_PASSWORD
    }

    void clearErrors();

    void showError(String messageKey, List<String> args);

    void showError(@Nullable FormField formField, String message);

    HasText getOldPassword();

    HasText getNewPassword();

    HasText getConfirmPassword();

    void close();
  }

  private PasswordDto getDto() {
    PasswordDto dto = PasswordDto.create();
    dto.setName(principal);
    dto.setOldPassword(getView().getOldPassword().getText());
    dto.setNewPassword(getView().getNewPassword().getText());

    return dto;
  }

  private class ViewValidator extends ViewValidationHandler {

    @Override
    protected Set<FieldValidator> getValidators() {
      Set<FieldValidator> validators = new LinkedHashSet<FieldValidator>();

      validators.add(new RequiredTextValidator(getView().getOldPassword(), "OldPasswordIsRequired",
          Display.FormField.OLD_PASSWORD.name()));

      PasswordFieldValidators passValidators = new PasswordFieldValidators(getView().getNewPassword(),
          getView().getConfirmPassword(), Display.FormField.NEW_PASSWORD.name());
      validators.addAll(passValidators.getValidators());

      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(Display.FormField.valueOf(id), message);
    }
  }
}
