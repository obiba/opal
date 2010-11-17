/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.opal.web.security.HttpAuthorizationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

  private static final String X_OPAL_AUTH = "X-Opal-Auth";

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  private final SessionsSecurityManager securityManager;

  public AuthenticationFilter(final SecurityManager mgr) {
    if(mgr instanceof SessionsSecurityManager == false) {
      throw new IllegalStateException("SecurityManager does not support session management");
    }
    this.securityManager = (SessionsSecurityManager) mgr;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    if(ThreadContext.getSubject() != null) {
      log.warn("Previous executing subject was not properly unbound from executing thread. Unbinding now.");
      ThreadContext.unbindSubject();
    }

    try {
      authenticateAndBind(request);
      filterChain.doFilter(request, response);
    } finally {
      unbind();
    }
  }

  protected SessionsSecurityManager getSecurityManager() {
    return securityManager;
  }

  protected boolean isValidSessionId(String sessionId) {
    if(sessionId != null) {
      SessionKey key = new DefaultSessionKey(sessionId);
      try {
        return securityManager.getSessionManager().getSession(key) != null;
      } catch(SessionException e) {
        // Means that the session does not exist or has expired.
      }
    }
    return false;
  }

  /**
   * This method will try to authenticate the user using the provided sessionId or the "Authorization" header. When no
   * credentials are provided, this method does nothing. This will invoke the filter chain with an anonymous subject,
   * which allows fetching public web resources.
   * 
   * @param request
   */
  private void authenticateAndBind(HttpServletRequest request) {
    String sessionId = extractSessionId(request);
    String authorization = request.getHeader("Authorization");
    if(isValidSessionId(sessionId)) {
      Subject s = new Subject.Builder(getSecurityManager()).sessionId(sessionId).authenticated(true).buildSubject();
      s.getSession().touch();
      log.debug("Binding subject {} session {} to executing thread {}", new Object[] { s.getPrincipal(), sessionId, Thread.currentThread().getId() });
      ThreadContext.bind(s);
    } else if(authorization != null) {
      HttpAuthorizationToken token = new HttpAuthorizationToken(X_OPAL_AUTH, authorization);
      SecurityUtils.getSubject().login(token);
      sessionId = SecurityUtils.getSubject().getSession().getId().toString();
      log.info("Successfull session creation for user '{}' session ID is '{}'.", token.getUsername(), sessionId);
    }
  }

  private void unbind() {
    try {
      if(log.isDebugEnabled()) {
        Subject s = ThreadContext.getSubject();
        if(s != null) {
          Session session = s.getSession(false);
          log.debug("Unbinding subject {} session {} from executing thread {}", new Object[] { s.getPrincipal(), (session != null ? session.getId() : "null"), Thread.currentThread().getId() });
        }
      }
    } finally {
      ThreadContext.unbindSubject();
    }
  }

  private String extractSessionId(HttpServletRequest request) {
    String sessionId = request.getHeader(X_OPAL_AUTH);
    if(sessionId == null && (request.getMethod().equalsIgnoreCase("GET") || request.getMethod().equalsIgnoreCase("POST"))) {
      // Extract from the cookie (only used for GET or POST requests)
      Cookie cookie = findCookie(request, OPAL_SESSION_ID_COOKIE_NAME);
      if(cookie != null) {
        sessionId = cookie.getValue();
      }
    }
    return sessionId;
  }

  private Cookie findCookie(HttpServletRequest request, String cookieName) {
    Cookie[] cookies = request.getCookies();
    if(cookies != null) {
      for(Cookie cookie : cookies) {
        if(cookie.getName().equals(cookieName)) {
          return cookie;
        }
      }
    }
    return null;
  }

}
