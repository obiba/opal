/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.admin;

import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.edit.EditProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.project.keystore.ProjectKeyStorePresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display>
    implements ProjectAdministrationUiHandlers {

  private final PlaceManager placeManager;

  private final ModalProvider<EditProjectModalPresenter> editProjectModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final Provider<ProjectKeyStorePresenter> projectDataExchangeProvider;

  private ProjectDto project;

  private Runnable removeConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view, PlaceManager placeManager,
      ModalProvider<EditProjectModalPresenter> editProjectModalProvider,
      Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
      Provider<ProjectKeyStorePresenter> projectDataExchangeProvider, TranslationMessages translationMessages) {
    super(eventBus, view);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
    this.placeManager = placeManager;
    this.editProjectModalProvider = editProjectModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.projectDataExchangeProvider = projectDataExchangeProvider;
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ConfirmationEvent.getType(), new RemoveConfirmationEventHandler());
    addRegisteredHandler(ProjectUpdatedEvent.getType(), new ProjectUpdatedEvent.ProjectUpdatedHandler() {
      @Override
      public void onProjectUpdated(ProjectUpdatedEvent event) {
        placeManager.revealPlace(ProjectPlacesHelper.getAdministrationPlace(project.getName()));
      }
    });
  }

  public void setProject(ProjectDto project) {
    this.project = project;
    getView().setProject(project);
    authorize();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(project.getLink()) //
        .authorize(getView().getEditAuthorizer()) //
        .put().send();

    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_PERMISSIONS_PROJECT.create().build(project.getName())) //
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsSlotAuthorizer())) //
        .post().send();

    // set keystore
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_KEYSTORE.create().build(project.getName())) //
        .authorize(new CompositeAuthorizer(getView().getKeyStoreAuthorizer(), new KeyStoreSlotAuthorizer())) //
        .post().send();

    // delete project
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(project.getLink()) //
        .authorize(getView().getDeleteAuthorizer()) //
        .delete().send();
  }

  @Override
  public void onEdit() {
    EditProjectModalPresenter presenter = editProjectModalProvider.create();
    presenter.setProjectName(project.getName());
    editProjectModalProvider.show();
  }

  @Override
  public void onDelete() {
    removeConfirmation = new RemoveRunnable(project, false);
    fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeProject(),
        translationMessages.confirmRemoveProject()));
  }

  @Override
  public void onArchive() {
    removeConfirmation = new RemoveRunnable(project, true);
    fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.archiveProject(),
        translationMessages.confirmArchiveProject()));
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

  private class RemoveRunnable implements Runnable {

    private final ProjectDto projectDto;

    private final boolean archive;

    private RemoveRunnable(ProjectDto projectDto, boolean archive) {
      this.projectDto = projectDto;
      this.archive = archive;
    }

    @Override
    public void run() {
      ResponseCodeCallback callbackHandler = new ResponseCodeCallback() {

        @Override
        public void onResponseCode(Request request, Response response) {
          if(response.getStatusCode() == SC_OK) {
            PlaceRequest projectsRequest = new PlaceRequest.Builder().nameToken(Places.PROJECTS).build();
            placeManager.revealPlace(projectsRequest);
          } else {
            String errorMessage = response.getText().isEmpty() ? "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      UriBuilder uri = UriBuilders.PROJECT.create().query("archive", archive + "");
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(uri.build(projectDto.getName())) //
          .withCallback(callbackHandler, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND) //
          .delete().send();
    }
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsSlotAuthorizer implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(Display.Places.PERMISSIONS);
    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();

      resourcePermissionsPresenter.initialize(ResourcePermissionType.PROJECT,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_PROJECT, project.getName());
      setInSlot(Display.Places.PERMISSIONS, resourcePermissionsPresenter);
    }
  }

  /**
   * Update keystore on authorization.
   */
  private final class KeyStoreSlotAuthorizer implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(Display.Places.KEYSTORE);
    }

    @Override
    public void authorized() {
      ProjectKeyStorePresenter p = projectDataExchangeProvider.get();

      p.initialize(project);
      setInSlot(Display.Places.KEYSTORE, p);
    }
  }

  public interface Display extends View, HasUiHandlers<ProjectAdministrationUiHandlers> {

    enum Places {
      PERMISSIONS, KEYSTORE
    }

    void setProject(ProjectDto project);

    ProjectDto getProject();

    HasAuthorization getEditAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getKeyStoreAuthorizer();

    HasAuthorization getDeleteAuthorizer();
  }

}
