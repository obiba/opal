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

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.session.Session;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.obiba.opal.web.ws.intercept.RequestCyclePostProcess;
import org.obiba.opal.web.ws.intercept.RequestCyclePreProcess;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthenticationInterceptor extends AbstractSecurityComponent
    implements RequestCyclePreProcess, RequestCyclePostProcess {

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  @Value("${org.obiba.opal.server.context-path}")
  private String contextPath;

  @Autowired
  private CSRFTokenHelper csrfTokenHelper;

  @Override
  public void preProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext) {
    // Check authentication before processing.
    // If resource requires authentication and user is not authenticated, return "401: Unauthorized"

    // If we have an authenticated user, let method through
    if(isUserAuthenticated()) {
      return;
    }

    // If resource is public, let method through
    if(isWebServicePublic(resourceMethod)) {
      return;
    }

    // If method allows authentication using the cookie only and a valid cookie is present, let method through
    if(isWebServiceAuthenticatedByCookie(resourceMethod) && isOpalCookieValid(requestContext)) {
      return;
    }

    // Not authorized: method requires proper authentication, and no user is authenticated
    throw new UnauthorizedException();
  }

  @Override
  public void postProcess(HttpServletRequest httpServletRequest, ResourceMethodInvoker resourceMethod, ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    // Set the cookie if the user is still authenticated
    if (isUserAuthenticated()) {
      Session session = SecurityUtils.getSubject().getSession();
      session.touch();
      int timeout = (int) (session.getTimeout() / 1000);
      responseContext.getHeaders().add(HttpHeaders.SET_COOKIE,
          new NewCookie.Builder(OPAL_SESSION_ID_COOKIE_NAME)
              .value(session.getId().toString())
              .path(getCookiePath())
              .maxAge(timeout)
              .secure(true)
              .httpOnly(true)
              .sameSite(NewCookie.SameSite.LAX)
              .build());
      Object cookieValue = session.getAttribute(HttpHeaders.SET_COOKIE);
      if(cookieValue != null) {
        responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, RuntimeDelegate.getInstance()
            .createHeaderDelegate(NewCookie.class)
            .fromString(cookieValue.toString()));
      }
      NewCookie csrfCookie = csrfTokenHelper.createCsrfTokenCookie();
      if (csrfCookie != null)
        responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, csrfCookie);
    } else {
      // Remove the cookie if the user is not/no longer authenticated
      if(resourceMethod == null || isWebServiceAuthenticated(resourceMethod.getMethod().getAnnotations())) {
        // Only web service calls that require authentication will lose their opalsid cookie
        responseContext.getHeaders().add(HttpHeaders.SET_COOKIE,
            new NewCookie.Builder(OPAL_SESSION_ID_COOKIE_NAME)
                .value(null)
                .path(getCookiePath())
                .comment("Opal session deleted")
                .maxAge(0)
                .secure(true)
                .httpOnly(true)
                .sameSite(NewCookie.SameSite.LAX)
                .build());
        responseContext.getHeaders().add(HttpHeaders.SET_COOKIE, csrfTokenHelper.deleteCsrfTokenCookie());
      }
    }
  }

  private String getCookiePath() {
    return Strings.isNullOrEmpty(contextPath) ? "/" : contextPath;
  }

  private boolean isOpalCookieValid(ContainerRequestContext requestContext) {
    Cookie cookie = requestContext.getCookies().get(OPAL_SESSION_ID_COOKIE_NAME);
    return cookie != null && isValidSessionId(cookie.getValue());
  }

  @SuppressWarnings("ChainOfInstanceofChecks")
  private boolean isWebServiceAuthenticated(@Nullable Annotation... annotations) {
    if(annotations != null) {
      for(Annotation annotation : annotations) {
        if(annotation instanceof NotAuthenticated) return false;
        if(annotation instanceof AuthenticatedByCookie) return false;
      }
    }
    return true;
  }

}
