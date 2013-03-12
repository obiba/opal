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

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.web.security.HttpHeaderAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class HttpHeaderAuthenticatingRealm extends AbstractHttpAuthenticatingRealm {

  public HttpHeaderAuthenticatingRealm() {
    super();
    super.setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof HttpHeaderAuthenticationToken;
  }

  @Override
  protected String getSessionId(AuthenticationToken token) {
    return ((HttpHeaderAuthenticationToken) token).getSessionId();
  }

  @Override
  protected AuthenticationInfo createtAuthenticationInfo(AuthenticationToken token, PrincipalCollection principals) {
    return new SimpleAccount(principals, null);
  }

}
