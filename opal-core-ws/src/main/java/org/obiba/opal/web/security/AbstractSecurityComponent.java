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
import org.apache.shiro.session.InvalidSessionException;
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
import org.obiba.opal.web.ws.security.ReAuthenticate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

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
   * Returns true when resource method or class is annotated with {@link ReAuthenticate} and the elapsed time since
   * session start is greater than the timeout defined in the annotation, false otherwise.
   *
   * @param method
   * @return
   */
  boolean needsReauthenticateSubject(ResourceMethodInvoker method) {
    boolean hasAnnotation = method.getMethod().isAnnotationPresent(ReAuthenticate.class) ||
        method.getResourceClass().isAnnotationPresent(ReAuthenticate.class);
    if (!hasAnnotation) return false;
    Subject subject = getSubject();
    if (subject == null || !subject.isAuthenticated()) {
      return false;
    }
    Session session = subject.getSession(false);
    if (session == null) {
      return false;
    }
    ReAuthenticate reAuth = method.getMethod().getAnnotation(ReAuthenticate.class);
    if (reAuth == null) {
      reAuth = method.getResourceClass().getAnnotation(ReAuthenticate.class);
    }
    if (reAuth == null) return false;
    Date startDate = session.getStartTimestamp();
    long now = System.currentTimeMillis();
    long elapsed = now - startDate.getTime();
    long timeoutMillis = reAuth.timeoutSeconds() * 1000L;
    return elapsed >= timeoutMillis;
  }

  void invalidateSession() {
    Subject subject = getSubject();
    if(subject != null) {
      try {
        Session session = subject.getSession(false);
        if(session != null) {
          session.stop();
        }
        subject.logout();
      } catch(InvalidSessionException e) {
        // Session is already stopped/invalidated.
      }
    }
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
