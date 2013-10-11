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
import java.util.List;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.support.ErrorResponseCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_OK;

public class UserPresenter extends ModalPresenterWidget<UserPresenter.Display> implements UserUiHandlers {

  private static final int MIN_PASSWORD_LENGTH = 6;

  private final Translations translations;

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
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save() {

    getView().clearErrors();

    if(dialogMode == Mode.CREATE) {

      if(Strings.isNullOrEmpty(getView().getName().getText())) {
        getView().showError(Display.FormField.USERNAME, translations.userMessageMap().get("UserNameRequiredError"));
        return;
      }

      // Password must be set when creating a user
      if(getView().getPassword().isEmpty() || getView().getConfirmPassword().isEmpty()) {
        getView().showError(Display.FormField.PASSWORD, translations.userMessageMap().get("UserPasswordRequiredError"));
        return;
      }

    }

    // Update password only when password is not empty (to allow updating groups only)
    if(!getView().getPassword().isEmpty()) {
      if(getView().getPassword().length() < MIN_PASSWORD_LENGTH) {
        getView().showError(Display.FormField.PASSWORD, TranslationsUtils
            .replaceArguments(translations.userMessageMap().get("UserPasswordLengthError"),
                Arrays.asList(String.valueOf(MIN_PASSWORD_LENGTH))));
        return;
      }
      if(!getView().getPassword().equals(getView().getConfirmPassword())) {
        getView().showError(Display.FormField.PASSWORD, translations.userMessageMap().get("UserPasswordMatchError"));
        return;
      }

    }

    ResponseCodeCallback callback = new ResponseCodeCallback() {
      @Override
      public void onResponseCode(Request request, Response response) {
        getEventBus().fireEvent(new UsersRefreshEvent());
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

  private UserDto getDto() {
    UserDto dto = UserDto.create();
    dto.setName(getView().getName().getText());
    dto.setPassword(getView().getPassword());
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

  public interface Display extends PopupView, HasUiHandlers<UserUiHandlers> {

    enum FormField {
      USERNAME,
      PASSWORD
    }

    void hideDialog();

    HasText getName();

    TakesValue<List<String>> getGroups();

    void setNamedEnabled(boolean enabled);

    String getPassword();

    String getConfirmPassword();

    void showError(@Nullable FormField formField, String message);

    void clearErrors();

  }

}
