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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.runtime.security.OpalSecurityManager;

import com.google.common.collect.Sets;

public class SecurityResourceTest {

  private SecurityResource securityResource;

  String testSessionId = "test-session-id";

  @Before
  public void setUp() throws URISyntaxException {
    securityResource = new SecurityResource();
  }

  private void startTestOpalSecurityManager() {
    OpalSecurityManager securityManager = new OpalSecurityManager(Sets.newHashSet((Realm) new SimpleAccountRealm()));
    System.setProperty("OPAL_HOME", getClass().getResource("/").getPath().toString());
    securityManager.start();
  }

  @Test
  public void testLogin() {
    startTestOpalSecurityManager();
    Response response = securityResource.createSession("administrator", "password");
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());
  }

  @Test
  public void testLoginBadCredentials() {
    startTestOpalSecurityManager();
    Response response = securityResource.createSession("user", "password");
    Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  private org.apache.shiro.mgt.SecurityManager mockSecurityManager() {
    org.apache.shiro.mgt.SecurityManager securityManagerMock = createMock(org.apache.shiro.mgt.SecurityManager.class);
    SecurityUtils.setSecurityManager(securityManagerMock);
    return securityManagerMock;
  }

  @Test
  public void testCheckSession() {

    org.apache.shiro.mgt.SecurityManager securityManagerMock = mockSecurityManager();

    expect(securityManagerMock.isValid(testSessionId)).andReturn(true).atLeastOnce();
    replay(securityManagerMock);

    Response response = securityResource.checkSession(testSessionId);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    verify(securityManagerMock);
  }

  @Test
  public void testCheckSessionNotFound() {

    org.apache.shiro.mgt.SecurityManager securityManagerMock = mockSecurityManager();

    expect(securityManagerMock.isValid(testSessionId)).andReturn(false).atLeastOnce();
    replay(securityManagerMock);

    Response response = securityResource.checkSession(testSessionId);
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(securityManagerMock);
  }

  @Test
  public void testDeleteSession() {
    Response response = securityResource.deleteSession(testSessionId);
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }
}
