/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.admin;

import static com.google.gwt.http.client.Response.SC_CONFLICT;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_FORBIDDEN;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

import com.google.common.collect.Maps;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import java.util.Map;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent.Handler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.place.Places;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.edit.EditProjectModalPresenter;
import org.obiba.opal.web.gwt.app.client.project.event.ProjectUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.project.identifiersmappings.ProjectIdentifiersMappingsPresenter;
import org.obiba.opal.web.gwt.app.client.project.keystore.ProjectKeyStorePresenter;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.PluginPackagesDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

public class ProjectAdministrationPresenter extends PresenterWidget<ProjectAdministrationPresenter.Display>
    implements ProjectAdministrationUiHandlers {

  public static final String VCF_STORE_SERVICE_TYPE = "vcf-store";

  private final PlaceManager placeManager;

  private final ModalProvider<EditProjectModalPresenter> editProjectModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final Provider<ProjectKeyStorePresenter> projectDataExchangeProvider;

  private final ProjectIdentifiersMappingsPresenter identifiersMappingsPresenter;

  private ProjectDto project;

  private Runnable removeConfirmation;

  private Runnable reloadConfirmation;

  private final TranslationMessages translationMessages;

  @Inject
  public ProjectAdministrationPresenter(EventBus eventBus, Display view, PlaceManager placeManager,
                                        ModalProvider<EditProjectModalPresenter> editProjectModalProvider,
                                        Provider<ResourcePermissionsPresenter> resourcePermissionsProvider,
                                        Provider<ProjectKeyStorePresenter> projectDataExchangeProvider,
                                        Provider<ProjectIdentifiersMappingsPresenter> identifiersMappingsPresenterProvider,
                                        TranslationMessages translationMessages) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.translationMessages = translationMessages;
    this.placeManager = placeManager;
    this.editProjectModalProvider = editProjectModalProvider.setContainer(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.projectDataExchangeProvider = projectDataExchangeProvider;
    this.identifiersMappingsPresenter = identifiersMappingsPresenterProvider.get();
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
    addRegisteredHandler(ConfirmationEvent.getType(), new Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (event.getSource().equals(reloadConfirmation) && event.isConfirmed()) {
          reloadConfirmation.run();
          reloadConfirmation = null;
        }
      }
    });
  }

  public void setProject(ProjectDto project) {
    this.project = project;
    getView().setProject(project);
    authorize();
    initProjectState();

    Map<String, String> params = Maps.newHashMap();
    params.put("type", VCF_STORE_SERVICE_TYPE);

    ResourceRequestBuilderFactory.<PluginPackagesDto>newBuilder()
      .forResource(UriBuilders.PLUGINS.create().query(params).build())
      .withCallback(new ResourceCallback<PluginPackagesDto>() {
        @Override
        public void onResource(Response response, PluginPackagesDto resource) {
          boolean hasPlugin = false;
          for (PluginPackageDto pluginPackage : JsArrays.toIterable(resource.getPackagesArray())) {
            if (ProjectAdministrationPresenter.VCF_STORE_SERVICE_TYPE.equals(pluginPackage.getType())) {
              hasPlugin = true;
              break;
            }
          }
          showVcfServiceNamePanel(hasPlugin);
        }
      }).get().send();
  }

  private void initProjectState() {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_STATE.create().build(project.getName()))
        .withCallback(SC_OK, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            String responseText = response.getText();
            getView().toggleReloadButton("READY".equals(responseText));
          }
        }).get().send();
  }

  private void authorize() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(project.getLink())
        .authorize(new CompositeAuthorizer(getView().getEditAuthorizer(), getView().getIdentifiersMappingsAuthorizer(), new ProjectIdentifiersMappingsSlotAuthorizer()))
        .put().send();

    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_PERMISSIONS_PROJECT.create().build(project.getName()))
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsSlotAuthorizer()))
        .post().send();

    // set keystore
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_KEYSTORE.create().build(project.getName()))
        .authorize(new CompositeAuthorizer(getView().getKeyStoreAuthorizer(), new KeyStoreSlotAuthorizer()))
        .post().send();

    // delete project
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(project.getLink())
        .authorize(getView().getDeleteAuthorizer())
        .delete().send();

    // reload database
    ResourceAuthorizationRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_RELOAD.create().build(project.getName()))
        .authorize(getView().getReloadAuthorizer())
        .post().send();

  }

  private void showVcfServiceNamePanel(boolean show) {
    getView().toggleVcfServicePluginPanel(show);
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

  @Override
  public void onReload() {
    reloadConfirmation = new Runnable() {
      @Override
      public void run() {
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(UriBuilders.PROJECT_COMMANDS_RELOAD.create().build(project.getName()))
            .withCallback(new ResponseCodeCallback() {
              @Override
              public void onResponseCode(Request request, Response response) {
                fireEvent(ConfirmationTerminatedEvent.create());
                if(response.getStatusCode() != SC_CREATED) {
                  String errorMessage = response.getText().isEmpty() ? response.getStatusCode() == SC_FORBIDDEN
                      ? "Forbidden"
                      : "ProjectMomentarilyNotReloadable" : response.getText();
                  fireEvent(NotificationEvent.newBuilder().error(errorMessage).args(project.getName()).build());
                } else {
                  initProjectState();
                }
              }
            }, SC_CREATED, SC_FORBIDDEN, SC_NOT_FOUND, SC_CONFLICT, SC_INTERNAL_SERVER_ERROR)
            .post().send();
      }
    };

    fireEvent(ConfirmationRequiredEvent.createWithMessages(reloadConfirmation, translationMessages.reloadProject(), translationMessages.confirmReloadProject()));
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
          fireEvent(ConfirmationTerminatedEvent.create());
          if(response.getStatusCode() == SC_OK) {
            PlaceRequest projectsRequest = new PlaceRequest.Builder().nameToken(Places.PROJECTS).build();
            placeManager.revealPlace(projectsRequest);
          } else {
            String errorMessage = response.getText().isEmpty() ? response.getStatusCode() == SC_FORBIDDEN
                ? "Forbidden"
                : "UnknownError" : response.getText();
            fireEvent(NotificationEvent.newBuilder().error(errorMessage).build());
          }
        }
      };

      UriBuilder uri = UriBuilders.PROJECT.create().query("archive", Boolean.toString(archive));
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(uri.build(projectDto.getName()))
          .withCallback(callbackHandler, SC_OK, SC_FORBIDDEN, SC_INTERNAL_SERVER_ERROR, SC_NOT_FOUND)
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

  /**
   * Update Project on authorization.
   */
  private final class ProjectIdentifiersMappingsSlotAuthorizer implements HasAuthorization {
    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(Display.Places.MAPPINGS);
    }

    @Override
    public void authorized() {
      identifiersMappingsPresenter.setProject(project);
      setInSlot(Display.Places.MAPPINGS, identifiersMappingsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<ProjectAdministrationUiHandlers> {

    enum Places {
      MAPPINGS, PERMISSIONS, KEYSTORE
    }

    void setProject(ProjectDto project);

    ProjectDto getProject();

    void toggleVcfServicePluginPanel(boolean show);

    HasAuthorization getEditAuthorizer();

    HasAuthorization getIdentifiersMappingsAuthorizer();

    HasAuthorization getPermissionsAuthorizer();

    HasAuthorization getKeyStoreAuthorizer();

    HasAuthorization getDeleteAuthorizer();

    HasAuthorization getReloadAuthorizer();

    void toggleReloadButton(boolean toggleOn);
  }

}
