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
import com.google.common.collect.Maps;
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
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import java.util.List;
import java.util.Map;

public class ProjectResourceListPresenter extends PresenterWidget<ProjectResourceListPresenter.Display>
    implements ProjectResourceListUiHandlers {

  private String projectName;

  private final ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider;

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  private List<ResourceReferenceDto> resources;

  @Inject
  public ProjectResourceListPresenter(Display display, EventBus eventBus, ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider, Provider<ResourcePermissionsPresenter> resourcePermissionsProvider) {
    super(eventBus, display);
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
  }

  @Override
  public void onRefresh() {
    refreshResources();
  }

  @Override
  public void onAddResource() {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectName, resourceFactories, null, false);
  }

  @Override
  public void onEditResource(ResourceReferenceDto resource) {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectName, resourceFactories, resource, false);
  }

  @Override
  public void onRemoveResource(ResourceReferenceDto resource) {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_RESOURCE.create().build(projectName, resource.getName()))
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            // TODO display error message (if any)
            refreshResources();
          }
        }, Response.SC_NO_CONTENT, Response.SC_INTERNAL_SERVER_ERROR, Response.SC_FORBIDDEN)
        .delete().send();
  }

  private void refreshResources() {
    // Fetch all providers
    ResourceRequestBuilderFactory.<JsArray<ResourceReferenceDto>>newBuilder() //
        .forResource(UriBuilders.PROJECT_RESOURCES.create().build(projectName)) //
        .withCallback(new ResourceCallback<JsArray<ResourceReferenceDto>>() {
          @Override
          public void onResource(Response response, JsArray<ResourceReferenceDto> resourceReferences) {
            resources = JsArrays.toList(resourceReferences);
            getView().renderResources(resources, resourceFactories);
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

  public void initialize(Map<String, ResourceFactoryDto> resourceFactories) {
    this.resourceFactories = resourceFactories;
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

    void renderResources(List<ResourceReferenceDto> resources, Map<String, ResourceFactoryDto> resourceFactories);

    HasAuthorization getAddResourceAuthorizer();

    HasAuthorization getPermissionsAuthorizer();
  }
}
