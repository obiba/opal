/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * A request that authenticates with a header carries no ambient credentials, so the referer/agent check does not apply.
 */
public class CSRFInterceptorTest {

  @Test(expected = ForbiddenException.class)
  public void unknownAgentWithoutRefererIsForbidden() {
    interceptor().preProcess(servletRequest(), null, request(null, null));
  }

  @Test
  public void unknownAgentWithOpalAuthHeaderIsAllowed() {
    interceptor().preProcess(servletRequest(), null, request("token", null));
  }

  @Test
  public void unknownAgentWithAuthorizationHeaderIsAllowed() {
    interceptor().preProcess(servletRequest(), null, request(null, "Bearer token"));
  }

  private CSRFInterceptor interceptor() {
    return new CSRFInterceptor(true, "", "curl", new CSRFTokenHelper());
  }

  private HttpServletRequest servletRequest() {
    HttpServletRequest servletRequest = createNiceMock(HttpServletRequest.class);
    replay(servletRequest);
    return servletRequest;
  }

  private ContainerRequestContext request(String opalAuth, String authorization) {
    ContainerRequestContext request = createNiceMock(ContainerRequestContext.class);
    expect(request.getMethod()).andStubReturn("GET");
    expect(request.getHeaderString("Host")).andStubReturn("opal.example.org");
    expect(request.getHeaderString("User-Agent")).andStubReturn("Mozilla/5.0 proxy");
    expect(request.getHeaderString(OpalAuth.CREDENTIALS_HEADER)).andStubReturn(opalAuth);
    expect(request.getHeaderString(HttpHeaders.AUTHORIZATION)).andStubReturn(authorization);
    replay(request);
    return request;
  }
}
