/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 *
 */
public class ResourceRequestPresenter<T extends JavaScriptObject>
    extends WidgetPresenter<ResourceRequestPresenter.Display> {
  //
  // Instance Variables
  //

  private final ResourceRequestBuilder<T> requestBuilder;

  private final ResponseCodeCallback callback;

  //
  // Constructors
  //

  public ResourceRequestPresenter(Display display, EventBus eventBus, ResourceRequestBuilder<T> requestBuilder,
      ResponseCodeCallback callback) {
    super(display, eventBus);

    this.requestBuilder = requestBuilder;
    this.callback = callback;
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

  public void setSuccessCodes(Integer... codes) {
    ResponseCodeCallback internalCallback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getDisplay().completed();

        if(callback != null) {
          callback.onResponseCode(request, response);
        }
      }
    };

    if(codes != null) {
      for(int code : codes) {
        requestBuilder.withCallback(code, internalCallback);
      }
    }
  }

  public void setErrorCodes(Integer... codes) {
    ResponseCodeCallback internalCallback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getDisplay().failed();

        if(response.getText() != null && response.getText().length() != 0) {
          try {
            ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
            getDisplay().showErrorMessage(errorDto.getStatus());
          } catch(Exception e) {
            // Should never get here!
            getDisplay().showErrorMessage("Internal Error");
          }
        } else {
          getDisplay().showErrorMessage("Unknown Error");
        }

        if(callback != null) {
          callback.onResponseCode(request, response);
        }
      }
    };

    if(codes != null) {
      for(int code : codes) {
        requestBuilder.withCallback(code, internalCallback);
      }
    }
  }

  public void sendRequest() {
    getDisplay().inProgress();
    requestBuilder.send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends WidgetDisplay {

    void setResourceName(String resourceName);

    void showErrorMessage(String status);

    HandlerRegistration setResourceClickHandler(ResourceClickHandler handler);

    void inProgress();

    void completed();

    void failed();
  }

  public interface ResourceClickHandler extends ClickHandler {

    String getResourceLink();
  }
}
