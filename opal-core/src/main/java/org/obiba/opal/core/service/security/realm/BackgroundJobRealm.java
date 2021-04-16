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

import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.springframework.stereotype.Component;

@Component
public class BackgroundJobRealm extends AuthorizingRealm {

  private final AuthenticationInfo simpleAccount = new SimpleAccount(SystemPrincipal.INSTANCE, null, getName());

  public BackgroundJobRealm() {
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof BackgroundJobServiceAuthToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return simpleAccount;
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    if(principals.oneByType(SystemPrincipal.class) != null) {
      info.addStringPermission("*");
    }
    return info;
  }

  @SuppressWarnings("Singleton")
  public static class SystemPrincipal implements Serializable {

    private static final long serialVersionUID = 7918271769058954770L;

    public static final SystemPrincipal INSTANCE = new SystemPrincipal();

    private SystemPrincipal() {
    }

    @Override
    public String toString() {
      return "opal/system";
    }

  }
}
