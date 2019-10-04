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

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceCreatedEvent;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.support.PluginsResource;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.*;

import java.util.List;
import java.util.Map;

public class ProjectResourcesPresenter extends PresenterWidget<ProjectResourcesPresenter.Display>
    implements ProjectResourcesUiHandlers {

  private ProjectDto projectDto;

  private final ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  @Inject
  public ProjectResourcesPresenter(Display display, EventBus eventBus, ModalProvider<ProjectResourceModalPresenter> projectResourceModalProvider) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.projectResourceModalProvider = projectResourceModalProvider.setContainer(this);
  }

  public void initialize(ProjectDto dto) {
    projectDto = dto;
    refreshResources();
  }

  @Override
  protected void onBind() {
    resourceFactories.clear();
    ResourcePluginsResource.getInstance().getPlugins(new PluginsResource.Handler() {
      @Override
      public void handle(List<PluginPackageDto> plugins) {
        for (PluginPackageDto plugin : plugins) {
          ResourcePluginPackageDto resourcePlugin = plugin.getExtension(ResourcePluginPackageDto.PluginPackageDtoExtensions.resource).cast();
          for (ResourceFactoryDto factory : JsArrays.toIterable(resourcePlugin.getResourceFactoriesArray())) {
            String key = ResourcePluginsResource.makeResourceFactoryKey(plugin, factory);
            resourceFactories.put(key, factory);
          }
        }
      }
    });
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
  }

  @Override
  public void onRefresh() {
    refreshResources();
  }

  @Override
  public void onAddResource() {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectDto, resourceFactories, null);
  }

  @Override
  public void onEditResource(ResourceReferenceDto resource) {
    ProjectResourceModalPresenter modal = projectResourceModalProvider.get();
    modal.initialize(projectDto, resourceFactories, resource);
  }

  @Override
  public void onRemoveResource(ResourceReferenceDto resource) {
    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_RESOURCE.create().build(projectDto.getName(), resource.getName()))
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
        .forResource(UriBuilders.PROJECT_RESOURCES.create().build(projectDto.getName())) //
        .withCallback(new ResourceCallback<JsArray<ResourceReferenceDto>>() {
          @Override
          public void onResource(Response response, JsArray<ResourceReferenceDto> resources) {
            getView().renderResources(JsArrays.toList(resources));
          }
        }) //
        .get().send();
  }

  public interface Display extends View, HasUiHandlers<ProjectResourcesUiHandlers> {

    void renderResources(List<ResourceReferenceDto> resources);

  }
}
