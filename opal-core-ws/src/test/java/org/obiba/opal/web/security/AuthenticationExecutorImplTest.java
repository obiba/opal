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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.oidc.OIDCConfiguration;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.realm.OpalOIDCRealm;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * #4235: an OIDC login writes the groups of its own UserInfo to the profile, and marks the session so that the
 * authorization interceptor does not overwrite them with the roles seen by permission resolution.
 */
public class AuthenticationExecutorImplTest {

  private final SubjectProfileService profiles = createMock(SubjectProfileService.class);

  private final AuthenticationExecutorImpl executor = new AuthenticationExecutorImpl();

  @Before
  public void setUp() throws Exception {
    OIDCConfiguration configuration = new OIDCConfiguration("keycloak");
    configuration.setClientId("opal");
    configuration.setCustomParam("groupsClaim", "roles");
    SecurityUtils.setSecurityManager(new DefaultSecurityManager(new OpalOIDCRealm(configuration)));
    Field field = AuthenticationExecutorImpl.class.getDeclaredField("subjectProfileService");
    field.setAccessible(true);
    field.set(executor, profiles);
  }

  @After
  public void tearDown() {
    SecurityUtils.setSecurityManager(null);
  }

  @Test
  public void oidcLoginStoresTheGroupsOfItsOwnUserInfo() {
    expect(profiles.supportProfile("alice")).andReturn(true).anyTimes();
    profiles.ensureProfile(anyObject(PrincipalCollection.class));
    expectLastCall().times(2);
    profiles.applyProfileGroups("alice", Set.of("analysts", "keycloak"));
    profiles.applyProfileGroups("alice", Set.of("reviewers", "keycloak"));
    replay(profiles);

    Session first = login("analysts");
    Session second = login("reviewers");

    verify(profiles);
    assertThat(first.getAttribute(AuthorizationInterceptor.ENSURED_PROFILE_GROUPS)).isEqualTo(true);
    assertThat(second.getAttribute(AuthorizationInterceptor.ENSURED_PROFILE_GROUPS)).isEqualTo(true);
  }

  @Test
  public void loginFromAnotherRealmLeavesTheGroupsToTheInterceptor() {
    expect(profiles.supportProfile("bob")).andReturn(true);
    profiles.ensureProfile(anyObject(PrincipalCollection.class));
    replay(profiles);

    Session session = login(new SimplePrincipalCollection("bob", "opal-user-realm"), "bob");

    verify(profiles);
    assertThat(session.getAttribute(AuthorizationInterceptor.ENSURED_PROFILE_GROUPS)).isNull();
  }

  private Session login(String group) {
    Map<String, Object> userInfo = Map.of("sub", "1234", "preferred_username", "alice", "roles", List.of(group));
    return login(new SimplePrincipalCollection(List.of("alice", userInfo), "keycloak"), "alice");
  }

  private Session login(PrincipalCollection principals, String username) {
    Session session = new SimpleSession();
    Subject subject = createNiceMock(Subject.class);
    expect(subject.getPrincipal()).andReturn(username).anyTimes();
    expect(subject.getPrincipals()).andReturn(principals).anyTimes();
    expect(subject.getSession(false)).andReturn(session).anyTimes();
    replay(subject);
    executor.ensureProfile(subject);
    return session;
  }
}
