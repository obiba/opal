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
public abstract class CascadingAuthorizer implements HasAuthorization {

  private HasAuthorization authorizer;

  private ResourceAuthorizationRequestBuilder request;

  protected CascadingAuthorizer(ResourceAuthorizationRequestBuilder request, HasAuthorization authorizer) {
    super();
    this.authorizer = authorizer;
    this.request = request;
  }

  @Override
  public void beforeAuthorization() {
    authorizer.beforeAuthorization();
  }

  protected HasAuthorization getAuthorizer() {
    return authorizer;
  }

  protected ResourceAuthorizationRequestBuilder getRequest() {
    return request;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    CascadingAuthorizer first;

    CascadingAuthorizer next;

    protected Builder() {

    }

    public Builder and(String resource, HttpMethod method) {
      return and(ResourceAuthorizationRequestBuilderFactory.newBuilder().request(resource, method));
    }

    public Builder and(ResourceAuthorizationRequestBuilder resourceAuthorizationRequestBuilder) {
      return request(new AndAuthorizer(resourceAuthorizationRequestBuilder, null));
    }

    public Builder or(String resource, HttpMethod method) {
      return or(ResourceAuthorizationRequestBuilderFactory.newBuilder().request(resource, method));
    }

    public Builder or(ResourceAuthorizationRequestBuilder resourceAuthorizationRequestBuilder) {
      return request(new OrAuthorizer(resourceAuthorizationRequestBuilder, null));
    }

    private Builder request(CascadingAuthorizer newNext) {
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
