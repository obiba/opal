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
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.obiba.opal.web.security.HttpCookieAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CookieAuthenticatingRealm extends AuthorizingRealm {

  public CookieAuthenticatingRealm() {
    super();
    super.setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    return token instanceof HttpCookieAuthenticationToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    HttpCookieAuthenticationToken cookieToken = (HttpCookieAuthenticationToken) token;

    Session session = getSession(cookieToken.getSessionId());
    if(session != null) {
      // Extract the principals from the session
      PrincipalCollection principals = (PrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
      if(principals != null) {
        return new SimpleAccount(principals, null);
      }
    } else {
      throw new IncorrectCredentialsException();
    }

    throw new AuthenticationException();
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    return null;
  }

  Session getSession(String sessionId) {
    if(sessionId != null) {
      SessionManager manager = getSessionManager();
      if(manager != null) {
        SessionKey key = new DefaultSessionKey(sessionId);
        try {
          return manager.getSession(key);
        } catch(SessionException e) {
          // Means that the session does not exist or has expired.
        }
      }
    }
    return null;
  }

  SessionManager getSessionManager() {
    SecurityManager sm = SecurityUtils.getSecurityManager();
    if(sm instanceof SessionsSecurityManager) return (SessionsSecurityManager) sm;
    return null;
  }

}
