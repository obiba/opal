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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.rest.client.event.RequestCredentialsExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestErrorEvent;
import org.obiba.opal.web.gwt.rest.client.event.RequestEventBus;
import org.obiba.opal.web.gwt.rest.client.event.UnhandledResponseEvent;

import com.google.common.collect.HashMultimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

@SuppressWarnings("StaticNonFinalField")
public class DefaultResourceRequestBuilder<T extends JavaScriptObject> implements ResourceRequestBuilder<T> {

  private final static String OPAL_WS_ROOT = "/ws";

  private final static String RESOURCE_MEDIA_TYPE = "application/x-protobuf+json";

  private static RequestEventBus eventBus;

  private static RequestCredentials credentials;

  private static ResourceAuthorizationCache authorizationCache;

  private static String version;

  private String resource;

  private String uri;

  private String contentType;

  private String body;

  private final Collection<String> accept = new HashSet<>();

  private final HashMultimap<String, String> form = HashMultimap.create();

  private ResourceCallback<T> resourceCallback;

  // An array of handlers for HTTP status responseCodes: the index of the array is the HTTP code. This will be 99% empty. This
  // may not be appropriate, depends on how the browser handles this...
  // TODO: determine the implications for this
  private ResponseCodeCallback[] codes;

  private AuthorizationCallback authorizationCallback;

  private HttpMethod method;

  private RequestBuilder builder;

  public DefaultResourceRequestBuilder() {
  }

  public static void setup(RequestEventBus requestEventBus, RequestCredentials credentials,
      ResourceAuthorizationCache authorizationCache) {
    eventBus = requestEventBus;
    DefaultResourceRequestBuilder.credentials = credentials;
    DefaultResourceRequestBuilder.authorizationCache = authorizationCache;
  }

