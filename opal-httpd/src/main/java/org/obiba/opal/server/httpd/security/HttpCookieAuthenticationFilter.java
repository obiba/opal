/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.server.httpd.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.eclipse.jetty.http.HttpHeaders;
import org.obiba.opal.web.security.HttpCookieAuthenticationToken;
import org.obiba.opal.web.security.OpalAuth;
import org.springframework.web.util.WebUtils;

public class HttpCookieAuthenticationFilter extends AuthenticatingFilter {

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  private static final String OPAL_REQUEST_ID_COOKIE_NAME = "opalrid";

  @Override
  protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    Cookie sessionCookie = WebUtils.getCookie(httpRequest, OPAL_SESSION_ID_COOKIE_NAME);
    Cookie requestCookie = WebUtils.getCookie(httpRequest, OPAL_REQUEST_ID_COOKIE_NAME);
    if(isValid(sessionCookie) && isValid(requestCookie)) {
      return new HttpCookieAuthenticationToken(extractSessionId(httpRequest, sessionCookie),
          httpRequest.getRequestURI(), requestCookie.getValue());
    }
    return null;
  }

  @Override
  protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
    return true;
  }

  private boolean isValid(Cookie cookie) {
    return cookie != null && cookie.getValue() != null;
  }

  private String extractSessionId(HttpServletRequest request, Cookie sessionCookie) {
    String sessionId = request.getHeader(HttpHeaders.AUTHORIZATION);
    if(sessionId == null) sessionId = request.getHeader(OpalAuth.CREDENTIALS_HEADER);
    return sessionId == null ? sessionCookie.getValue() : sessionId;
  }

}
