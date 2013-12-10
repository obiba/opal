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
import org.obiba.opal.web.gwt.app.client.administration.user.event.GroupsRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.administration.user.event.UsersRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.HasBreadcrumbs;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.support.DefaultBreadcrumbsBuilder;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.ui.celltable.HasActionHandler;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.GroupDto;
import org.obiba.opal.web.model.client.opal.SubjectCredentialsDto;

import com.google.gwt.core.client.JsArray;
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

import static com.google.gwt.http.client.Response.SC_OK;

public class UserAdministrationPresenter
    extends ItemAdministrationPresenter<UserAdministrationPresenter.Display, UserAdministrationPresenter.Proxy>
    implements UserAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.USERS_GROUPS)
  public interface Proxy extends ProxyPlace<UserAdministrationPresenter> {}

  private final ModalProvider<UserPresenter> userModalProvider;

  private Runnable removeConfirmation;

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
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()).get()
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListUsersAuthorization())).send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
    getView().getUsersTable().setVisibleRange(0, 10);
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()).post().authorize(authorizer).send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageUsersAndApplicationsTitle();
  }

  @Override
  public void onAddUser() {
    userModalProvider.get().setDialogMode(UserPresenter.Mode.CREATE);
  }

  @Override
  protected void onBind() {
    super.onBind();

    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));

    // Refresh user list
    registerHandler(
        getEventBus().addHandler(UsersRefreshedEvent.getType(), new UsersRefreshedEvent.UsersRefreshedHandler() {
          @Override
          public void onUsersRefreshed(UsersRefreshedEvent event) {
            refreshUsers();
            refreshGroups();
          }
        }));

    // Refresh group list
    registerHandler(
        getEventBus().addHandler(GroupsRefreshedEvent.getType(), new GroupsRefreshedEvent.GroupsRefreshedHandler() {
          @Override
          public void onGroupsRefreshed(GroupsRefreshedEvent event) {
            refreshUsers();
            refreshGroups();
          }
        }));

    // User Actions
    getView().getUsersActions().setActionHandler(new ActionHandler<SubjectCredentialsDto>() {

      @Override
      public void doAction(SubjectCredentialsDto object, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          UserPresenter dialog = userModalProvider.get();
          dialog.setDialogMode(UserPresenter.Mode.UPDATE);
          dialog.setSubjectCredentials(object);
        } else if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          removeConfirmation = new RemoveRunnable(object.getName(), true);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeUser"),
                  translations.confirmationMessageMap().get("confirmRemoveUser").replace("{0}", object.getName())));
        } else if(Display.DISABLE_ACTION.equals(actionName) || Display.ENABLE_ACTION.equals(actionName)) {
          object.setEnabled(!object.getEnabled());
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.SUBJECT_CREDENTIAL.create().build(object.getName())) //
              .withResourceBody(SubjectCredentialsDto.stringify(object)) //
              .withCallback(new ResponseCodeCallback() {
                @Override
                public void onResponseCode(Request request, Response response) {
                  getEventBus().fireEvent(new UsersRefreshedEvent());
                }
              }, SC_OK).put().send();
        }
      }
    });

    // Groups actions
    getView().getGroupsActions().setActionHandler(new ActionHandler<GroupDto>() {

      @Override
      public void doAction(GroupDto object, String actionName) {
        if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          removeConfirmation = new RemoveRunnable(object.getName(), false);

          if(object.getUsersCount() > 0) {
            fireEvent(ConfirmationRequiredEvent
                .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeGroup"),
                    translations.confirmationMessageMap().get("confirmRemoveGroupWithUsers")
                        .replace("{0}", object.getName())));
          } else {
            fireEvent(ConfirmationRequiredEvent
                .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeGroup"),
                    translations.confirmationMessageMap().get("confirmRemoveGroup").replace("{0}", object.getName())));
          }
        }
      }
    });
  }

  private void refreshUsers() {
    ResourceRequestBuilderFactory.<JsArray<SubjectCredentialsDto>>newBuilder() //
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<SubjectCredentialsDto>>() {

          @Override
          public void onResource(Response response, JsArray<SubjectCredentialsDto> resource) {
            getView().renderUserRows(resource);
          }
        }) //
        .get().send();
  }

  private void refreshGroups() {
    // Fetch all groups
    ResourceRequestBuilderFactory.<JsArray<GroupDto>>newBuilder() //
        .forResource(UriBuilders.GROUPS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<GroupDto>>() {

          @Override
          public void onResource(Response response, JsArray<GroupDto> resource) {
            getView().renderGroupRows(resource);
          }
        }) //
        .get().send();
  }

  private final class ListUsersAuthorization implements HasAuthorization {

    @Override
    public void beforeAuthorization() {
    }

    @Override
    public void authorized() {
      refreshUsers();
      refreshGroups();
    }

    @Override
    public void unauthorized() {
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
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(
              isUser ? UriBuilders.SUBJECT_CREDENTIAL.create().build(name) : UriBuilders.GROUP.create().build(name)) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(isUser ? new UsersRefreshedEvent() : new GroupsRefreshedEvent());
            }
          }).delete().send();
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

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<UserAdministrationUiHandlers> {

    String ENABLE_ACTION = "Enable";

    String DISABLE_ACTION = "Disable";

    void renderUserRows(JsArray<SubjectCredentialsDto> rows);

    void renderGroupRows(JsArray<GroupDto> rows);

    void clear();

    HasData<SubjectCredentialsDto> getUsersTable();

    HasActionHandler<SubjectCredentialsDto> getUsersActions();

    HasActionHandler<GroupDto> getGroupsActions();
  }
}
