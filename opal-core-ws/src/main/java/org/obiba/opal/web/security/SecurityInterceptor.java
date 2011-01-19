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

import java.lang.annotation.Annotation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.obiba.opal.web.ws.security.AuthenticatedByCookie;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ServerInterceptor
public class SecurityInterceptor extends AbstractSecurityComponent implements PreProcessInterceptor, PostProcessInterceptor {

  private static final Logger log = LoggerFactory.getLogger(SecurityInterceptor.class);

  private static final String X_OPAL_AUTH = "X-Opal-Auth";

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  @Autowired
  public SecurityInterceptor(SessionsSecurityManager securityManager) {
    super(securityManager);
  }

  @Override
  public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
    // Check authentication before processing. If resource requires authentication and user is not authenticated, return
    // "401: Unauthorized"

    // If we have an authenticated user, let method through
    if(isUserAuthenticated()) {
      return null;
    }

    // If resource is public, let method through
    if(isWebServicePublic(method)) {
      return null;
    }

    // If method allows authentication using the cookie only and a valid cookie is present
    // let method through
    if(isWebServiceAuthenticatedByCookie(method) && isOpalCookieValid(request)) {
      return null;
    }

    // Not authorized: method requires proper authentication, and no user is authenticated
    return (ServerResponse) ServerResponse.status(Status.UNAUTHORIZED).header(HttpHeaders.WWW_AUTHENTICATE, X_OPAL_AUTH + " realm=\"Opal\"").build();
  }

  @Override
  public void postProcess(ServerResponse response) {
    // Set the cookie if the user is still authenticated
    if(isUserAuthenticated()) {
      Session session = SecurityUtils.getSubject().getSession();
      session.touch();
      int timeout = (int) (session.getTimeout() / 1000);
      response.getMetadata().add(HttpHeaderNames.SET_COOKIE, new NewCookie(OPAL_SESSION_ID_COOKIE_NAME, session.getId().toString(), "/", null, null, timeout, false));
    } else {
      // Remove the cookie if the user is not/no longer authenticated
      if(isWebServiceAuthenticated(response.getAnnotations())) {
        // Only web service calls that require authentication will lose their opalsid cookie
        response.getMetadata().add(HttpHeaderNames.SET_COOKIE, new NewCookie(OPAL_SESSION_ID_COOKIE_NAME, null, "/", null, "Opal session deleted", 0, false));
      }
    }
  }

  private boolean isUserAuthenticated() {
    return SecurityUtils.getSubject().isAuthenticated();
  }

  /**
   * Returns true when resource method or class is annotated with {@link NotAuthenticated}, false otherwise.
   * @param method
   * @return
   */
  private boolean isWebServicePublic(ResourceMethod method) {
    return method.getMethod().isAnnotationPresent(NotAuthenticated.class) || method.getResourceClass().isAnnotationPresent(NotAuthenticated.class);
  }

  /**
   * Returns true when resource method or class is annotated with {@link AuthenticatedByCookie}, false otherwise.
   * @param method
   * @return
   */
  private boolean isWebServiceAuthenticatedByCookie(ResourceMethod method) {
    return method.getMethod().isAnnotationPresent(AuthenticatedByCookie.class) || method.getResourceClass().isAnnotationPresent(AuthenticatedByCookie.class);
  }

  private boolean isOpalCookieValid(HttpRequest request) {
    Cookie cookie = request.getHttpHeaders().getCookies().get(OPAL_SESSION_ID_COOKIE_NAME);
    if(cookie != null) {
      return isValidSessionId(cookie.getValue());
    }
    return false;
  }

  private boolean isWebServiceAuthenticated(Annotation... annotations) {
    for(Annotation annotation : annotations) {
      if(annotation instanceof NotAuthenticated) return false;
      if(annotation instanceof AuthenticatedByCookie) return false;
    }
    return true;
  }

}
