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
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.util.SimpleByteSource;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Realm for users defined in opal's own users database.
 */
@Component
public class OpalUserRealm extends OpalBaseRealm implements InitializingBean {

  public static final String OPAL_REALM = "opal-user-realm";

  /**
   * Number of times the user password is hashed for attack resiliency
   */
  @Value("${org.obiba.opal.security.password.nbHashIterations}")
  private int nbHashIterations;

  @Autowired
  private OpalConfigurationService opalConfigurationService;

  private String salt;

  @Override
  public void afterPropertiesSet() {
    setCacheManager(new MemoryConstrainedCacheManager());

    HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher(Sha512Hash.ALGORITHM_NAME);
    credentialsMatcher.setHashIterations(nbHashIterations);
    setCredentialsMatcher(credentialsMatcher);

    salt = opalConfigurationService.getOpalConfiguration().getSecretKey();
  }

  @Override
  public String getName() {
    return OPAL_REALM;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();

    // Null username is invalid
    if (username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }

    SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(username);
    if (subjectCredentials == null || !subjectCredentials.isEnabled()) {
      throw new UnknownAccountException("No account found for subjectCredentials [" + username + "]");
    }
    SimpleAuthenticationInfo authInfo = new SimpleAuthenticationInfo(username, subjectCredentials.getPassword(),
        getName());
    authInfo.setCredentialsSalt(new SimpleByteSource(salt));
    return authInfo;
  }

}
