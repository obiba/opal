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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceSelectionChangedEvent;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceProviderDto;
import org.obiba.opal.web.model.client.opal.ResourceProvidersDto;
import org.obiba.opal.web.model.client.opal.ResourceTagDto;

import java.util.List;
import java.util.Map;

public class ProjectResourcesPresenter extends PresenterWidget<ProjectResourcesPresenter.Display> {

  private final ProjectResourceListPresenter projectResourceListPresenter;

  private final ProjectResourcePresenter projectResourcePresenter;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  private List<ResourceTagDto> resourceTags = Lists.newArrayList();

  @Inject
  public ProjectResourcesPresenter(Display display, EventBus eventBus, ProjectResourceListPresenter projectResourceListPresenter, ProjectResourcePresenter projectResourcePresenter) {
    super(eventBus, display);
    this.projectResourceListPresenter = projectResourceListPresenter;
    this.projectResourcePresenter = projectResourcePresenter;
  }

  @Override
  protected void onBind() {
    ResourceProvidersResource.getInstance().getProviders(new ResourceProvidersResource.Handler() {
      @Override
      public void handle(ResourceProvidersDto providers) {
        for (ResourceProviderDto provider : JsArrays.toIterable(providers.getProvidersArray())) {
          for (ResourceFactoryDto factory : JsArrays.toIterable(provider.getResourceFactoriesArray())) {
            String key = ResourceProvidersResource.makeResourceFactoryKey(factory);
            resourceFactories.put(key, factory);
          }
        }
        resourceTags = JsArrays.toList(providers.getTagsArray());
      }
    });
    addRegisteredHandler(ResourceSelectionChangedEvent.getType(), new ResourceSelectionChangedEvent.ResourceSelectionChangedHandler() {
      @Override
      public void onResourceSelectionChanged(ResourceSelectionChangedEvent event) {
        String selectedResourceName = event.getName();
        if (Strings.isNullOrEmpty(selectedResourceName)) {
          projectResourceListPresenter.initialize(resourceFactories, resourceTags);
          getView().renderResourceList(event.getProject());
        } else {
          projectResourcePresenter.initialize(resourceFactories, resourceTags);
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
