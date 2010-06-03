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

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.event.SessionExpiredEvent;
import org.obiba.opal.web.gwt.inject.client.OpalGinjector;
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
import com.google.gwt.http.client.URL;

public class DefaultResourceRequestBuilder<T extends JavaScriptObject> implements ResourceRequestBuilder<T> {

  private static OpalGinjector OPAL_GINJECTOR;

  private final static String OPAL_WS_ROOT = "/ws";

  private final static String RESOURCE_MEDIA_TYPE = "application/x-protobuf+json";

  private final EventBus eventBus;

  private final RequestCredentials credentials;

  private String uri;

  private String contentType;

  private String body;

  private String acceptHeader;

  private Map<String, String> form = new HashMap<String, String>();

  private ResourceCallback<T> resourceCallback;

  // An array of handlers for HTTP status codes: the index of the array is the HTTP code. This will be 99% empty. This
  // may not be appropriate, depends on how the browser handles this...
  // TODO: determine the implications fo this
  private ResponseCodeCallback[] codes;

  private RequestBuilder.Method method;

  private RequestBuilder builder;

  public DefaultResourceRequestBuilder() {
    if(OPAL_GINJECTOR == null) throw new IllegalStateException("initialize the OpalGinjector static variable before making a request");
    this.eventBus = OPAL_GINJECTOR.getEventBus();
    this.credentials = OPAL_GINJECTOR.getRequestCredentials();
  }

  public static void setup(OpalGinjector opalGinjector) {
    OPAL_GINJECTOR = opalGinjector;
  }

  public DefaultResourceRequestBuilder<T> forResource(String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.uri = resource.startsWith("http") ? resource : OPAL_WS_ROOT + resource;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback) {
    this.resourceCallback = callback;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withCallback(int code, ResponseCodeCallback callback) {
    if(codes == null) {
      codes = new ResponseCodeCallback[505];
    }
    codes[code] = callback;
    return this;
  }

  public DefaultResourceRequestBuilder<T> accept(String acceptHeader) {
    this.acceptHeader = acceptHeader;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withBody(String contentType, String body) {
    this.contentType = contentType;
    this.body = body;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withResourceBody(/* T.stringify() */String dto) {
    return withBody(RESOURCE_MEDIA_TYPE, dto);
  }

  public DefaultResourceRequestBuilder<T> withFormBody(String key1, String value1, String... keyValues) {
    form.put(key1, com.google.gwt.http.client.URL.encodeComponent(value1));
    for(int i = 0; i < keyValues.length; i += 2) {
      form.put(keyValues[i], com.google.gwt.http.client.URL.encodeComponent(keyValues[i + 1]));
    }
    return this;
  }

  public DefaultResourceRequestBuilder<T> get() {
    method = RequestBuilder.GET;
    return this;
  }

  public DefaultResourceRequestBuilder<T> head() {
    method = RequestBuilder.HEAD;
    return this;
  }

  public DefaultResourceRequestBuilder<T> post() {
    method = RequestBuilder.POST;
    return this;
  }

  public DefaultResourceRequestBuilder<T> put() {
    method = RequestBuilder.PUT;
    return this;
  }

  public DefaultResourceRequestBuilder<T> delete() {
    method = RequestBuilder.DELETE;
    return this;
  }

  public RequestBuilder build() {
    builder = new RequestBuilder(method, uri);
    builder.setCallback(new InnerCallback());
    if(resourceCallback != null && acceptHeader == null) {
      builder.setHeader("Accept", RESOURCE_MEDIA_TYPE);
    }
    if(acceptHeader != null) builder.setHeader("Accept", acceptHeader);
    if(body != null) {
      builder.setHeader("Content-Type", contentType);
      builder.setRequestData(body);
    }
    if(form != null && form.size() > 0) builder.setRequestData(encodeForm());
    if(credentials != null) credentials.provideCredentials(builder);
    return builder;
  }

  public void send() {
    try {
      build().send();
    } catch(RequestException e) {
      eventBus.fireEvent(new RequestErrorEvent(e));
    }
  }

  private String encodeForm() {
    builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
    boolean needSeparator = false;
    StringBuilder sb = new StringBuilder();
    for(String key : this.form.keySet()) {
      String value = this.form.get(key);
      if(needSeparator) sb.append('&');
      sb.append(key).append('=').append(URL.encode(value));
      needSeparator = true;
    }
    return sb.toString();
  }

  private class InnerCallback implements RequestCallback {
    @Override
    public void onError(Request request, Throwable exception) {
      eventBus.fireEvent(new RequestErrorEvent(exception));
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
      int code = response.getStatusCode();
      if(code == 0) {
        GWT.log("Invalid status code. Status text was '" + response.getStatusText() + "'. Interrupting response handling.");
        throw new IllegalStateException("Invalid status code.");
      }

      if(credentials.hasExpired(builder)) {
        eventBus.fireEvent(new SessionExpiredEvent());
      } else if(codes != null && codes[code] != null) {
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
