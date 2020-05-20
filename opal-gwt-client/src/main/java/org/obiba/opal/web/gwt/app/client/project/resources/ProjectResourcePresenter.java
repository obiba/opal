/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.resources;

import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceSelectionChangedEvent;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import javax.inject.Inject;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;

public class ProjectResourcePresenter extends PresenterWidget<ProjectResourcePresenter.Display>
    implements ProjectResourceUiHandlers {

  private final PlaceManager placeManager;

  private final TranslationMessages translationMessages;

  private final Translations translations;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider;

  private String projectName;

  private ResourceReferenceDto resource;

  private Runnable removeConfirmation;

  @Inject
  public ProjectResourcePresenter(EventBus eventBus,
                                  Display view,
                                  PlaceManager placeManager, TranslationMessages translationMessages, Translations translations, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider) {
    super(eventBus, view);
    this.placeManager = placeManager;
    this.translationMessages = translationMessages;
    this.translations = translations;
    this.projectResourceModalProvider = projectResourceModalProvider.setContainer(this);
    getView().setUiHandlers(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ResourceSelectionChangedEvent.getType(), new ResourceSelectionChangedEvent.ResourceSelectionChangedHandler() {
      @Override
      public void onResourceSelectionChanged(ResourceSelectionChangedEvent event) {
        if (!Strings.isNullOrEmpty(event.getName())) {
          projectName = event.getProject();
          refreshResource(event.getName());
        }
      }
    });
    addRegisteredHandler(ResourceUpdatedEvent.getType(), new ResourceUpdatedEvent.ResourceUpdatedHandler() {
      @Override
      public void onResourceUpdated(ResourceUpdatedEvent event) {
        refreshResource(resource.getName());
      }
    });
    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEvent.Handler() {
      @Override
      public void onConfirmation(ConfirmationEvent event) {
        if (removeConfirmation != null && event.getSource().equals(removeConfirmation) &&
            event.isConfirmed()) {
          removeConfirmation.run();
          removeConfirmation = null;
        }
      }
    });
  }

  @Override
  public void onEdit() {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectName, resource, false);
  }

  @Override
  public void onDuplicate() {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    ResourceReferenceDto resourceCopy = ResourceReferenceDto.parse(ResourceReferenceDto.stringify(resource));
    resourceCopy.setName(null);
    modal.setResourceCreatedHandler(new ResourceCreatedEvent.ResourceCreatedHandler() {
      @Override
      public void onResourceCreated(ResourceCreatedEvent event) {
        placeManager.revealPlace(ProjectPlacesHelper.getResourcePlace(projectName, event.getName()));
      }
    });
    modal.initialize(projectName, resourceCopy, false);
  }

  @Override
  public void onDelete() {
    removeConfirmation = new RemoveRunnable();
    fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeResource(),
        translationMessages.confirmRemoveResource(resource.getName())));
  }

  @Override
  public void onTest() {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_RESOURCE_TEST.create().build(projectName, resource.getName()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            if (response.getStatusCode() == Response.SC_OK)
              fireEvent(NotificationEvent.newBuilder().success(translations.userMessageMap().get("ResourceAssignSuccess")).build());
            else
              fireEvent(NotificationEvent.newBuilder().error(translations.userMessageMap().get("ResourceAssignFailed") + response.getStatusText()).build());
            getView().testCompleted();
          }
        }, Response.SC_OK, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN, Response.SC_BAD_REQUEST)
        .put().send();
  }

  private void refreshResource(String name) {
    // Fetch all providers
    ResourceRequestBuilderFactory.<ResourceReferenceDto>newBuilder() //
        .forResource(UriBuilders.PROJECT_RESOURCE.create().build(projectName, name))
        .withCallback(new ResourceCallback<ResourceReferenceDto>() {
          @Override
          public void onResource(Response response, ResourceReferenceDto resourceReference) {
            resource = resourceReference;

            if (resource.getEditable()) {
              ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
              resourcePermissionsPresenter.initialize(ResourcePermissionType.RESOURCE,
                  ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_RESOURCE, projectName, resource.getName());
              setInSlot(Display.RESOURCE_PERMISSIONS, resourcePermissionsPresenter);
            }

            getView().renderResource(resource);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            placeManager.revealPlace(ProjectPlacesHelper.getResourcesPlace(projectName));
          }
        }, SC_FORBIDDEN)
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<ProjectResourceUiHandlers> {

    SingleSlot<ResourcePermissionsPresenter> RESOURCE_PERMISSIONS = new SingleSlot<ResourcePermissionsPresenter>();

    void renderResource(ResourceReferenceDto resource);

    void testCompleted();
  }

  private class RemoveRunnable implements Runnable {

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(UriBuilders.PROJECT_RESOURCE.create().build(projectName, resource.getName()))
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              placeManager.revealPlace(ProjectPlacesHelper.getResourcesPlace(projectName));
            }
          }, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN)
          .delete().send();
    }
  }
}
