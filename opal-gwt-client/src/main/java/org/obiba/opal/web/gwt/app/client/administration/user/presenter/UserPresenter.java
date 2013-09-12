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

import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.GroupSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.UserDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class UserPresenter extends ModalPresenterWidget<UserPresenter.Display> implements UserUiHandlers {

  private static final int MIN_PASSWORD_LENGTH = 6;

  private static final Translations translations = GWT.create(Translations.class);

  private UserDto userDto;

  private Mode dialogMode;

  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public UserPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  @Override
  public void save() {

    if(dialogMode == Mode.CREATE) {
      userDto = UserDto.create();
      if(getView().getUserName().isEmpty()) {
        getView().setNameError(translations.userMessageMap().get("UserNameRequiredError"));
        return;
      }

      // Password must be set when creating a user
      if(getView().getPassword().isEmpty() || getView().getConfirmPassword().isEmpty()) {
        getView().setPasswordError(translations.userMessageMap().get("UserPasswordRequiredError"));
        return;
      }

      userDto.setName(getView().getUserName());
    }

    // Update password only when password is not empty (to allow updating groups only)
    if(!getView().getPassword().isEmpty()) {
      if(getView().getPassword().length() < MIN_PASSWORD_LENGTH) {
        getView().setPasswordError(TranslationsUtils
            .replaceArguments(translations.userMessageMap().get("UserPasswordLengthError"),
                Arrays.asList(String.valueOf(MIN_PASSWORD_LENGTH))));
        return;
      }
      if(!getView().getPassword().equals(getView().getConfirmPassword())) {
        getView().setPasswordError(translations.userMessageMap().get("UserPasswordMatchError"));
        return;
      }

      if(getView().getPassword().equals(getView().getConfirmPassword())) {
        userDto.setPassword(getView().getPassword());
      }
    }

    // update groups
    userDto.clearGroupsArray();
    userDto.setGroupsArray(getView().getGroups());

    if(dialogMode == Mode.CREATE) {
      // Create
      ResourceRequestBuilderFactory.newBuilder()//
          .forResource("/users").withResourceBody(UserDto.stringify(userDto)).accept("application/json")//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                getEventBus().fireEvent(new UsersRefreshEvent());
                getView().hideDialog();
              } else if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getView().setNameError(error.getStatus());
              } else {
                getView().hideDialog();
                getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
              }
            }
          }, Response.SC_OK, Response.SC_BAD_REQUEST, Response.SC_PRECONDITION_FAILED).post().send();
    } else {
      // Update
      ResourceRequestBuilderFactory.newBuilder()//
          .forResource("/user/" + userDto.getName()).withResourceBody(UserDto.stringify(userDto))
          .accept("application/json")//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() == Response.SC_OK) {
                getEventBus().fireEvent(new UsersRefreshEvent());
              } else if(response.getStatusCode() == Response.SC_BAD_REQUEST) {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getView().setNameError(error.getStatus());
              } else {
                getEventBus().fireEvent(NotificationEvent.newBuilder().error(response.getText()).build());
              }
            }
          }, Response.SC_OK, Response.SC_PRECONDITION_FAILED, Response.SC_INTERNAL_SERVER_ERROR,
              Response.SC_BAD_REQUEST).put().send();
      getView().hideDialog();
    }

  }

  @Override
  protected void onBind() {

    // Groups selection handler
    registerHandler(getView().addSearchSelectionHandler(new GroupSuggestionSelectionHandler()));
  }

  public void setUser(UserDto userDto) {
    this.userDto = userDto;
    if(userDto.getGroupsArray() != null) {
      for(int i = 0; i < userDto.getGroupsArray().length(); i++) {
        getView().addSearchItem(userDto.getGroups(i));
      }
    }

    getView().setUser(userDto.getName(), JsArrays.toList(userDto.getGroupsArray()));
    getView().usernameSetEnabled(false);
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
  }

  public interface Display extends PopupView, HasUiHandlers<UserUiHandlers> {

    void hideDialog();

    void setUser(String originalUserName, List<String> originalGroups);

    void usernameSetEnabled(boolean b);

    String getUserName();

    JsArrayString getGroups();

    String getPassword();

    String getConfirmPassword();

    void setPasswordError(String message);

    void addSearchItem(String text);

    HandlerRegistration addSearchSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler);

    void setNameError(String message);
  }

  private class GroupSuggestionSelectionHandler implements SelectionHandler<SuggestOracle.Suggestion> {

    @Override
    public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
      // Get the table dto to fire the event to select the variable
      String group = ((GroupSuggestOracle.GroupSuggestion) event.getSelectedItem()).getGroup();
      userDto.addGroups(group);
      getView().setUser(getView().getUserName().isEmpty() ? userDto.getName() : getView().getUserName(),
          JsArrays.toList(userDto.getGroupsArray()));
    }
  }

}
