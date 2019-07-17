/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.NoSuchSubjectTokenException;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.SubjectTokenService;
import org.obiba.shiro.authc.HttpHeaderAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * A realm for handling personal access API tokens represented by {@link org.obiba.opal.core.domain.security.SubjectToken}.
 */
@Component
public class OpalTokenRealm extends OpalBaseRealm {

  public static final String TOKEN_REALM = "opal-token-realm";

  @Autowired
  private SubjectTokenService subjectTokenService;

  @Autowired
  private SubjectProfileService subjectProfileService;

  @PostConstruct
  public void postConstruct() {
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
      SubjectProfile subProfile = subjectProfileService.getProfile(subToken.getPrincipal());
      SimplePrincipalCollection principals = new SimplePrincipalCollection();
      principals.add(subToken.getPrincipal(), subProfile.getFirstRealm());
      principals.add(subToken.getToken(), getName());
      return new SimpleAuthenticationInfo(principals, subToken.getToken());
    } catch(NoSuchSubjectTokenException| NoSuchSubjectProfileException e) {
      throw new UnknownAccountException("No account found for subjectToken [" + tokenId + "]");
    }
  }
}
