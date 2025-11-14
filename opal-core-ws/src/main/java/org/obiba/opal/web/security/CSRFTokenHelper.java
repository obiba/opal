package org.obiba.opal.web.security;

import com.google.common.base.Strings;
import jakarta.ws.rs.core.NewCookie;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CSRFTokenHelper {

  public static final String CSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

  public static final String CSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

  public final String DEFAULT_CSRF_TOKEN;

  @Value("${org.obiba.opal.server.context-path}")
  private String contextPath;

  public CSRFTokenHelper() {
    DEFAULT_CSRF_TOKEN = UUID.randomUUID().toString();
  }

  public NewCookie createCsrfTokenCookie() {
    String csrfToken = generateCsrfToken();
    return new NewCookie.Builder(CSRF_TOKEN_COOKIE_NAME)
        .value(csrfToken)
        .path(getCookiePath())
        .maxAge(-1)
        .httpOnly(false)
        .secure(true)
        .sameSite(NewCookie.SameSite.LAX)
        .build();
  }

  public boolean validateXsrfToken(String headerToken) {
    Subject subject = ThreadContext.getSubject();
    if (subject == null) {
      // do not validate if no subject is associated with the request
      return true;
    }
    Session session = subject.getSession(false);
    if(session != null) {
      String sessionToken = (String) session.getAttribute(CSRF_TOKEN_COOKIE_NAME);
      return (Strings.isNullOrEmpty(headerToken) && Strings.isNullOrEmpty(sessionToken)) || (!Strings.isNullOrEmpty(headerToken) && headerToken.equals(sessionToken));
    }
    return DEFAULT_CSRF_TOKEN.equals(headerToken);
  }

  private String generateCsrfToken() {
    Subject subject = ThreadContext.getSubject();
    if (subject == null) {
      // return a default token if no subject is associated with the request
      return DEFAULT_CSRF_TOKEN;
    }
    Session session = subject.getSession();
    String csrfToken = (String) session.getAttribute(CSRF_TOKEN_COOKIE_NAME);
    if(csrfToken == null) {
      csrfToken = UUID.randomUUID().toString();
      session.setAttribute(CSRF_TOKEN_COOKIE_NAME, csrfToken);
    }
    return csrfToken;
  }

  private String getCookiePath() {
    return Strings.isNullOrEmpty(contextPath) ? "/" : contextPath;
  }
}
