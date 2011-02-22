/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.jboss.resteasy.core.ResourceMethod;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NotAuthenticated;

abstract class AbstractSecurityComponent {

  private final SessionsSecurityManager securityManager;

  protected AbstractSecurityComponent(SessionsSecurityManager securityManager) {
    if(securityManager == null) throw new IllegalArgumentException("securityManager cannot be null");
    this.securityManager = securityManager;
  }

  protected SessionsSecurityManager getSecurityManager() {
    return securityManager;
  }

  protected Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  protected boolean isUserAuthenticated() {
    return SecurityUtils.getSubject().isAuthenticated();
  }

  /**
   * Returns true when resource method or class is annotated with {@link NotAuthenticated}, false otherwise.
   * @param method
   * @return
   */
  protected boolean isWebServicePublic(ResourceMethod method) {
    return method.getMethod().isAnnotationPresent(NotAuthenticated.class) || method.getResourceClass().isAnnotationPresent(NotAuthenticated.class);
  }

  /**
   * Returns true when resource method or class is annotated with {@link AuthenticatedByCookie}, false otherwise.
   * @param method
   * @return
   */
  protected boolean isWebServiceAuthenticatedByCookie(ResourceMethod method) {
    return method.getMethod().isAnnotationPresent(AuthenticatedByCookie.class) || method.getResourceClass().isAnnotationPresent(AuthenticatedByCookie.class);
  }

  protected boolean isValidSessionId(String sessionId) {
    return getSession(sessionId) != null;
  }

  protected Session getSession(String sessionId) {
    if(sessionId != null) {
      SessionKey key = new DefaultSessionKey(sessionId);
      try {
        return securityManager.getSessionManager().getSession(key);
      } catch(SessionException e) {
        // Means that the session does not exist or has expired.
      }
    }
    return null;
  }
}
