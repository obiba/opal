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

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.user.event.GroupsRefreshEvent;
import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.IconActionCell;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.UserDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class UserAdministrationPresenter
    extends ItemAdministrationPresenter<UserAdministrationPresenter.Display, UserAdministrationPresenter.Proxy>
    implements UserAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.USERS_GROUPS)
  public interface Proxy extends ProxyPlace<UserAdministrationPresenter> {}

  private final ModalProvider<UserPresenter> userModalProvider;

  private Runnable removeConfirmation;

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<UserAdministrationUiHandlers> {

    String PERMISSIONS_ACTION = "Permissions";

    void renderUserRows(JsArray<UserDto> rows);

    void renderGroupRows(JsArray<GroupDto> rows);

    void clear();

    HasClickHandlers getAddUserButton();

    void setDelegate(IconActionCell.Delegate<UserDto> delegate);

    HasData<UserDto> getUsersTable();

    HasActionHandler<UserDto> getUsersActions();

    HasActionHandler<GroupDto> getGroupsActions();
  }

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public UserAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<UserPresenter> userModalProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    getView().setUiHandlers(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.userModalProvider = userModalProvider.setContainer(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/users").get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListUsersAuthorization())).send();
  }

  @Override
  public String getName() {
    return translations.indicesLabel();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    getView().getUsersTable().setVisibleRange(0, 10);
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder().forResource("/users").post().authorize(authorizer).send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageUsersAndGroupsTitle();
  }

  @Override
  public void onUsersSelected() {
    ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
        .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

      @Override
      public void onResource(Response response, JsArray<UserDto> resource) {
        getView().renderUserRows(resource);
      }
    }).get().send();
  }

  @Override
  public void onGroupsSelected() {
    // Fetch all groups
    ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder()//
        .forResource("/groups").withCallback(new ResourceCallback<JsArray<GroupDto>>() {

      @Override
      public void onResource(Response response, JsArray<GroupDto> resource) {
        getView().renderGroupRows(resource);
      }
    }).get().send();
  }

  @Override
  protected void onBind() {
    super.onBind();

    getView().setDelegate(new UserStatusChangeDelegate());

    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));

    // Refresh user list
    registerHandler(getEventBus().addHandler(UsersRefreshEvent.getType(), new UsersRefreshEvent.Handler() {
      @Override
      public void onRefresh(UsersRefreshEvent event) {
        ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
            .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

          @Override
          public void onResource(Response response, JsArray<UserDto> resource) {
            getView().renderUserRows(resource);
          }
        }).get().send();
      }
    }));

    // Refresh group list
    registerHandler(getEventBus().addHandler(GroupsRefreshEvent.getType(), new GroupsRefreshEvent.Handler() {
      @Override
      public void onRefresh(GroupsRefreshEvent event) {
        ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder()//
            .forResource("/groups").withCallback(new ResourceCallback<JsArray<GroupDto>>() {

          @Override
          public void onResource(Response response, JsArray<GroupDto> resource) {
            getView().renderGroupRows(resource);
          }
        }).get().send();
      }
    }));

    // Add user
    getView().getAddUserButton().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        UserPresenter dialog = userModalProvider.get();
        dialog.setDialogMode(UserPresenter.Mode.CREATE);
      }
    });

    // User Actions
    getView().getUsersActions().setActionHandler(new ActionHandler<UserDto>() {

      @Override
      public void doAction(UserDto object, String actionName) {
        if(actionName.trim().equalsIgnoreCase(ActionsColumn.EDIT_ACTION)) {
          UserPresenter dialog = userModalProvider.get();
          dialog.setDialogMode(UserPresenter.Mode.UPDATE);
          dialog.setUser(object);
        } else if(actionName.trim().equalsIgnoreCase(ActionsColumn.DELETE_ACTION)) {
          removeConfirmation = new RemoveRunnable(object.getName(), true);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeUser"),
                  translations.confirmationMessageMap().get("confirmRemoveUser").replace("{0}", object.getName())));
        }
      }
    });

    // Groups actions
    getView().getGroupsActions().setActionHandler(new ActionHandler<GroupDto>() {

      @Override
      public void doAction(GroupDto object, String actionName) {
        if(actionName.trim().equalsIgnoreCase(ActionsColumn.DELETE_ACTION)) {
          removeConfirmation = new RemoveRunnable(object.getName(), false);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeGroup"),
                  translations.confirmationMessageMap().get("confirmRemoveGroup").replace("{0}", object.getName())));
        }
      }
    });
  }

  private final class ListUsersAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {

      // Fetch all users
      ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
          .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

        @Override
        public void onResource(Response response, JsArray<UserDto> resource) {
          getView().renderUserRows(resource);
        }
      }).get().send();
    }

    @Override
    public void unauthorized() {
    }
  }

  public class UserStatusChangeDelegate implements IconActionCell.Delegate<UserDto> {
    @Override
    public void executeClick(NativeEvent event, UserDto value) {
      // Enable/Disable user all groups
      value.setEnabled(!value.getEnabled());
      ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
          .forResource("/user/" + value.getName()).withResourceBody(UserDto.stringify(value))
          .accept("application/json")//
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              // Fetch all users
              ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
                  .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

                @Override
                public void onResource(Response response, JsArray<UserDto> resource) {
                  getView().renderUserRows(resource);

                }
              }).get().send();
            }
          })//
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              if(response.getStatusCode() != Response.SC_OK) {
                ClientErrorDto error = JsonUtils.unsafeEval(response.getText());
                getEventBus().fireEvent(
                    NotificationEvent.Builder.newNotification().error(error.getStatus()).args(error.getArgumentsArray())
                        .build());
              }
            }
          }).put().send();

    }

    @Override
    public void executeMouseDown(NativeEvent event, UserDto value) {
      // empty
    }
  }

  // Remove group/user confirmation event
  private class RemoveRunnable implements Runnable {

    private final String name;

    private final boolean isUser;

    RemoveRunnable(String name, boolean isUser) {
      this.name = name;
      this.isUser = isUser;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder()//
          .forResource(UriBuilder.create().segment(isUser ? "user" : "group", name).build())
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(isUser ? new UsersRefreshEvent() : new GroupsRefreshEvent());
            }
          }, Response.SC_OK).delete().send();
    }
  }

  private class RemoveConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(removeConfirmation != null && event.getSource().equals(removeConfirmation) && event.isConfirmed()) {
        removeConfirmation.run();
        removeConfirmation = null;
      }
    }
  }
}
