/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.presenter;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 *
 */
public class ResourceRequestPresenter<T extends JavaScriptObject>
    extends PresenterWidget<ResourceRequestPresenter.Display> {
  //
  // Instance Variables
  //

  private final ResourceRequestBuilder<T> requestBuilder;

  private final ResponseCodeCallback callback;

  //
  // Constructors
  //

  @Inject
  public ResourceRequestPresenter(Display display, EventBus eventBus, ResourceRequestBuilder<T> requestBuilder,
      ResponseCodeCallback callback) {
    super(eventBus, display);

    this.requestBuilder = requestBuilder;
    this.callback = callback;
  }

  //
  // Methods
  //

  public void setSuccessCodes(Integer... codes) {
    ResponseCodeCallback internalCallback = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        getView().completed();

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
        getView().failed();

        if(response.getText() != null && !response.getText().isEmpty()) {
          try {
            ClientErrorDto errorDto = JsonUtils.unsafeEval(response.getText());
            getView().showErrorMessage(errorDto.getStatus());
          } catch(Exception e) {
            // Should never get here!
            getView().showErrorMessage("Internal Error");
          }
        } else {
          getView().showErrorMessage("Unknown Error");
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
    getView().inProgress();
    requestBuilder.send();
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Display extends View {

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
