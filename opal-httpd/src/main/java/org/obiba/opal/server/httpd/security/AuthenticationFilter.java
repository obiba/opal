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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.cert.X509Certificate;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.obiba.opal.core.unit.security.X509CertificateAuthenticationToken;
import org.obiba.opal.web.security.HttpAuthorizationToken;
import org.obiba.opal.web.security.HttpCookieAuthenticationToken;
import org.obiba.opal.web.security.HttpHeaderAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

  private static final String X_OPAL_AUTH = "X-Opal-Auth";

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  private static final String OPAL_REQUEST_ID_COOKIE_NAME = "opalrid";

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
    } catch(AuthenticationException e) {
      response.setStatus(HttpStatus.UNAUTHORIZED_401);
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

    Subject subject = null;

    if(hasSslCert(request)) {
      subject = authenticateBySslCert(request);
    }

    if(subject == null && hasOpalAuthHeader(request)) {
      subject = authenticateByOpalAuthHeader(request);
    }

    if(subject == null && hasAuthorizationHeader(request)) {
      subject = authenticateByAuthorizationHeader(request);
    }

    if(subject == null && hasOpalSessionCookie(request) && hasOpalRequestCookie(request)) {
      subject = authenticateByCookie(request);
    }

    if(subject != null) {
      Session session = subject.getSession();
      log.debug("Binding subject {} session {} to executing thread {}", new Object[] { subject.getPrincipal(), session.getId(), Thread.currentThread().getId() });
      session.touch();
      return;
    }

  }

  private Subject authenticateBySslCert(HttpServletRequest request) {
    X509Certificate[] chain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    for(X509Certificate cert : chain) {
      X509CertificateAuthenticationToken token = new X509CertificateAuthenticationToken(cert);
      String sessionId = extractSessionId(request);
      Subject subject = new Subject.Builder(getSecurityManager()).sessionId(sessionId).buildSubject();
      subject.login(token);
      log.info("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
      return subject;
    }
    return null;
  }

  private boolean hasSslCert(HttpServletRequest request) {
    X509Certificate[] chain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    return chain != null && chain.length > 0;
  }

  private Subject authenticateByOpalAuthHeader(HttpServletRequest request) {
    String opalAuthToken = getOpalAuthToken(request);
    HttpHeaderAuthenticationToken token = new HttpHeaderAuthenticationToken(opalAuthToken);
    Subject subject = new Subject.Builder(getSecurityManager()).sessionId(opalAuthToken).buildSubject();
    subject.login(token);
    return subject;
  }

  private Subject authenticateByAuthorizationHeader(HttpServletRequest request) {
    String authorization = getAuthorizationHeader(request);
    String sessionId = extractSessionId(request);

    HttpAuthorizationToken token = new HttpAuthorizationToken(X_OPAL_AUTH, authorization);
    Subject subject = new Subject.Builder(getSecurityManager()).sessionId(sessionId).buildSubject();
    subject.login(token);
    return subject;
  }

  private Subject authenticateByCookie(HttpServletRequest request) {
    String sessionId = extractSessionId(request);
    String requestId = extractRequestId(request);
    String url = request.getRequestURI();
    if(request.getQueryString() != null) {
      try {
        String query = URLDecoder.decode(request.getQueryString().replace("+", "%2B"), "UTF-8").replace("%2B", "+");
        url = url + "?" + query;
      } catch(UnsupportedEncodingException e) {
      }
    }
    HttpCookieAuthenticationToken token = new HttpCookieAuthenticationToken(sessionId, url, requestId);
    Subject subject = new Subject.Builder(getSecurityManager()).sessionId(sessionId).buildSubject();
    subject.login(token);
    return subject;
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

  private boolean hasOpalAuthHeader(HttpServletRequest request) {
    String header = getOpalAuthToken(request);
    return header != null && header.isEmpty() == false;
  }

  private String getOpalAuthToken(HttpServletRequest request) {
    return request.getHeader(X_OPAL_AUTH);
  }

  private boolean hasOpalSessionCookie(HttpServletRequest request) {
    Cookie cookie = findCookie(request, OPAL_SESSION_ID_COOKIE_NAME);
    return cookie != null && cookie.getValue() != null;
  }

  private boolean hasAuthorizationHeader(HttpServletRequest request) {
    String header = getAuthorizationHeader(request);
    return header != null && header.isEmpty() == false;
  }

  private String getAuthorizationHeader(HttpServletRequest request) {
    return request.getHeader(HttpHeaders.AUTHORIZATION);
  }

  private String extractSessionId(HttpServletRequest request) {
    String sessionId = request.getHeader(X_OPAL_AUTH);
    if(sessionId == null) {
      // Extract from the cookie (only used for GET or POST requests)
      Cookie cookie = findCookie(request, OPAL_SESSION_ID_COOKIE_NAME);
      if(cookie != null) {
        sessionId = cookie.getValue();
      }
    }
    return sessionId;
  }

  private boolean hasOpalRequestCookie(HttpServletRequest request) {
    Cookie cookie = findCookie(request, OPAL_REQUEST_ID_COOKIE_NAME);
    return cookie != null && cookie.getValue() != null;
  }

  private String extractRequestId(HttpServletRequest request) {
    Cookie cookie = findCookie(request, OPAL_REQUEST_ID_COOKIE_NAME);
    if(cookie != null) {
      return cookie.getValue();
    }
    return null;
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
