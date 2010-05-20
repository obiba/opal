/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class ResourceRequestBuilder<T extends JavaScriptObject> {

  private final static String OPAL_WS_ROOT = GWT.isScript() ? GWT.getModuleBaseURL() + "ws" : "http://localhost:8080/ws";

  private final EventBus eventBus;

  private String uri;

  private String body;

  private String acceptHeader;

  private ResourceCallback<T> resourceCallback;

  // An array of handlers for HTTP status codes: the index of the array is the HTTP code. This will be 99% empty. This
  // may not be appropriate, depends on how the browser handles this...
  // TODO: determine the implications fo this
  private ResponseCodeCallback[] codes;

  private RequestBuilder.Method method;

  private RequestBuilder builder;

  @Inject
  public ResourceRequestBuilder(EventBus eventBus) {
    if(eventBus == null) throw new IllegalArgumentException("eventBus cannot be null");
    this.eventBus = eventBus;
  }

  public ResourceRequestBuilder<T> forResource(String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.uri = resource.startsWith("http") ? resource : OPAL_WS_ROOT + resource;
    return this;
  }

  public ResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback) {
    this.resourceCallback = callback;
    return this;
  }

  public ResourceRequestBuilder<T> withCallback(int code, ResponseCodeCallback callback) {
    if(codes == null) {
      codes = new ResponseCodeCallback[505];
    }
    codes[code] = callback;
    return this;
  }

  public ResourceRequestBuilder<T> accept(String acceptHeader) {
    this.acceptHeader = acceptHeader;
    return this;
  }

  public ResourceRequestBuilder<T> withBody(String body) {
    this.body = body;
    return this;
  }

  public ResourceRequestBuilder<T> get() {
    method = RequestBuilder.GET;
    return this;
  }

  public ResourceRequestBuilder<T> head() {
    method = RequestBuilder.HEAD;
    return this;
  }

  public ResourceRequestBuilder<T> post() {
    method = RequestBuilder.POST;
    return this;
  }

  public ResourceRequestBuilder<T> put() {
    method = RequestBuilder.PUT;
    return this;
  }

  public ResourceRequestBuilder<T> delete() {
    method = RequestBuilder.DELETE;
    return this;
  }

  public RequestBuilder build() {
    builder = new RequestBuilder(method, uri);
    builder.setCallback(new InnerCallback());
    if(resourceCallback != null && acceptHeader == null) {
      builder.setHeader("Accept", "application/x-protobuf+json");
    }
    if(acceptHeader != null) builder.setHeader("Accept", acceptHeader);
    if(body != null) builder.setRequestData(body);
    return builder;
  }

  public void send() {
    try {
      build().send();
    } catch(RequestException e) {
      eventBus.fireEvent(new RequestErrorEvent(e));
    }
  }

  private class InnerCallback implements RequestCallback {

    @Override
    public void onError(Request request, Throwable exception) {
      eventBus.fireEvent(new RequestErrorEvent(exception));
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
      int code = response.getStatusCode();

      // Restore the 403 code when the request is 'forbidden'. This ensures the correct handler is called.
      if(code == 0 && response.getStatusText().equalsIgnoreCase("Forbidden")) code = 403;

      if(codes != null && codes[code] != null) {
        codes[code].onResponseCode(request, response);
      } else {
        if(resourceCallback != null && code < 400) {
          final T resource = (T) JsonUtils.unsafeEval(response.getText());
          resourceCallback.onResource(response, resource);
        } else {
          eventBus.fireEvent(new UnhandledResponseEvent(request, response));
        }
      }
    }

  }

}
