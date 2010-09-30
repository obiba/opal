/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.createview.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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

  protected void addEventHandlers() {
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

  public void clearResourceRequest() {
    resourceRequests.clear();
    resourceRequestsCompleted = 0;

    getDisplay().clearResourceRequest();
  }

  public <T extends JavaScriptObject> void addResourceRequest(String resourceName, String resourceLink, ResourceRequestBuilder<T> requestBuilder) {
    ResourceRequestPresenter<T> resourceRequestPresenter = new ResourceRequestPresenter<T>(new ResourceRequestView(), eventBus, requestBuilder, new ConclusionResponseCodeCallback());
    resourceRequestPresenter.getDisplay().setResourceName(resourceName);
    resourceRequestPresenter.setSuccessCodes(200, 201);
    resourceRequestPresenter.setErrorCodes(400, 404, 405, 500);

    resourceRequests.add(resourceRequestPresenter);
    getDisplay().addResourceRequest(resourceRequestPresenter.getDisplay());
  }

  public int getResourceRequestCount() {
    return resourceRequests.size();
  }

  public void sendResourceRequest() {
    for(ResourceRequestPresenter<? extends JavaScriptObject> r : resourceRequests) {
      r.sendRequest();
    }
  }

  public void setConfigureViewButtonEnabled(boolean enabled) {
    getDisplay().setConfigureViewButtonEnabled(enabled);
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void clearResourceRequest();

    void addResourceRequest(ResourceRequestPresenter.Display resourceRequestDisplay);

    void setConfigureViewButtonEnabled(boolean enabled);

    HandlerRegistration addConfigureViewClickHandler(ClickHandler handler);
  }

  class ConclusionResponseCodeCallback implements ResponseCodeCallback {

    public void onResponseCode(Request request, Response response) {
      resourceRequestsCompleted++;

      if(resourceRequestsCompleted == resourceRequests.size()) {
        getDisplay().setConfigureViewButtonEnabled(true);
      }
    }
  }
}
