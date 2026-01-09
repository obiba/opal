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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.annotation.Nullable;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.util.List;
import java.util.Objects;

abstract class AbstractSecurityComponent {

  protected SessionsSecurityManager securityManager;

  /**
   * Re-authentication timeout in seconds.
   */
  @Value("${org.obiba.opal.security.login.reAuth.timeout}")
  private int reAuthTimeout;

  @Value("${org.obiba.opal.security.login.reAuth.endpoints}")
  private String reAuthEndpoints;

  private List<Endpoint> reAuthEndpointsList;

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
   * Returns true when request method and path are identified as being critical
   * and the elapsed time since session start is greater than the timeout defined
   * in the configuration, false otherwise.
   *
   * @param request
   * @return
   */
  boolean needsReauthenticateSubject(HttpServletRequest request) {
    if (reAuthTimeout <= 0) {
      return false;
    }
    Subject subject = getSubject();
    if (subject == null || !subject.isAuthenticated()) {
      return false;
    }
    Session session = subject.getSession(false);
    if (session == null) {
      return false;
    }
    boolean isCritical = getReAuthEndpointsList().stream().anyMatch((endpoint) -> endpoint.appliesTo(request));
    if (!isCritical) return false;
    Date startDate = session.getStartTimestamp();
    long now = System.currentTimeMillis();
    long elapsed = now - startDate.getTime();
    long timeoutMillis = reAuthTimeout * 1000L;
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
        method.getResourceClass().isAnnotationPresent(NotAuthenticated.class) ||
        method.getResourceClass() == OpenApiResource.class;
  }

  /**
   * Returns true when resource method or class is annotated with {@link NoAuthorization}, false otherwise.
   *
   * @param method
   * @return
   */
  boolean isWebServiceWithoutAuthorization(ResourceMethodInvoker method) {
    return method.getMethod().isAnnotationPresent(NoAuthorization.class) ||
        method.getResourceClass().isAnnotationPresent(NoAuthorization.class) ||
        method.getResourceClass() == OpenApiResource.class;
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

  private List<Endpoint> getReAuthEndpointsList() {
    if (reAuthEndpointsList == null) {
      if (reAuthEndpoints == null || reAuthEndpoints.isEmpty()) {
        reAuthEndpointsList = Lists.newArrayList();
      } else {
        reAuthEndpointsList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(reAuthEndpoints).stream()
            .map((key) -> {
              String[] parts = key.split(":");
              if (parts.length == 2) {
                String method = parts[0];
                String path = parts[1];
                return new Endpoint(method.toUpperCase(), path);
              }
              return null;
            }).filter(Objects::nonNull).toList();
      }
    }
    return reAuthEndpointsList;
  }

  record Endpoint(String method, String path) {

    public boolean appliesTo(HttpServletRequest request) {
      String requestPath = request.getPathInfo();
      return appliesTo(request.getMethod(), requestPath);
    }

    public boolean appliesTo(String requestMethod, String requestPath) {
      if (!requestMethod.equals(this.method)) return false;
      if (this.path.equals(requestPath)) return true;
      // check for wildcards '*'
      if (this.path.contains("/*")) {
        String[] patternParts = this.path.split("/");
        String[] requestParts = requestPath.split("/");
        if (patternParts.length != requestParts.length) return false;
        for (int i = 0; i < patternParts.length; i++) {
          if (patternParts[i].equals("*") && requestParts[i].isEmpty()) {
            return false;
          }
          if (!patternParts[i].equals("*") && !patternParts[i].equals(requestParts[i])) {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  }
}
