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

import java.util.Set;

import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

/**
 *
 */
public class DefaultResourceAuthorizationRequestBuilder implements ResourceAuthorizationRequestBuilder {

  private String resource;

  private HttpMethod method;

  private HasAuthorization toAuthorize;

  private static ResourceAuthorizationCache authorizationCache;

  public static void setup(ResourceAuthorizationCache authorizationCache) {
    DefaultResourceAuthorizationRequestBuilder.authorizationCache = authorizationCache;
  }

  @Override
  public DefaultResourceAuthorizationRequestBuilder authorize(HasAuthorization toAuthorize) {
    if(toAuthorize == null) throw new IllegalArgumentException("UI object to authorize cannot be null");
    this.toAuthorize = toAuthorize;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder request(String resource, HttpMethod method) {
    forResource(resource);
    return method(method);
  }

  @Override
  public ResourceAuthorizationRequestBuilder forResource(String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.resource = resource;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder get() {
    return method(HttpMethod.GET);
  }

  @Override
  public ResourceAuthorizationRequestBuilder post() {
    return method(HttpMethod.POST);
  }

  @Override
  public ResourceAuthorizationRequestBuilder put() {
    return method(HttpMethod.PUT);
  }

  @Override
  public ResourceAuthorizationRequestBuilder delete() {
    return method(HttpMethod.DELETE);
  }

  private ResourceAuthorizationRequestBuilder method(HttpMethod method) {
    this.method = method;
    return this;
  }

  @Override
  public void send() {
    beforeAuthorization();

    if(authorizationCache.contains(resource)) {
      apply(authorizationCache.get(resource));
    } else {
      ResourceRequestBuilderFactory.newBuilder().forResource(resource).options()
          .withAuthorizationCallback(new AuthorizationCallback() {

            @Override
            public void onResponseCode(Request request, Response response, Set<HttpMethod> allowed) {
              apply(allowed);
            }

          })//
          .withCallback(Response.SC_NOT_FOUND, new ResponseCodeCallback() {

            @Override
            public void onResponseCode(Request request, Response response) {
              unauthorized();
            }
          }).send();
    }
  }

  private void apply(Set<HttpMethod> allowed) {
    // GWT.log(resource + ": " + allowed);
    if(allowed != null && allowed.contains(method)) {
      authorized();
    } else {
      unauthorized();
    }
  }

  private void beforeAuthorization() {
    toAuthorize.beforeAuthorization();
  }

  private void authorized() {
    toAuthorize.authorized();
  }

  private void unauthorized() {
    toAuthorize.unauthorized();
  }

}
