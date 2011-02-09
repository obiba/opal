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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;
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

  private final static String OPAL_WS_ROOT = "/ws";

  private final static String RESOURCE_MEDIA_TYPE = "application/x-protobuf+json";

  private static RequestEventBus eventBus;

  private static RequestCredentials credentials;

  private static ResourceAuthorizationCache authorizationCache;

  private String uri;

  private String contentType;

  private String body;

  private Set<String> accept = new HashSet<String>();

  private Map<String, String> form = new HashMap<String, String>();

  private ResourceCallback<T> resourceCallback;

  // An array of handlers for HTTP status codes: the index of the array is the HTTP code. This will be 99% empty. This
  // may not be appropriate, depends on how the browser handles this...
  // TODO: determine the implications fo this
  private ResponseCodeCallback[] codes;

  private AuthorizationCallback authorizationCallback;

  private HttpMethod method;

  private RequestBuilder builder;

  public DefaultResourceRequestBuilder() {
  }

  public static void setup(RequestEventBus requestEventBus, RequestCredentials credentials, ResourceAuthorizationCache authorizationCache) {
    DefaultResourceRequestBuilder.eventBus = requestEventBus;
    DefaultResourceRequestBuilder.credentials = credentials;
    DefaultResourceRequestBuilder.authorizationCache = authorizationCache;
  }

  public DefaultResourceRequestBuilder<T> forResource(String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.uri = resource.startsWith("http") ? resource : OPAL_WS_ROOT + resource;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback) {
    accept(RESOURCE_MEDIA_TYPE);
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

  @Override
  public ResourceRequestBuilder<T> withAuthorizationCallback(AuthorizationCallback callback) {
    this.authorizationCallback = callback;
    return this;
  }

  public DefaultResourceRequestBuilder<T> accept(String acceptHeader) {
    this.accept.add(acceptHeader);
    return this;
  }

  public DefaultResourceRequestBuilder<T> withBody(String contentType, String body) {
    this.contentType = contentType;
    this.body = body;
    return this;
  }

  public DefaultResourceRequestBuilder<T> withResourceBody(/* T.stringify() */String dto) {
    // In this case, the response should be of the same type. Tell the server we accept the type we posted.
    accept(RESOURCE_MEDIA_TYPE);
    return withBody(RESOURCE_MEDIA_TYPE, dto);
  }

  public DefaultResourceRequestBuilder<T> withFormBody(String key1, String value1, String... keyValues) {
    form.put(key1, URL.encodeQueryString(value1));
    for(int i = 0; i < keyValues.length; i += 2) {
      form.put(keyValues[i], URL.encodeQueryString(keyValues[i + 1]));
    }
    return this;
  }

  public DefaultResourceRequestBuilder<T> get() {
    method = HttpMethod.GET;
    return this;
  }

  public DefaultResourceRequestBuilder<T> head() {
    method = HttpMethod.HEAD;
    return this;
  }

  public DefaultResourceRequestBuilder<T> post() {
    method = HttpMethod.POST;
    return this;
  }

  public DefaultResourceRequestBuilder<T> put() {
    method = HttpMethod.PUT;
    return this;
  }

  public DefaultResourceRequestBuilder<T> delete() {
    method = HttpMethod.DELETE;
    return this;
  }

  public DefaultResourceRequestBuilder<T> options() {
    method = HttpMethod.OPTIONS;
    return this;
  }

  public RequestBuilder build() {
    builder = new InnerRequestBuilder(method, uri);
    builder.setCallback(new InnerCallback());
    if(this.accept.size() > 0) {
      builder.setHeader("Accept", buildAcceptHeader());
    }
    if(body != null) {
      builder.setHeader("Content-Type", contentType);
      builder.setRequestData(body);
    }
    if(form != null && form.size() > 0) builder.setRequestData(encodeForm());
    if(credentials != null) credentials.provideCredentials(builder);
    return builder;
  }

  public Request send() {
    try {
      return build().send();
    } catch(RequestException e) {
      eventBus.fireEvent(new RequestErrorEvent(e));
      return null;
    }
  }

  private String buildAcceptHeader() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for(String accept : this.accept) {
      if(first != true) sb.append(", ");
      sb.append(accept);
      first = false;
    }
    return sb.toString();
  }

  private String encodeForm() {
    builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
    boolean needSeparator = false;
    StringBuilder sb = new StringBuilder();
    for(String key : this.form.keySet()) {
      String value = this.form.get(key);
      if(needSeparator) sb.append('&');
      sb.append(key).append('=').append(value);
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

      if(credentials.hasExpired(builder) || code == 401) {
        // this is fired even after a request for deleting the session
        eventBus.fireEvent(new RequestCredentialsExpiredEvent());
      } else {
        processResponse(request, response);
      }

    }

    private void processResponse(Request request, Response response) {
      int code = response.getStatusCode();

      cacheAuthorization(response);

      if(authorizationCallback != null) {
        authorizationCallback.onResponseCode(request, response, authorizationCache.get(uri));
      }

      if(codes != null && codes[code] != null) {
        codes[code].onResponseCode(request, response);
      } else if(resourceCallback != null && code < 400) {
        final T resource = (T) JsonUtils.unsafeEval(response.getText());
        resourceCallback.onResource(response, resource);
      } else if(authorizationCallback == null) {
        eventBus.fireEvent(new UnhandledResponseEvent(request, response));
      }
    }

    private void cacheAuthorization(Response response) {
      Set<HttpMethod> allowed = getAllowedMethods(response);
      if(allowed.size() > 0) {
        // GWT.log(authorizationCache.toString());
        authorizationCache.put(uri, allowed);
      }
    }

    private Set<HttpMethod> getAllowedMethods(Response response) {
      Set<HttpMethod> allowed = new LinkedHashSet<HttpMethod>();
      String header = response.getHeader("Allow");
      if(header != null && header.length() > 0) {
        for(String string : header.split(",")) {
          allowed.add(HttpMethod.valueOf(string.trim()));
        }
      }

      return allowed;
    }

  }

  private class InnerRequestBuilder extends RequestBuilder {

    /**
     * @param httpMethod
     * @param url
     */
    protected InnerRequestBuilder(HttpMethod httpMethod, String url) {
      super(httpMethod.toString(), url);
    }

  }

}
