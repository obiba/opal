/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.magma.security.shiro.SudoAuthToken;
import org.springframework.stereotype.Component;

@Component
public class SudoRealm extends AuthorizingRealm {

  public SudoRealm() {
    super();
    super.setName("sudo-realm");
    // We don't care
    super.setCredentialsMatcher(null);
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof SudoAuthToken;
  }

  @Override
  public CredentialsMatcher getCredentialsMatcher() {
    return new CredentialsMatcher() {

      @Override
      public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        return true;
      }
    };
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    SudoAuthToken sudoToken = (SudoAuthToken) token;
    // TODO: test some kind of permission to conditionally accept the sudo request:
    // SecurityUtils.getSecurityManager().isPermitted(sudoToken.getSudoer(), "sudo")
    return new SimpleAccount(new SudoPrincipal(), null, getName());
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
    if(principals.oneByType(SudoPrincipal.class) != null) {
      info.addStringPermission("*");
    }
    return info;
  }

  private static class SudoPrincipal {

  }
}
