/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.rest.client.authorization;

import org.obiba.opal.web.gwt.rest.client.HttpMethod;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.ResourceAuthorizationRequestBuilderFactory;

/**
 * Cascade of authorizers: when authorization is received, request for a following authorization to be applied to the
 * provided authorizer.
 */
public class CascadingAuthorizer implements HasAuthorization {

  HasAuthorization authorizer;

  ResourceAuthorizationRequestBuilder request;

  protected CascadingAuthorizer(ResourceAuthorizationRequestBuilder request, HasAuthorization authorizer) {
    super();
    this.authorizer = authorizer;
    this.request = request;
  }

  @Override
  public void beforeAuthorization() {
    authorizer.beforeAuthorization();
  }

  @Override
  public void authorized() {
    request.authorize(authorizer).send();
  }

  @Override
  public void unauthorized() {
    authorizer.unauthorized();
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    CascadingAuthorizer first;

    CascadingAuthorizer next;

    protected Builder() {

    }

    public Builder request(String resource, HttpMethod method) {
      return request(ResourceAuthorizationRequestBuilderFactory.newBuilder().request(resource, method));
    }

    public Builder request(ResourceAuthorizationRequestBuilder resourceAuthorizationRequestBuilder) {
      CascadingAuthorizer newNext = new CascadingAuthorizer(resourceAuthorizationRequestBuilder, null);
      if(next != null) {
        next.authorizer = newNext;
      } else {
        first = newNext;
      }
      next = newNext;
      return this;
    }

    public Builder authorize(HasAuthorization authorizer) {
      if(next != null) {
        next.authorizer = authorizer;
      } else {
        first.authorizer = authorizer;
      }
      return this;
    }

    public CascadingAuthorizer build() {
      return first;
    }
  }
}
