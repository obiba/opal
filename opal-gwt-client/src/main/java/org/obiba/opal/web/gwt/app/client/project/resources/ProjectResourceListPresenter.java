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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationTerminatedEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceSelectionChangedEvent;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceUpdatedEvent;
import org.obiba.opal.web.gwt.rest.client.*;
import org.obiba.opal.web.gwt.rest.client.authorization.Authorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.CompositeAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.List;

public class ProjectResourceListPresenter extends PresenterWidget<ProjectResourceListPresenter.Display>
    implements ProjectResourceListUiHandlers {

  private String projectName;

  private final TranslationMessages translationMessages;

  private final ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private final ResourceProvidersService resourceProvidersService;

  private List<ResourceReferenceDto> resources;

  private Runnable removeConfirmation;

  @Inject
  public ProjectResourceListPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages, ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider, ResourceProvidersService resourceProvidersService) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    this.resourceProvidersService = resourceProvidersService;
    getView().setUiHandlers(this);
    this.resourcePermissionsProvider = resourcePermissionsProvider;
    this.projectResourceModalProvider = projectResourceModalProvider.setContainer(this);
  }

  @Override
  protected void onBind() {
    addRegisteredHandler(ResourceCreatedEvent.getType(), new ResourceCreatedEvent.ResourceCreatedHandler() {
      @Override
      public void onResourceCreated(ResourceCreatedEvent event) {
        refreshResources();
      }
    });
    addRegisteredHandler(ResourceUpdatedEvent.getType(), new ResourceUpdatedEvent.ResourceUpdatedHandler() {
      @Override
      public void onResourceUpdated(ResourceUpdatedEvent event) {
        refreshResources();
      }
    });
    addRegisteredHandler(ResourceSelectionChangedEvent.getType(), new ResourceSelectionChangedEvent.ResourceSelectionChangedHandler() {
      @Override
      public void onResourceSelectionChanged(ResourceSelectionChangedEvent event) {
        if (Strings.isNullOrEmpty(event.getName())) {
          if (!event.getProject().equals(projectName)) {
            projectName = event.getProject();
            authorizeAndRefreshResources();
          } else {
            refreshResources();
          }
        }
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
    resourceProvidersService.initialize();
  }

  @Override
  public void onRefresh() {
    refreshResources();
  }

  @Override
  public void onAddResource() {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectName, null, false);
  }

  @Override
  public void onEditResource(ResourceReferenceDto resource) {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectName, resource, false);
  }

  @Override
  public void onRemoveResource(ResourceReferenceDto resource) {
    if (!resource.getEditable()) return;
    removeConfirmation = new RemoveRunnable(Lists.newArrayList(resource));
    fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeResource(),
        translationMessages.confirmRemoveResource(resource.getName())));
  }

  @Override
  public void onRemoveResources(List<ResourceReferenceDto> selectedItems) {
    removeConfirmation = new RemoveRunnable(selectedItems);
    List<String> names = Lists.newArrayList();
    for (ResourceReferenceDto res : selectedItems) {
      if (res.getEditable())
        names.add(res.getName());
    }
    if (names.isEmpty()) return;
    fireEvent(ConfirmationRequiredEvent.createWithMessages(removeConfirmation, translationMessages.removeResources(),
        translationMessages.confirmRemoveResources(Joiner.on(", ").join(names))));
  }

  private void refreshResources() {
    // Fetch all providers
    ResourceRequestBuilderFactory.<JsArray<ResourceReferenceDto>>newBuilder() //
        .forResource(UriBuilders.PROJECT_RESOURCES.create().build(projectName)) //
        .withCallback(new ResourceCallback<JsArray<ResourceReferenceDto>>() {
          @Override
          public void onResource(Response response, JsArray<ResourceReferenceDto> resourceReferences) {
            resources = JsArrays.toList(resourceReferences);
            getView().renderResources(resources);
          }
        }) //
        .get().send();
  }

  private void authorizeAndRefreshResources() {
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(UriBuilders.PROJECT_RESOURCES.create().build(projectName)) //
        .authorize(new CompositeAuthorizer(getView().getAddResourceAuthorizer(), new Authorizer(getEventBus()) {
          @Override
          public void authorized() {
            refreshResources();
          }

          @Override
          public void unauthorized() {
            refreshResources();
          }
        })) //
        .post().send();
    // set permissions
    ResourceAuthorizationRequestBuilderFactory.newBuilder() //
        .forResource(ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_RESOURCES.create().build(projectName)) //
        .authorize(new CompositeAuthorizer(getView().getPermissionsAuthorizer(), new PermissionsUpdate())) //
        .post().send();
  }

  /**
   * Update permissions on authorization.
   */
  private final class PermissionsUpdate implements HasAuthorization {

    @Override
    public void unauthorized() {

    }

    @Override
    public void beforeAuthorization() {
      clearSlot(Display.RESOURCES_PERMISSIONS);
    }

    @Override
    public void authorized() {
      ResourcePermissionsPresenter resourcePermissionsPresenter = resourcePermissionsProvider.get();
      resourcePermissionsPresenter.initialize(ResourcePermissionType.RESOURCES,
          ResourcePermissionRequestPaths.UriBuilders.PROJECT_PERMISSIONS_RESOURCES, projectName);
      setInSlot(Display.RESOURCES_PERMISSIONS, resourcePermissionsPresenter);
    }
  }

  public interface Display extends View, HasUiHandlers<ProjectResourceListUiHandlers> {

    SingleSlot<ResourcePermissionsPresenter> RESOURCES_PERMISSIONS = new SingleSlot<ResourcePermissionsPresenter>();

    void renderResources(List<ResourceReferenceDto> resources);

    HasAuthorization getAddResourceAuthorizer();

    HasAuthorization getPermissionsAuthorizer();
  }

  private class RemoveRunnable implements Runnable {

    private final List<ResourceReferenceDto> resources;

    public RemoveRunnable(List<ResourceReferenceDto> resources) {
      this.resources = resources;
    }

    private UriBuilder getUriBuilder() {
      UriBuilder builder = UriBuilders.PROJECT_RESOURCES.create();
      for (ResourceReferenceDto res : resources) {
        builder.query("names", res.getName());
      }
      return builder;
    }

    @Override
    public void run() {
      ResourceRequestBuilderFactory.newBuilder()
          .forResource(getUriBuilder().build(projectName))
          .withCallback(new ResponseCodeCallback() {
            @Override
            public void onResponseCode(Request request, Response response) {
              fireEvent(ConfirmationTerminatedEvent.create());
              refreshResources();
            }
          }, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN)
          .delete().send();
    }
  }
}
