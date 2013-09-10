/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.user.Group;
import org.obiba.opal.core.domain.user.User;
import org.obiba.opal.core.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrientDbRealm extends AuthorizingRealm {

  @Autowired
  private UserService userService;

  public OrientDbRealm() {
    setCacheManager(new MemoryConstrainedCacheManager());
    setCredentialsMatcher(new HashedCredentialsMatcher("SHA"));
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();

    // Null username is invalid
    if(username == null) {
      throw new AccountException("Null usernames are not allowed by this realm.");
    }

    User user = userService.getUserWithName(username);
    if(user == null) {
      throw new UnknownAccountException("No account found for user [" + username + "]");
    }
    return new SimpleAuthenticationInfo(username, user.getPassword(), getName());
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    // Null username is invalid
    if(principals == null) {
      throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
    }

    Set<String> roleNames = new HashSet<String>();
    String username = (String) getAvailablePrincipal(principals);
    User user = userService.getUserWithName(username);
    if(user != null) {
      for(Group group : user.getGroups()) {
        roleNames.add(group.getName());
      }
    }
    return new SimpleAuthorizationInfo(roleNames);
  }

}
