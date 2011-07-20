/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.navigator.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter.ResourceClickHandler;
import org.obiba.opal.web.gwt.app.client.widgets.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ConclusionStepPresenter extends WidgetPresenter<ConclusionStepPresenter.Display> {
  //
  // Instance Variables
  //

  /**
   * Resource requests.
   */
  private Set<ResourceRequestPresenter<? extends JavaScriptObject>> resourceRequests;

  /**
   * Number of resource requests completed (successfully or with an error).
   */
  private int resourceRequestsCompleted;

  private String targetDatasourceName;

  //
  // Constructors
  //

  @Inject
  public ConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);

    resourceRequests = new LinkedHashSet<ResourceRequestPresenter<? extends JavaScriptObject>>();
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  //
  // Methods
  //

  public void clearResourceRequests() {
    resourceRequests.clear();
    resourceRequestsCompleted = 0;

    getDisplay().clearResourceRequests();
  }

  public <T extends JavaScriptObject> void addResourceRequest(String resourceName, String resourceLink, ResourceRequestBuilder<T> requestBuilder) {
    ResourceRequestPresenter<T> resourceRequestPresenter = new ResourceRequestPresenter<T>(new ResourceRequestView(), eventBus, requestBuilder, new ImportVariablesResponseCodeCallback());
    resourceRequestPresenter.getDisplay().setResourceName(resourceName);
    resourceRequestPresenter.getDisplay().setResourceClickHandler(new TableResourceClickHandler(resourceLink));
    resourceRequestPresenter.setSuccessCodes(200, 201);
    resourceRequestPresenter.setErrorCodes(400, 404, 405, 500);

    resourceRequests.add(resourceRequestPresenter);
    getDisplay().addResourceRequest(resourceRequestPresenter.getDisplay());
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

  public interface Display extends WidgetDisplay {

    void clearResourceRequests();

    void addResourceRequest(ResourceRequestPresenter.Display resourceRequestDisplay);
  }

  class ImportVariablesResponseCodeCallback implements ResponseCodeCallback {

    public void onResponseCode(Request request, Response response) {
      resourceRequestsCompleted++;

      if(resourceRequestsCompleted == resourceRequests.size()) {
        // TODO enable finish getDisplay().setReturnButtonEnabled(true);
        ;

        // OPAL-927: Refresh target datasource.
        refreshTargetDatasource();
      }
    }

    private void refreshTargetDatasource() {
      final ResourceCallback<DatasourceDto> resourceCallback = new ResourceCallback<DatasourceDto>() {

        @Override
        public void onResource(Response response, DatasourceDto resource) {
          eventBus.fireEvent(new DatasourceUpdatedEvent(resource));
        }
      };

      ResourceRequestBuilderFactory.<DatasourceDto> newBuilder().get().forResource("/datasource/" + targetDatasourceName).withCallback(resourceCallback).send();
    }
  }

  static class TableResourceClickHandler implements ResourceClickHandler {

    private String resourceLink;

    public TableResourceClickHandler(String resourceLink) {
      this.resourceLink = resourceLink;
    }

    public String getResourceLink() {
      return resourceLink;
    }

    public void onClick(ClickEvent event) {
      fireTableSelectionChangeEvent(getResourceLink());
    }

    private void fireTableSelectionChangeEvent(final String tableName) {
      ResourceRequestBuilderFactory.<TableDto> newBuilder().forResource(getResourceLink()).get().withCallback(new ResourceCallback<TableDto>() {

        @Override
        public void onResource(Response response, TableDto resource) {
          // TODO: Fire an event to trigger display of the selected table.
        }
      }).send();
    }
  }
}
