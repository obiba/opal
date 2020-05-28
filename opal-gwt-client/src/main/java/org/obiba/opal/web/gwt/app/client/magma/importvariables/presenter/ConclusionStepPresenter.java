/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importvariables.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.magma.event.VariableRefreshEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ResourceRequestPresenter.ResourceClickHandler;
import org.obiba.opal.web.gwt.app.client.project.ProjectPlacesHelper;
import org.obiba.opal.web.gwt.app.client.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class ConclusionStepPresenter extends PresenterWidget<ConclusionStepPresenter.Display> {
  //
  // Instance Variables
  //

  /**
   * Resource requests.
   */
  private final Set<ResourceRequestPresenter<? extends JavaScriptObject>> resourceRequests;

  /**
   * Number of resource requests completed (successfully or with an error).
   */
  private int resourceRequestsCompleted;

  private String targetDatasourceName;

  private final PlaceManager placeManager;

  //
  // Constructors
  //

  @Inject
  public ConclusionStepPresenter(EventBus eventBus, Display display, PlaceManager placeManager) {
    super(eventBus, display);
    this.placeManager = placeManager;

    resourceRequests = new LinkedHashSet<ResourceRequestPresenter<? extends JavaScriptObject>>();
  }

  //
  // Methods
  //

  public void clearResourceRequests() {
    resourceRequests.clear();
    resourceRequestsCompleted = 0;

    getView().clearResourceRequests();
  }

  public <T extends JavaScriptObject> void addResourceRequest(String resourceName, String resourceLink,
      ResourceRequestBuilder<T> requestBuilder) {
    ResourceRequestPresenter<T> resourceRequestPresenter = new ResourceRequestPresenter<T>(new ResourceRequestView(),
        getEventBus(), requestBuilder, new ImportVariablesResponseCodeCallback());
    resourceRequestPresenter.getView().setResourceName(resourceName);
    resourceRequestPresenter.getView().setResourceClickHandler(new TableResourceClickHandler(resourceLink));
    resourceRequestPresenter.setSuccessCodes(200, 201);
    resourceRequestPresenter.setErrorCodes(400, 404, 405, 500);

    resourceRequests.add(resourceRequestPresenter);
    getView().addResourceRequest(resourceRequestPresenter.getView());
  }

  public int getResourceRequestCount() {
    return resourceRequests.size();
  }

  public void sendResourceRequests() {
    for(ResourceRequestPresenter<? extends JavaScriptObject> r : resourceRequests) {
      r.sendRequest();
    }
  }

  public void setTargetDatasourceName(String targetDatasourceName) {
    this.targetDatasourceName = targetDatasourceName;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

    void clearResourceRequests();

    void addResourceRequest(ResourceRequestPresenter.Display resourceRequestDisplay);
  }

  class ImportVariablesResponseCodeCallback implements ResponseCodeCallback {

    @Override
    public void onResponseCode(Request request, Response response) {
      resourceRequestsCompleted++;

      if(resourceRequestsCompleted == resourceRequests.size()) {
        // TODO enable finish getView().setReturnButtonEnabled(true);
        // OPAL-927: Refresh target datasource.
        refreshTargetDatasource();
      }
    }

    private void refreshTargetDatasource() {
      ResourceCallback<DatasourceDto> resourceCallback = new ResourceCallback<DatasourceDto>() {

        @Override
        public void onResource(Response response, DatasourceDto resource) {
          placeManager.revealPlace(ProjectPlacesHelper.getDatasourcePlace(resource.getName()));
          fireEvent(new VariableRefreshEvent());
        }
      };
      UriBuilder ub = UriBuilder.create().segment("datasource", targetDatasourceName);
      ResourceRequestBuilderFactory.<DatasourceDto>newBuilder().get().forResource(ub.build())
          .withCallback(resourceCallback).send();
    }
  }

  static class TableResourceClickHandler implements ResourceClickHandler {

    private final String resourceLink;

    TableResourceClickHandler(String resourceLink) {
      this.resourceLink = resourceLink;
    }

    @Override
    public String getResourceLink() {
      return resourceLink;
    }

    @Override
    public void onClick(ClickEvent event) {
      fireTableSelectionChangeEvent();
    }

    private void fireTableSelectionChangeEvent() {
      ResourceRequestBuilderFactory.<TableDto>newBuilder().forResource(getResourceLink()).get()
          .withCallback(new ResourceCallback<TableDto>() {

            @Override
            public void onResource(Response response, TableDto resource) {
              // TODO: Fire an event to trigger display of the selected table.
            }
          }).send();
    }
  }
}
