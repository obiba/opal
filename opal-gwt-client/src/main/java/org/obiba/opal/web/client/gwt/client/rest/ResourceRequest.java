/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.client.gwt.client.rest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestPermissionException;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;

/**
 * A VERY simple pattern for making requests to restful services that return JSON. The response text (JSON) is parsed
 * using {@code JsonUtils#unsafeEval(String)} and cast to the generic type of this class.
 * 
 * @param <T> the JavaScript overlay type returned by the RESTful service
 */
public class ResourceRequest<T extends JavaScriptObject> {

  public static String OPAL_WS_ROOT = "http://localhost:8080";

  private final String uri;

  public ResourceRequest(final String path) {
    if(path == null) throw new IllegalArgumentException("path cannot be null");
    this.uri = path.startsWith("http") ? path : OPAL_WS_ROOT + path;
  }

  public void get(final ResourceCallback<T> callback) {
    final RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, uri);
    builder.setCallback(new StandardizedOnErrorCallback() {

      @Override
      public void onResponseReceived(Request request, Response response) {
        int status = response.getStatusCode();
        switch(status) {
        case Response.SC_OK:
          // Nominal case
          try {
            final T result = JsonUtils.unsafeEval(response.getText()).cast();
            callback.onResource(result);
          } catch(Exception e) {
            GWT.log("Error processing request ", e);
          }
          break;
        default:
          GWT.log("Request returned " + response.getStatusCode());
        }

      }
    });
    send(builder);
  }

  public void post(final String resource) {
    final RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, uri);
    builder.setRequestData(resource);

    builder.setCallback(new StandardizedOnErrorCallback() {

      @Override
      public void onResponseReceived(Request request, Response response) {
        // Handle POST result
      }

    });

    send(builder);
  }

  public void put(final String resource) {
    final RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, uri);
    builder.setRequestData(resource);

    builder.setCallback(new StandardizedOnErrorCallback() {

      @Override
      public void onResponseReceived(Request request, Response response) {
        // Handle PUT result
      }

    });

    send(builder);
  }

  public void delete() {
    final RequestBuilder builder = new RequestBuilder(RequestBuilder.DELETE, uri);
    builder.setCallback(new StandardizedOnErrorCallback() {

      @Override
      public void onResponseReceived(Request request, Response response) {
        // Handle DELETE result
      }

    });

    send(builder);
  }

  private RequestBuilder preHandle(final RequestBuilder builder) {
    // TODO: populate the custom authentication header. This should be the contents of the Cookie set by the server
    // after authentication.
    builder.setHeader("X-Opal-Auth", /* SomeStaticCookie.get().getValue() */"someValue");
    builder.setHeader("Accept", "application/json");
    return builder;
  }

  private void send(final RequestBuilder builder) {
    try {
      preHandle(builder).send();
    } catch(RequestPermissionException e) {
      // We violated the Same-Origin poliy
    } catch(RequestTimeoutException e) {
      // Darn!
    } catch(RequestException e) {
      // No idea what just happened
    }
  }

  private abstract class StandardizedOnErrorCallback implements RequestCallback {
    @Override
    public void onError(Request request, Throwable exception) {
      // TODO: Create a standard way of providing developer and user feedback
      GWT.log("Error processing request ", exception);
    }
  }
}
