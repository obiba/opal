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
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceSelectionChangedEvent;
import org.obiba.opal.web.gwt.app.client.support.PluginsResource;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourcePluginPackageDto;

import java.util.List;
import java.util.Map;

public class ProjectResourcesPresenter extends PresenterWidget<ProjectResourcesPresenter.Display> {

  private final PlaceManager placeManager;

  private final ProjectResourceListPresenter projectResourceListPresenter;

  private final ProjectResourcePresenter projectResourcePresenter;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  @Inject
  public ProjectResourcesPresenter(Display display, EventBus eventBus, PlaceManager placeManager, ProjectResourceListPresenter projectResourceListPresenter, ProjectResourcePresenter projectResourcePresenter) {
    super(eventBus, display);
    this.placeManager = placeManager;
    this.projectResourceListPresenter = projectResourceListPresenter;
    this.projectResourcePresenter = projectResourcePresenter;
  }

  @Override
  protected void onReveal() {
    GWT.log("resources on reveal");
  }

  @Override
  protected void onBind() {
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
    addRegisteredHandler(ResourceSelectionChangedEvent.getType(), new ResourceSelectionChangedEvent.ResourceSelectionChangedHandler() {
      @Override
      public void onResourceSelectionChanged(ResourceSelectionChangedEvent event) {
        String selectedResourceName = event.getName();
        if (Strings.isNullOrEmpty(selectedResourceName)) {
          projectResourceListPresenter.initialize(resourceFactories);
          getView().renderResourceList(event.getProject());
        } else {
          projectResourcePresenter.initialize(resourceFactories);
          getView().renderResource(event.getProject(), event.getName());
        }
      }
    });
    setInSlot(Display.RESOURCES, projectResourceListPresenter);
    setInSlot(Display.RESOURCE, projectResourcePresenter);
  }

  public interface Display extends View {

    SingleSlot<ProjectResourceListPresenter> RESOURCES = new SingleSlot<ProjectResourceListPresenter>();
    SingleSlot<ProjectResourcePresenter> RESOURCE = new SingleSlot<ProjectResourcePresenter>();

    void renderResourceList(String projectName);

    void renderResource(String projectName, String resourceName);

  }
}
