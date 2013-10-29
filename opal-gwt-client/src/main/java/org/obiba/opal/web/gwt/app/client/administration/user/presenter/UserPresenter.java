/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.user.presenter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
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
import org.obiba.opal.web.model.client.opal.UserDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

public class UserPresenter extends ModalPresenterWidget<UserPresenter.Display> implements UserUiHandlers {

  private static final int MIN_PASSWORD_LENGTH = 6;

  private final Translations translations;

  protected ValidationHandler validationHandler;

  private Mode dialogMode;

  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public UserPresenter(Display display, EventBus eventBus, Translations translations) {
    super(eventBus, display);
    this.translations = translations;
    getView().setUiHandlers(this);
  }

  @Override
  public void onBind() {
    validationHandler = new UserValidationHandler();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save() {

    getView().clearErrors();

    if(validationHandler.validate()) {

      ResponseCodeCallback callback = new ResponseCodeCallback() {
        @Override
        public void onResponseCode(Request request, Response response) {
          getEventBus().fireEvent(new UsersRefreshedEvent());
          getView().hideDialog();
        }
      };

      UserDto userDto = getDto();

      switch(dialogMode) {
        case CREATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource("/users") //
              .withResourceBody(UserDto.stringify(userDto)) //
              .withCallback(SC_OK, callback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .post().send();
          break;
        case UPDATE:
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource("/user/" + userDto.getName()) //
              .withResourceBody(UserDto.stringify(userDto)) //
              .withCallback(SC_OK, callback) //
              .withCallback(SC_BAD_REQUEST, new ErrorResponseCallback(getView().asWidget())) //
              .put().send();
          break;
      }
    }
  }

  private UserDto getDto() {
    UserDto dto = UserDto.create();
    dto.setName(getView().getName().getText());
    dto.setPassword(getView().getPassword().getText());
    dto.setEnabled(true);
    dto.setGroupsArray(JsArrays.fromIterable(getView().getGroups().getValue()));
    return dto;
  }

  public void setUser(UserDto userDto) {
    getView().getName().setText(userDto.getName());
    getView().getGroups().setValue(JsArrays.toList(userDto.getGroupsArray()));
    getView().setNamedEnabled(false);
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
  }

  private class UserValidationHandler extends ViewValidationHandler {

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      if(validators == null) {
        validators = new LinkedHashSet<FieldValidator>();

        if(dialogMode == Mode.CREATE) {
          validators.add(
              new RequiredTextValidator(getView().getName(), "UsernameIsRequired", Display.FormField.USERNAME.name()));
          validators.add(new RequiredTextValidator(getView().getPassword(), "PasswordIsRequired",
              Display.FormField.PASSWORD.name()));

        }

        ConditionValidator minLength = new ConditionValidator(minLengthCondition(getView().getPassword()),
            "PasswordLengthMin", Display.FormField.PASSWORD.name());
        minLength.setArgs(Arrays.asList(String.valueOf(MIN_PASSWORD_LENGTH)));
        validators.add(minLength);

        validators.add(
            new ConditionValidator(passwordsMatchCondition(getView().getPassword(), getView().getConfirmPassword()),
                "PasswordsMustMatch", Display.FormField.PASSWORD.name()));
      }
      return validators;
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

  public interface Display extends PopupView, HasUiHandlers<UserUiHandlers> {

    enum FormField {
      USERNAME,
      PASSWORD
    }

    void hideDialog();

    HasText getName();

    TakesValue<List<String>> getGroups();

    void setNamedEnabled(boolean enabled);

    HasText getPassword();

    HasText getConfirmPassword();

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

}
