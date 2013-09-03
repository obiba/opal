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
import com.google.gwt.user.client.Command;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.annotations.TitleFunction;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class UserAdministrationPresenter
    extends ItemAdministrationPresenter<UserAdministrationPresenter.Display, UserAdministrationPresenter.Proxy> {

  @ProxyStandard
  @NameToken(Places.usersGroups)
  public interface Proxy extends ProxyPlace<UserAdministrationPresenter> {}

  private final ModalProvider<UserPresenter> userModalProvider;

  public interface Display extends View, HasBreadcrumbs {

    String PERMISSIONS_ACTION = "Permissions";

    HasClickHandlers getUsersLink();

    void showUsers();

    void showGroups();

    HasClickHandlers getGroupsLink();

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

  @SuppressWarnings("UnusedDeclaration")
  private Command confirmedCommand;

  @Inject
  public UserAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<UserPresenter> userModalProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
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
    getView().showUsers();
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
  protected void onBind() {
    super.onBind();

    getView().setDelegate(new UserStatusChangeDelegate());
    getView().getUsersLink().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ResourceRequestBuilderFactory.<JsArray<UserDto>>newBuilder()//
            .forResource("/users").withCallback(new ResourceCallback<JsArray<UserDto>>() {

          @Override
          public void onResource(Response response, JsArray<UserDto> resource) {
            getView().renderUserRows(resource);
          }
        }).get().send();
        getView().showUsers();
      }
    });

    getView().getGroupsLink().addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // Fetch all groups
        ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder()//
            .forResource("/groups").withCallback(new ResourceCallback<JsArray<GroupDto>>() {

          @Override
          public void onResource(Response response, JsArray<GroupDto> resource) {
            getView().renderGroupRows(resource);
          }
        }).get().send();
        getView().showGroups();
      }
    });

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
        getView().showUsers();
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
        getView().showGroups();
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
      public void doAction(final UserDto object, String actionName) {
        if(actionName.trim().equalsIgnoreCase(ActionsColumn.EDIT_ACTION)) {
          UserPresenter dialog = userModalProvider.get();
          dialog.setDialogMode(UserPresenter.Mode.UPDATE);
          dialog.setUser(object);
        } else if(actionName.trim().equalsIgnoreCase(ActionsColumn.DELETE_ACTION)) {
          ResourceRequestBuilderFactory.newBuilder()//
              .forResource("/user/" + object.getName()).withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(new UsersRefreshEvent());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().info("UserDeletedOk").args(object.getName()).build());
            }
          }, Response.SC_OK).delete().send();
        }
      }
    });

    // Groups actions
    getView().getGroupsActions().setActionHandler(new ActionHandler<GroupDto>() {

      @Override
      public void doAction(final GroupDto object, String actionName) {
        if(actionName.trim().equalsIgnoreCase(ActionsColumn.DELETE_ACTION)) {
          ResourceRequestBuilderFactory.newBuilder()//
              .forResource("/group/" + object.getName()).withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(new GroupsRefreshEvent());
              getEventBus().fireEvent(
                  NotificationEvent.Builder.newNotification().info("GroupDeletedOk").args(object.getName()).build());
            }
          }, Response.SC_OK).delete().send();
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
    public void unauthorized() {
    }
  }

  public class UserStatusChangeDelegate implements IconActionCell.Delegate<UserDto> {
    @Override
    public void executeClick(NativeEvent event, final UserDto value) {
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
                  getEventBus().fireEvent(NotificationEvent.Builder.newNotification().info("UserStatusChangedOk")
                      .args(value.getName(),
                          value.getEnabled() ? translations.enabledLabel() : translations.disabledLabel()).build());

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

}
