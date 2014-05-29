/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class SecurityResourceTest {

  private final static String TEST_SESSION_ID = "test-session-id";

  private DefaultSecurityManager mockSecurityManager;

  private SimpleAccountRealm mockRealm;

  private AuthenticationResource securityResource;

  @Before
  public void setUp() {
    mockSecurityManager = new DefaultSecurityManager();
    mockRealm = new SimpleAccountRealm();
    mockSecurityManager.setRealm(mockRealm);

    SecurityUtils.setSecurityManager(mockSecurityManager);

    securityResource = new AuthenticationResource();
    securityResource.setSecurityManager(mockSecurityManager);
  }

  @Ignore
  @Test
  public void testLogin() throws FileSystemException {
    mockRealm.addAccount("administrator", "password");
    Response response = securityResource.createSession(mockHttpServletRequest(), "administrator", "password");
    assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
  }

  @Test
  public void testCheckSession() {
    Session mockSession = createMock(Session.class);

    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(TEST_SESSION_ID))).andReturn(mockSession).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(TEST_SESSION_ID);
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

    verify(sessionManager);
  }

  @Test
  public void testCheckSessionThrowsSessionException() {
    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(TEST_SESSION_ID))).andThrow(new SessionException()).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(TEST_SESSION_ID);
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    verify(sessionManager);
  }

  @Test
  public void testCheckSessionReturnsNull() {
    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(TEST_SESSION_ID))).andReturn(null).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(TEST_SESSION_ID);
    assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    verify(sessionManager);
  }

  @Test
  public void testDeleteSession() {
    Response response = securityResource.deleteSession();
    assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());
  }

  private HttpServletRequest mockHttpServletRequest() {
    HttpServletRequest httpServletRequestMock = createMock(HttpServletRequest.class);
    expect(httpServletRequestMock.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
    return httpServletRequestMock;
  }

  private SessionManager mockSessionManager() {
    SessionManager mockSessionManager = createMock(SessionManager.class);
    mockSecurityManager.setSessionManager(mockSessionManager);
    return mockSessionManager;
  }

  private SessionKey expectSession(String sessionId) {
    EasyMock.reportMatcher(new SessionKeyMatcher(sessionId));
    return null;
  }

  private static class SessionKeyMatcher implements IArgumentMatcher {

    private final String sessionId;

    private SessionKeyMatcher(String sessionId) {
      this.sessionId = sessionId;
    }

    @Override
    public void appendTo(StringBuffer buffer) {

    }

    @Override
    public boolean matches(Object argument) {
      return ((SessionKey) argument).getSessionId().equals(sessionId);
    }
  }
}
