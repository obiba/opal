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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.administration.presenter.ItemAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.presenter.RequestAdministrationPermissionEvent;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.SubjectCredentialsDtos;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.event.GroupsRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.event.SubjectCredentialsRefreshedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
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
import org.obiba.opal.web.model.client.opal.SubjectCredentialsType;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
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

public class SubjectCredentialsAdministrationPresenter extends
    ItemAdministrationPresenter<SubjectCredentialsAdministrationPresenter.Display, SubjectCredentialsAdministrationPresenter.Proxy>
    implements SubjectCredentialsAdministrationUiHandlers {

  @ProxyStandard
  @NameToken(Places.SUBJECT_CREDENTIALS)
  public interface Proxy extends ProxyPlace<SubjectCredentialsAdministrationPresenter> {}

  private final ModalProvider<SubjectCredentialsPresenter> modalProvider;

  private Runnable removeConfirmation;

  private final DefaultBreadcrumbsBuilder breadcrumbsHelper;

  @Inject
  public SubjectCredentialsAdministrationPresenter(Display display, EventBus eventBus, Proxy proxy,
      ModalProvider<SubjectCredentialsPresenter> modalProvider, DefaultBreadcrumbsBuilder breadcrumbsHelper) {
    super(eventBus, display, proxy);
    getView().setUiHandlers(this);
    this.breadcrumbsHelper = breadcrumbsHelper;
    this.modalProvider = modalProvider.setContainer(this);
  }

  @ProxyEvent
  @Override
  public void onAdministrationPermissionRequest(RequestAdministrationPermissionEvent event) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()) //
        .authorize(new CompositeAuthorizer(event.getHasAuthorization(), new ListUsersAuthorization())) //
        .get().send();
  }

  @Override
  public String getName() {
    return getTitle();
  }

  @Override
  protected void onReveal() {
    super.onReveal();
    breadcrumbsHelper.setBreadcrumbView(getView().getBreadcrumbs()).build();
  }

  @Override
  public void authorize(HasAuthorization authorizer) {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()).post().authorize(authorizer).send();
  }

  @Override
  @TitleFunction
  public String getTitle() {
    return translations.pageUsersGroupsAndApplicationsTitle();
  }

  @Override
  public void onAddUser() {
    SubjectCredentialsPresenter presenter = modalProvider.get();
    presenter.setDialogMode(SubjectCredentialsPresenter.Mode.CREATE);
    presenter.setSubjectCredentialsType(SubjectCredentialsType.USER);
    presenter.setTitle(translations.addUserLabel());
  }

  @Override
  public void onAddApplication() {
    SubjectCredentialsPresenter presenter = modalProvider.get();
    presenter.setDialogMode(SubjectCredentialsPresenter.Mode.CREATE);
    presenter.setSubjectCredentialsType(SubjectCredentialsType.APPLICATION);
    presenter.setTitle(translations.addApplicationLabel());
  }

  @Override
  protected void onBind() {
    super.onBind();

    // Register event handlers
    registerHandler(getEventBus().addHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler()));

    // Refresh user list
    registerHandler(getEventBus().addHandler(SubjectCredentialsRefreshedEvent.getType(),
        new SubjectCredentialsRefreshedEvent.SubjectCredentialsRefreshedHandler() {
          @Override
          public void onSubjectCredentialsRefreshed(SubjectCredentialsRefreshedEvent event) {
            refreshTables();
          }
        }));

    // Refresh group list
    registerHandler(
        getEventBus().addHandler(GroupsRefreshedEvent.getType(), new GroupsRefreshedEvent.GroupsRefreshedHandler() {
          @Override
          public void onGroupsRefreshed(GroupsRefreshedEvent event) {
            refreshTables();
          }
        }));

    // User Actions
    getView().getSubjectCredentialActions().setActionHandler(new ActionHandler<SubjectCredentialsDto>() {

      @Override
      public void doAction(SubjectCredentialsDto subjectDto, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          SubjectCredentialsPresenter dialog = modalProvider.get();
          dialog.setDialogMode(SubjectCredentialsPresenter.Mode.UPDATE);
          dialog.setSubjectCredentials(subjectDto);
        } else if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          removeConfirmation = new RemoveRunnable(subjectDto.getName(), true);
          fireEvent(ConfirmationRequiredEvent
              .createWithMessages(removeConfirmation, translations.confirmationTitleMap().get("removeUser"),
                  translations.confirmationMessageMap().get("confirmRemoveUser").replace("{0}", subjectDto.getName())));
        } else if(Display.DISABLE_ACTION.equals(actionName) || Display.ENABLE_ACTION.equals(actionName)) {
          subjectDto.setEnabled(!subjectDto.getEnabled());
          ResourceRequestBuilderFactory.newBuilder() //
              .forResource(UriBuilders.SUBJECT_CREDENTIAL.create().build(subjectDto.getName())) //
              .withResourceBody(SubjectCredentialsDto.stringify(subjectDto)) //
              .withCallback(new ResponseCodeCallback() {
                @Override
                public void onResponseCode(Request request, Response response) {
                  getEventBus().fireEvent(new SubjectCredentialsRefreshedEvent());
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

  private void refreshTables() {
    refreshUsersAndApplications();
    refreshGroups();
  }

  private void refreshUsersAndApplications() {
    ResourceRequestBuilderFactory.<JsArray<SubjectCredentialsDto>>newBuilder() //
        .forResource(UriBuilders.SUBJECT_CREDENTIALS.create().build()) //
        .withCallback(new ResourceCallback<JsArray<SubjectCredentialsDto>>() {
          @Override
          public void onResource(Response response, JsArray<SubjectCredentialsDto> resource) {
            List<SubjectCredentialsDto> users = new ArrayList<SubjectCredentialsDto>();
            List<SubjectCredentialsDto> applications = new ArrayList<SubjectCredentialsDto>();
            if(resource != null) {
              int length = resource.length();
              for(int i = 0; i < length; i++) {
                SubjectCredentialsDto dto = resource.get(i);
                if(SubjectCredentialsDtos.isUser(dto)) {
                  users.add(dto);
                } else if(SubjectCredentialsDtos.isApplication(dto)) {
                  applications.add(dto);
                }
              }
            }
            getView().renderUserRows(users);
            getView().renderApplicationRows(applications);
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
            getView().renderGroupRows(JsArrays.toList(JsArrays.toSafeArray(resource)));
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
      refreshTables();
    }

    @Override
    public void unauthorized() {
    }
  }

  // Remove group/user confirmation event
  private class RemoveRunnable implements Runnable {

    private final String name;

    private final boolean isSubjectCredentials;

    RemoveRunnable(String name, boolean isSubjectCredentials) {
      this.name = name;
      this.isSubjectCredentials = isSubjectCredentials;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(isSubjectCredentials
              ? UriBuilders.SUBJECT_CREDENTIAL.create().build(name)
              : UriBuilders.GROUP.create().build(name)) //
          .withCallback(Response.SC_OK, new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              getEventBus().fireEvent(
                  isSubjectCredentials ? new SubjectCredentialsRefreshedEvent() : new GroupsRefreshedEvent());
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

  public interface Display extends View, HasBreadcrumbs, HasUiHandlers<SubjectCredentialsAdministrationUiHandlers> {

    String ENABLE_ACTION = "Enable";

    String DISABLE_ACTION = "Disable";

    void renderUserRows(List<SubjectCredentialsDto> rows);

    void renderApplicationRows(List<SubjectCredentialsDto> rows);

    void renderGroupRows(List<GroupDto> rows);

    void clear();

    HasActionHandler<SubjectCredentialsDto> getSubjectCredentialActions();

    HasActionHandler<GroupDto> getGroupsActions();
  }
}
