/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpStatus;
import org.obiba.opal.core.service.security.X509CertificateAuthenticationToken;
import org.obiba.opal.web.security.OpalAuth;
import org.obiba.shiro.authc.HttpAuthorizationToken;
import org.obiba.shiro.authc.HttpCookieAuthenticationToken;
import org.obiba.shiro.authc.HttpHeaderAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

  private static final String OPAL_SESSION_ID_COOKIE_NAME = "opalsid";

  private static final String OPAL_REQUEST_ID_COOKIE_NAME = "opalrid";

  @Autowired
  private SessionsSecurityManager securityManager;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if(ThreadContext.getSubject() != null) {
      log.warn("Previous executing subject was not properly unbound from executing thread. Unbinding now.");
      ThreadContext.unbindSubject();
    }

    try {
      authenticateAndBind(request);
      filterChain.doFilter(request, response);
    } catch(AuthenticationException e) {
      response.setStatus(HttpStatus.UNAUTHORIZED_401);
    } catch(Exception e) {
      log.error("Exception", e);
      // see org.obiba.opal.web.magma.provider.UnhandledExceptionMapper
      response.setStatus(HttpStatus.BAD_REQUEST_400);
      response.getWriter().println(e.getMessage());
    } finally {
      unbind();
    }
  }

  /**
   * This method will try to authenticate the user using the provided sessionId or the "Authorization" header. When no
   * credentials are provided, this method does nothing. This will invoke the filter chain with an anonymous subject,
   * which allows fetching public web resources.
   *
   * @param request
   */
  private void authenticateAndBind(HttpServletRequest request) {

    Subject subject = authenticateSslCert(request);
    if(subject == null) {
      subject = authenticateOpalAuthHeader(request);
    }
    if(subject == null) {
      subject = authenticateBasicHeader(request);
    }
    if(subject == null) {
      subject = authenticateCookie(request);
    }

    if(subject != null) {
      Session session = subject.getSession();
      log.trace("Binding subject {} session {} to executing thread {}", subject.getPrincipal(), session.getId(),
          Thread.currentThread().getId());
      ThreadContext.bind(subject);
      session.touch();
      log.debug("Successfully authenticated subject {}", SecurityUtils.getSubject().getPrincipal());
    }
  }

  @Nullable
  private Subject authenticateSslCert(HttpServletRequest request) {
    X509Certificate[] chain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
    if(chain == null || chain.length == 0) return null;

    AuthenticationToken token = new X509CertificateAuthenticationToken(chain[0]);
    String sessionId = extractSessionId(request);
    Subject subject = new Subject.Builder(securityManager).sessionId(sessionId).buildSubject();
    subject.login(token);
    return subject;
  }

  @Nullable
  private Subject authenticateOpalAuthHeader(HttpServletRequest request) {
    String opalAuthToken = request.getHeader(OpalAuth.CREDENTIALS_HEADER);
    if(opalAuthToken == null || opalAuthToken.isEmpty()) return null;

    AuthenticationToken token = new HttpHeaderAuthenticationToken(opalAuthToken);
    Subject subject = new Subject.Builder(securityManager).sessionId(opalAuthToken).buildSubject();
    subject.login(token);
    return subject;
  }

  @Nullable
  private Subject authenticateBasicHeader(HttpServletRequest request) {
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if(authorization == null || authorization.isEmpty()) return null;

    String sessionId = extractSessionId(request);
    AuthenticationToken token = new HttpAuthorizationToken(OpalAuth.CREDENTIALS_HEADER, authorization);
    Subject subject = new Subject.Builder(securityManager).sessionId(sessionId).buildSubject();
    subject.login(token);
    return subject;
  }

  @Nullable
  private Subject authenticateCookie(HttpServletRequest request) {
    Cookie sessionCookie = WebUtils.getCookie(request, OPAL_SESSION_ID_COOKIE_NAME);
    Cookie requestCookie = WebUtils.getCookie(request, OPAL_REQUEST_ID_COOKIE_NAME);
    if(isValid(sessionCookie) && isValid(requestCookie)) {
      String sessionId = extractSessionId(request, sessionCookie);
      AuthenticationToken token = new HttpCookieAuthenticationToken(sessionId, request.getRequestURI(),
          requestCookie.getValue());
      Subject subject = new Subject.Builder(securityManager).sessionId(sessionId).buildSubject();
      subject.login(token);
      return subject;
    }
    return null;
  }

  private boolean isValid(Cookie cookie) {
    return cookie != null && cookie.getValue() != null;
  }

  private String extractSessionId(HttpServletRequest request) {
    return extractSessionId(request, null);
  }

  private String extractSessionId(HttpServletRequest request, @Nullable Cookie sessionCookie) {
    String sessionId = request.getHeader(OpalAuth.CREDENTIALS_HEADER);
    if(sessionId == null) {
      Cookie safeSessionCookie = sessionCookie == null
          ? WebUtils.getCookie(request, OPAL_SESSION_ID_COOKIE_NAME)
          : sessionCookie;
      if(safeSessionCookie != null) {
        sessionId = safeSessionCookie.getValue();
      }
    }
    return sessionId;
  }

  private void unbind() {
    try {
      if(log.isTraceEnabled()) {
        Subject s = ThreadContext.getSubject();
        if(s != null) {
          Session session = s.getSession(false);
          log.trace("Unbinding subject {} session {} from executing thread {}", s.getPrincipal(),
              session == null ? null : session.getId(), Thread.currentThread().getId());
        }
      }
    } finally {
      ThreadContext.unbindSubject();
    }
  }

}
