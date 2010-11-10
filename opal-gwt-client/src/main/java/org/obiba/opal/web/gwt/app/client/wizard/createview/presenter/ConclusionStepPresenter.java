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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.navigator.event.ViewConfigurationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.view.ResourceRequestView;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.magma.ViewDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ConclusionStepPresenter extends WidgetPresenter<ConclusionStepPresenter.Display> {
  //
  // Instance Variables
  //

  private ResourceRequestPresenter<? extends JavaScriptObject> resourceRequestPresenter;

  private String datasourceName;

  private String viewName;

  //
  // Constructors
  //

  @Inject
  public ConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  //
  // WidgetPresenter Methods
  //

  @Override
  protected void onBind() {
    addEventHandlers();
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

  public <T extends JavaScriptObject> void setResourceRequest(String datasourceName, String viewName, String resourceLink, ResourceRequestBuilder<T> requestBuilder) {
    this.datasourceName = datasourceName;
    this.viewName = viewName;

    resourceRequestPresenter = new ResourceRequestPresenter<T>(new ResourceRequestView(), eventBus, requestBuilder, new ConclusionResponseCodeCallback());
    resourceRequestPresenter.getDisplay().setResourceName(viewName);
    resourceRequestPresenter.setSuccessCodes(Response.SC_OK, Response.SC_CREATED);
    resourceRequestPresenter.setErrorCodes(Response.SC_BAD_REQUEST, Response.SC_NOT_FOUND, Response.SC_METHOD_NOT_ALLOWED, Response.SC_INTERNAL_SERVER_ERROR);

    getDisplay().setResourceRequest(resourceRequestPresenter.getDisplay());
  }

  public void sendResourceRequest() {
    if(resourceRequestPresenter != null) {
      resourceRequestPresenter.sendRequest();
    }
  }

  public void showConfigureViewWidgets(boolean show) {
    getDisplay().showConfigureViewWidgets(show);
  }

  private void addEventHandlers() {
    super.registerHandler(getDisplay().addConfigureViewClickHandler(new ConfigureViewClickHandler()));
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setResourceRequest(ResourceRequestPresenter.Display resourceRequestDisplay);

    void showConfigureViewWidgets(boolean show);

    HandlerRegistration addConfigureViewClickHandler(ClickHandler handler);
  }

  class ConclusionResponseCodeCallback implements ResponseCodeCallback {

    public void onResponseCode(Request request, Response response) {
      int statusCode = response.getStatusCode();
      if(statusCode == Response.SC_OK || statusCode == Response.SC_CREATED) {
        getDisplay().showConfigureViewWidgets(true);
      }
    }
  }

  class ConfigureViewClickHandler implements ClickHandler {

    public void onClick(ClickEvent event) {
      ResourceRequestBuilderFactory.<ViewDto> newBuilder().forResource("/datasource/" + datasourceName + "/view/" + viewName).get().withCallback(new ResourceCallback<ViewDto>() {

        @Override
        public void onResource(Response response, ViewDto viewDto) {
          viewDto.setDatasourceName(datasourceName);
          viewDto.setName(viewName);

          eventBus.fireEvent(new ViewConfigurationRequiredEvent(viewDto));
        }
      }).send();
    }
  }
}
