/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.NoSuchSubjectTokenException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.shiro.authc.HttpHeaderAuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A realm for handling personal access API tokens represented by {@link org.obiba.opal.core.domain.security.SubjectToken}.
 */
@Component
public class OpalTokenRealm extends AuthorizingRealm implements InitializingBean {

  public static final String TOKEN_REALM = "opal-token-realm";

  private final SubjectTokenService subjectTokenService;

  private final SubjectProfileService subjectProfileService;

  @Autowired
  public OpalTokenRealm(SubjectTokenService subjectTokenService, SubjectProfileService subjectProfileService) {
    this.subjectTokenService = subjectTokenService;
    this.subjectProfileService = subjectProfileService;
  }

  @Override
  public void afterPropertiesSet() {
    setAuthenticationTokenClass(HttpHeaderAuthenticationToken.class);
  }

  @Override
  public String getName() {
    return TOKEN_REALM;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    HttpHeaderAuthenticationToken httpToken = (HttpHeaderAuthenticationToken) token;
    String tokenId = httpToken.getPrincipal().toString();
    try {
      SubjectToken subToken = subjectTokenService.getToken(tokenId);
      if (!subjectTokenService.getTokenTimestamps(subToken).isActive()) {
        throw new ExpiredCredentialsException("Personal access token is inactive");
      }
      subjectTokenService.touchToken(subToken);
      SubjectProfile subProfile = subjectProfileService.getProfile(subToken.getPrincipal());
      SimplePrincipalCollection principals = new SimplePrincipalCollection();
      principals.add(subToken.getPrincipal(), subProfile.getFirstRealm());
      principals.add(tokenId, getName());
      return new SimpleAuthenticationInfo(principals, tokenId);
    } catch (NoSuchSubjectProfileException e) {
      throw new UnknownAccountException("No account found for personal access token");
    } catch (NoSuchSubjectTokenException e) {
      throw new UnknownAccountException("No such personal access token");
    }
  }

  /**
   * Roles are inherited from original user, extracted from ts profile (has the primary authenticating realm is not accessible).
   *
   * @param principals
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());
    if (thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Set<String> roleNames = new HashSet<>();
      String username = principals.getPrimaryPrincipal().toString();
      try {
        SubjectProfile subjectProfile = subjectProfileService.getProfile(username);
        roleNames.addAll(subjectProfile.getGroups());
        return new SimpleAuthorizationInfo(roleNames);
      } catch (NoSuchSubjectProfileException e) {
        // ignore
      }
    }
    return new SimpleAuthorizationInfo();
  }
}