  @Override
  public DefaultResourceRequestBuilder<T> forResource(
      @SuppressWarnings("ParameterHidesMemberVariable") String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.resource = resource.replaceAll("//", "/");
    uri = resource.startsWith("http") ? this.resource : OPAL_WS_ROOT + this.resource;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> withCallback(ResourceCallback<T> callback) {
    accept(RESOURCE_MEDIA_TYPE);
    resourceCallback = callback;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> withCallback(int code, ResponseCodeCallback callback) {
    accept(RESOURCE_MEDIA_TYPE);
    if(codes == null) {
      codes = new ResponseCodeCallback[Response.SC_HTTP_VERSION_NOT_SUPPORTED];
    }
    codes[code] = callback;
    return this;
  }

  @Override
  public ResourceRequestBuilder<T> withCallback(ResponseCodeCallback callback, int... responseCodes) {
    accept(RESOURCE_MEDIA_TYPE);
    if(codes == null) {
      codes = new ResponseCodeCallback[Response.SC_HTTP_VERSION_NOT_SUPPORTED];
    }
    if(responseCodes != null) {
      for(int code : responseCodes) {
        codes[code] = callback;
      }
    }
    return this;
  }

  @Override
  public ResourceRequestBuilder<T> withAuthorizationCallback(AuthorizationCallback callback) {
    authorizationCallback = callback;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> accept(String acceptHeader) {
    accept.add(acceptHeader);
    return this;
  }

  @SuppressWarnings("ParameterHidesMemberVariable")
  @Override
  public DefaultResourceRequestBuilder<T> withBody(String contentType, String body) {
    this.contentType = contentType;
    this.body = body;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> withResourceBody(/* T.stringify() */String dto) {
    // In this case, the response should be of the same type. Tell the server we accept the type we posted.
    accept(RESOURCE_MEDIA_TYPE);
    return withBody(RESOURCE_MEDIA_TYPE, dto);
  }

  @Override
  public DefaultResourceRequestBuilder<T> withFormBody(String... keyValues) {
    for(int i = 0; i < keyValues.length; i += 2) {
      form.put(keyValues[i], URL.encodeQueryString(keyValues[i + 1]));
    }
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> get() {
    method = HttpMethod.GET;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> head() {
    method = HttpMethod.HEAD;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> post() {
    method = HttpMethod.POST;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> put() {
    method = HttpMethod.PUT;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> delete() {
    method = HttpMethod.DELETE;
    return this;
  }

  @Override
  public DefaultResourceRequestBuilder<T> options() {
    method = HttpMethod.OPTIONS;
    return this;
  }

  @Override
  public RequestBuilder build() {
    builder = new InnerRequestBuilder(method, uri);
    builder.setCallback(new InnerCallback());
    if(accept.size() > 0) {
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

  @Nullable
  @Override
  public Request send() {
    try {
        UserSessionTracker.getInstance().sessionTouched();
      return build().send();
    } catch(RequestException e) {
      eventBus.fireEvent(new RequestErrorEvent.Builder().exception(e).build());
      return null;
    }
  }

  private String buildAcceptHeader() {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for(String s : accept) {
      if(!first) sb.append(", ");
      sb.append(s);
      first = false;
    }
    return sb.toString();
  }

  private String encodeForm() {
    builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
    boolean needSeparator = false;
    StringBuilder sb = new StringBuilder();
    for(String key : form.keySet()) {
      for(String value : form.get(key)) {
        if(needSeparator) sb.append('&');
        sb.append(key).append('=').append(value);
        needSeparator = true;
      }
    }
    return sb.toString();
  }

  private class InnerCallback implements RequestCallback {

    @Override
    public void onError(Request request, Throwable exception) {
      eventBus.fireEvent(new RequestErrorEvent.Builder().exception(exception).build());
    }

    @Override
    public void onResponseReceived(Request request, Response response) {
      int code = response.getStatusCode();
      if(code == 0) {
        GWT.log(
            "Invalid status code. Status text was '" + response.getStatusText() + "'. Interrupting response handling.");
        throw new IllegalStateException("Invalid status code.");
      }

      setOpalVersion(response);

      if(credentials.hasExpired(builder) || code == Response.SC_UNAUTHORIZED) {
        // this is fired even after a request for deleting the session
        eventBus.fireEvent(new RequestCredentialsExpiredEvent());
      } else {
        processResponse(request, response);
      }

    }

    @SuppressWarnings("unchecked")
    private void processResponse(Request request, Response response) {
      int code = response.getStatusCode();

      cacheAuthorization(response);

      boolean handled = false;
      if(authorizationCallback != null) {
        handled = true;
        authorizationCallback.onResponseCode(request, response, authorizationCache.get(uri));
      }

      if(codes != null && codes[code] != null) {
        handled = true;
        codes[code].onResponseCode(request, response);
      }

      if(resourceCallback != null && code < Response.SC_BAD_REQUEST) {
        handled = true;

        try {
          resourceCallback.onResource(response, (T) JsonUtils.unsafeEval(response.getText()));
        } catch(Exception e) {
          GWT.log("ERROR: unsafeEval fails on response: " + uri + " -> " + response.getText() + ": " + e.getMessage());
        }
      }

      if(!handled) {
        eventBus.fireEvent(new UnhandledResponseEvent(method, uri, response));
      }
    }

    private void cacheAuthorization(Response response) {
      // do not cache when client or server error
      if(response.getStatusCode() >= Response.SC_BAD_REQUEST) return;

      Set<HttpMethod> allowed = getAllowedMethods(response);
      if(allowed.size() > 0) {
        authorizationCache.put(uri, allowed);
      }
    }

    private Set<HttpMethod> getAllowedMethods(Response response) {
      Set<HttpMethod> allowed = new LinkedHashSet<>();
      String header = response.getHeader("Allow");
      if(header != null && header.length() > 0) {
        for(String string : header.split(",")) {
          allowed.add(HttpMethod.valueOf(string.trim()));
        }
      }

      return allowed;
    }

    private void setOpalVersion(Response response) {
      String header = response.getHeader("X-Opal-Version");
      if(header != null && header.length() > 0) {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        version = header;
      }
    }

  }

  private static class InnerRequestBuilder extends RequestBuilder {

    /**
     * @param httpMethod
     * @param url
     */
    protected InnerRequestBuilder(HttpMethod httpMethod, String url) {
      super(httpMethod.toString(), url);
    }

  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getUri() {
    return uri;
  }

  @Override
  public String getResource() {
    return resource;
  }
}
