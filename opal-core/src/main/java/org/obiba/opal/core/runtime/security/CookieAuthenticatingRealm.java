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
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.web.security.HttpCookieAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CookieAuthenticatingRealm extends AbstractHttpAuthenticatingRealm {

  public CookieAuthenticatingRealm() {
    super();
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof HttpCookieAuthenticationToken;
  }

  @Override
  protected String getSessionId(AuthenticationToken token) {
    return ((HttpCookieAuthenticationToken) token).getSessionId();
  }

  @Override
  protected AuthenticationInfo createtAuthenticationInfo(AuthenticationToken token, PrincipalCollection principals) {
    HttpCookieAuthenticationToken cookieToken = (HttpCookieAuthenticationToken) token;
    String urlHash = getUrlHash(cookieToken.getSessionId(), cookieToken.getUrl());
    return new SimpleAccount(principals, urlHash);
  }

  /**
   * @param sessionId
   * @param url
   * @return
   */
  private String getUrlHash(String sessionId, String url) {
    return new Md5Hash(url, sessionId).toHex();
  }

}
