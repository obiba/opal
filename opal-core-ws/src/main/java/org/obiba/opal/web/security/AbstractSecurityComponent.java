/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.security;

import jakarta.annotation.Nullable;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;

abstract class AbstractSecurityComponent {

  protected SessionsSecurityManager securityManager;

  @Autowired
  void setSecurityManager(SessionsSecurityManager securityManager) {
    this.securityManager = securityManager;
  }

  static Subject getSubject() {
    return SecurityUtils.getSubject();
  }

  @Nullable
  static String getPrincipal() {
    Object principal = getSubject().getPrincipal();
    return principal == null ? null : principal.toString();
  }

  static boolean isUserAuthenticated() {
    Subject subject = ThreadContext.getSubject();
    return subject != null && subject.isAuthenticated();
  }

  /**
   * Returns true when resource method or class is annotated with {@link NotAuthenticated}, false otherwise.
   *
   * @param method
   * @return
   */
  boolean isWebServicePublic(ResourceMethodInvoker method) {
    return method.getMethod().isAnnotationPresent(NotAuthenticated.class) ||
        method.getResourceClass().isAnnotationPresent(NotAuthenticated.class);
  }

  /**
   * Returns true when resource method or class is annotated with {@link NoAuthorization}, false otherwise.
   *
   * @param method
   * @return
   */
  boolean isWebServiceWithoutAuthorization(ResourceMethodInvoker method) {
    return method.getMethod().isAnnotationPresent(NoAuthorization.class) ||
        method.getResourceClass().isAnnotationPresent(NoAuthorization.class);
  }

  /**
   * Returns true when resource method or class is annotated with {@link AuthenticatedByCookie}, false otherwise.
   *
   * @param method
   * @return
   */
  boolean isWebServiceAuthenticatedByCookie(ResourceMethodInvoker method) {
    return method.getMethod().isAnnotationPresent(AuthenticatedByCookie.class) ||
        method.getResourceClass().isAnnotationPresent(AuthenticatedByCookie.class);
  }

  boolean isValidSessionId(String sessionId) {
    return getSession(sessionId) != null;
  }

  @Nullable
  Session getSession(String sessionId) {
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
