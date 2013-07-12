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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.workbench.view.GroupSuggestOracle;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;
import org.obiba.opal.web.model.client.opal.UserDto;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class UserPresenter extends PresenterWidget<UserPresenter.Display> {

  private static final int MIN_PASSWORD_LENGTH = 6;

  private List<TableIndexStatusDto> tableIndexStatusDtos;

  private boolean refreshIndices = true;

  private boolean refreshTable = false;

  private UserDto userDto;

  private Mode dialogMode;

  public enum Mode {
    UPDATE, CREATE
  }

  @Inject
  public UserPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
  }

  @Override
  protected void onBind() {
    registerHandler(getView().getCancelButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        getView().hideDialog();
      }
    }));

    registerHandler(getView().getSaveButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        if(dialogMode.equals(Mode.CREATE)) {
          userDto = UserDto.create();
          userDto.setName(getView().getUserName());
        }

        // Update password
        if(getView().getPassword().length() < MIN_PASSWORD_LENGTH) {
          getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error("UserPasswordLengthError")
              .args(String.valueOf(MIN_PASSWORD_LENGTH)).build());
          getView().setPasswordError(true);
          return;
        }
        if(!getView().getPassword().equals(getView().getConfirmPassword())) {
          getEventBus().fireEvent(NotificationEvent.Builder.newNotification().error("UserPasswordMatchError").build());
          getView().setPasswordError(true);
          return;
        }

        if(getView().getPassword().equals(getView().getConfirmPassword())) {
          userDto.setPassword(getView().getPassword());
        }

        // update groups
        userDto.clearGroupsArray();
        userDto.setGroupsArray(getView().getGroups());

        if(dialogMode.equals(Mode.CREATE)) {
          // Create
          ResourceRequestBuilderFactory.newBuilder()//
              .forResource("/users").withResourceBody(UserDto.stringify(userDto)).accept("application/json")//
              .withCallback(new ResponseCodeCallback() {
                @Override
                public void onResponseCode(Request request, Response response) {
                  if(response.getStatusCode() == Response.SC_OK) {
                    getEventBus().fireEvent(new UsersRefreshEvent());
                    getEventBus().fireEvent(
                        NotificationEvent.Builder.newNotification().info("UserCreatedOk").args(userDto.getName())
                            .build());
                    getView().hideDialog();
                  } else if(response.getStatusCode() == Response.SC_CONFLICT) {
                    getEventBus().fireEvent(
                        NotificationEvent.Builder.newNotification().error("UserAlreadyExists").args(userDto.getName())
                            .build());
                  } else {
                    getEventBus()
                        .fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
                    getView().hideDialog();
                  }
                }
              }, Response.SC_OK, Response.SC_CONFLICT).post().send();
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
                    getEventBus().fireEvent(
                        NotificationEvent.Builder.newNotification().info("UserUpdatedOk").args(userDto.getName())
                            .build());
                  } else {
                    getEventBus()
                        .fireEvent(NotificationEvent.Builder.newNotification().error(response.getText()).build());
                  }
                }
              }, Response.SC_OK, Response.SC_PRECONDITION_FAILED, Response.SC_INTERNAL_SERVER_ERROR).put().send();
          getView().hideDialog();
        }

      }
    }));

    // Groups selection handler
    registerHandler(getView().addSearchSelectionHandler(new GroupSuggestionSelectionHandler()));
  }

  public void setUser(UserDto userDto) {
    this.userDto = userDto;
    for(int i = 0; i < userDto.getGroupsArray().length(); i++) {
      getView().addSearchItem(userDto.getGroups(i));
    }

    getView().setUser(userDto.getName(), JsArrays.toList(userDto.getGroupsArray()));
    getView().usernameSetEnabled(false);
  }

  public void setDialogMode(Mode mode) {
    dialogMode = mode;
  }

  public interface Display extends PopupView {

    void hideDialog();

    HasClickHandlers getSaveButton();

    HasClickHandlers getCancelButton();

    void setUser(String originalUserName, List<String> originalGroups);

    void usernameSetEnabled(boolean b);

    String getUserName();

    JsArrayString getGroups();

    String getPassword();

    String getConfirmPassword();

    void setPasswordError(boolean b);

    void addSearchItem(String text);

    HandlerRegistration addSearchSelectionHandler(SelectionHandler<SuggestOracle.Suggestion> handler);

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
