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

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.UIObjectAuthorizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.UIObject;

/**
 *
 */
public class DefaultResourceAuthorizationRequestBuilder implements ResourceAuthorizationRequestBuilder {

  private String resource;

  private HttpMethod method;

  private Set<HasAuthorization> hasAuthorization = new LinkedHashSet<HasAuthorization>();

  private static ResourceAuthorizationCache authorizationCache;

  public static void setup(ResourceAuthorizationCache authorizationCache) {
    DefaultResourceAuthorizationRequestBuilder.authorizationCache = authorizationCache;
  }

  public DefaultResourceAuthorizationRequestBuilder authorize(UIObject toAuthorize) {
    authorize(new UIObjectAuthorizer(toAuthorize));
    return this;
  }

  public DefaultResourceAuthorizationRequestBuilder authorize(HasAuthorization toAuthorize) {
    this.hasAuthorization.add(toAuthorize);
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder forResource(String resource) {
    if(resource == null) throw new IllegalArgumentException("path cannot be null");
    this.resource = resource;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder get() {
    this.method = HttpMethod.GET;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder post() {
    this.method = HttpMethod.POST;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder put() {
    this.method = HttpMethod.PUT;
    return this;
  }

  @Override
  public ResourceAuthorizationRequestBuilder delete() {
    this.method = HttpMethod.DELETE;
    return this;
  }

  @Override
  public void send() {
    beforeAuthorization();

    GWT.log(resource + ": " + authorizationCache.get(resource));

    if(authorizationCache.contains(resource)) {
      apply(authorizationCache.get(resource));
    } else {
      ResourceRequestBuilderFactory.newBuilder().forResource(resource).options().withAuthorizationCallback(new AuthorizationCallback() {

        @Override
        public void onResponseCode(Request request, Response response, Set<HttpMethod> allowed) {
          apply(allowed);
        }

      }).send();
    }
  }

  private void apply(Set<HttpMethod> allowed) {
    if(allowed != null && allowed.contains(method)) {
      authorized();
    } else {
      unauthorized();
    }
  }

  private void beforeAuthorization() {
    for(HasAuthorization toAuthorize : hasAuthorization) {
      toAuthorize.beforeAuthorization();
    }
  }

  private void authorized() {
    for(HasAuthorization toAuthorize : hasAuthorization) {
      toAuthorize.authorized();
    }
  }

  private void unauthorized() {
    for(HasAuthorization toAuthorize : hasAuthorization) {
      toAuthorize.unauthorized();
    }
  }

}
