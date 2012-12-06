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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.net.URISyntaxException;

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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.SubjectAclService;

public class SecurityResourceTest {

  private DefaultSecurityManager mockSecurityManager;

  private SimpleAccountRealm mockRealm;

  private AuthenticationResource securityResource;

  String testSessionId = "test-session-id";

  @Before
  public void setUp() throws URISyntaxException {
    mockSecurityManager = new DefaultSecurityManager();
    mockRealm = new SimpleAccountRealm();
    mockSecurityManager.setRealm(mockRealm);

    SecurityUtils.setSecurityManager(mockSecurityManager);

    securityResource = new AuthenticationResource(mockSecurityManager);
  }

  @Ignore
  @Test
  public void testLogin() throws FileSystemException {
    mockRealm.addAccount("administrator", "password");
    Response response = securityResource.createSession("administrator", "password");
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
  }

  @Test
  public void testLoginBadCredentials() throws FileSystemException {
    Response response = securityResource.createSession("admninistrator", "password");
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  public void testCheckSession() {
    Session mockSession = EasyMock.createMock(Session.class);

    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(testSessionId))).andReturn(mockSession).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(testSessionId);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    verify(sessionManager);
  }

  @Test
  public void testCheckSessionThrowsSessionException() {
    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(testSessionId))).andThrow(new SessionException()).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(testSessionId);
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(sessionManager);
  }

  @Test
  public void testCheckSessionReturnsNull() {
    SessionManager sessionManager = mockSessionManager();
    expect(sessionManager.getSession(expectSession(testSessionId))).andReturn(null).atLeastOnce();
    replay(sessionManager);

    Response response = securityResource.checkSession(testSessionId);
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(sessionManager);
  }

  @Test
  public void testDeleteSession() {
    Response response = securityResource.deleteSession(testSessionId);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }

  private SessionManager mockSessionManager() {
    SessionManager mockSessionManager = createMock(SessionManager.class);
    this.mockSecurityManager.setSessionManager(mockSessionManager);
    return mockSessionManager;
  }

  private SessionKey expectSession(String sessionId) {
    EasyMock.reportMatcher(new SessionKeyMatcher(sessionId));
    return null;
  }

  private static class SessionKeyMatcher implements IArgumentMatcher {

    private final String sessionId;

    public SessionKeyMatcher(String sessionId) {
      this.sessionId = sessionId;
    }

    @Override
    public void appendTo(StringBuffer buffer) {

    }

    @Override
    public boolean matches(Object argument) {
      return ((SessionKey) argument).getSessionId().equals(this.sessionId);
    }
  }
}
