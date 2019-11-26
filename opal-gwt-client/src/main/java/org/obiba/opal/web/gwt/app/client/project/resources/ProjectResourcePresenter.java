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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionRequestPaths;
import org.obiba.opal.web.gwt.app.client.permissions.support.ResourcePermissionType;
import org.obiba.opal.web.gwt.app.client.project.resources.event.ResourceSelectionChangedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.ResourceFactoryDto;
import org.obiba.opal.web.model.client.opal.ResourceReferenceDto;

import javax.inject.Inject;
import java.util.Map;

import static com.google.gwt.http.client.Response.SC_FORBIDDEN;

public class ProjectResourcePresenter extends PresenterWidget<ProjectResourcePresenter.Display>
    implements ProjectResourceUiHandlers {

  private final Provider<ResourcePermissionsPresenter> resourcePermissionsProvider;

  private String projectName;

  private Map<String, ResourceFactoryDto> resourceFactories = Maps.newHashMap();

  private ResourceReferenceDto resource;

  @Inject
  public ProjectResourcePresenter(EventBus eventBus,
                                  Display view,
                                  Provider<ResourcePermissionsPresenter> resourcePermissionsProvider) {
    super(eventBus, view);
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
  }

  @Override
  public void onEdit() {

  }

  @Override
  public void onDelete() {

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

            getView().renderResource(resourceFactories, resource);
          }
        })
        .withCallback(new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            fireEvent(new ResourceSelectionChangedEvent(projectName, null));
          }
        }, SC_FORBIDDEN)
        .get().send();
  }

  public void initialize(Map<String, ResourceFactoryDto> resourceFactories) {
    this.resourceFactories = resourceFactories;
  }

  public interface Display extends View, HasUiHandlers<ProjectResourceUiHandlers> {

    SingleSlot<ResourcePermissionsPresenter> RESOURCE_PERMISSIONS = new SingleSlot<ResourcePermissionsPresenter>();

    void renderResource(Map<String, ResourceFactoryDto> resourceFactories, ResourceReferenceDto resource);

  }
}
