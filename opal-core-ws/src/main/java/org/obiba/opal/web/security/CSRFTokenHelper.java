package org.obiba.opal.web.security;

import com.google.common.base.Strings;
import jakarta.ws.rs.core.NewCookie;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper class to manage CSRF tokens.
 */
@Component
public class CSRFTokenHelper {

  public static final String CSRF_TOKEN_HEADER = "X-XSRF-TOKEN";

  public static final String CSRF_TOKEN_COOKIE_NAME = "XSRF-TOKEN";

  @Value("${org.obiba.opal.server.context-path}")
  private String contextPath;

  /**
   * Create a new CSRF token cookie. If a token is already associated with the current user's session, it is reused.
   * @return a NewCookie containing the CSRF token, or null if no subject is authenticated
   */
  public NewCookie createCsrfTokenCookie() {
    String csrfToken = getOrGenerateCsrfToken();
    if (csrfToken == null) {
      return null;
    }
    return new NewCookie.Builder(CSRF_TOKEN_COOKIE_NAME)
        .value(csrfToken)
        .path(getCookiePath())
        .maxAge(-1)
        .httpOnly(false)
        .secure(true)
        .sameSite(NewCookie.SameSite.LAX)
        .build();
  }

  /**
   * Create a CSRF token cookie that will delete the cookie from the client.
   * @return a NewCookie that will delete the CSRF token cookie from the client
   */
  public NewCookie deleteCsrfTokenCookie() {
    return new NewCookie.Builder(CSRF_TOKEN_COOKIE_NAME)
        .value(null)
        .comment("Opal session deleted")
        .path(getCookiePath())
        .maxAge(0)
        .httpOnly(false)
        .secure(true)
        .sameSite(NewCookie.SameSite.LAX)
        .build();
  }

  /**
   * Validate the XSRF token sent in the request header against the token stored in the user's session.
   * If no subject is associated with the request, validation is skipped.
   * @param headerToken
   * @return true if the token is valid or no subject is associated with the request, false otherwise
   */
  public boolean validateXsrfToken(String headerToken) {
    Subject subject = ThreadContext.getSubject();
    if (subject == null) {
      // do not validate if no subject is associated with the request
      return true;
    }
    Session session = subject.getSession(false);
    if (session != null) {
      String sessionToken = (String) session.getAttribute(CSRF_TOKEN_COOKIE_NAME);
      if (Strings.isNullOrEmpty(sessionToken)) {
        return true; // No token in session yet, skip validation
      }
      return !Strings.isNullOrEmpty(headerToken) && headerToken.equals(sessionToken);
    }
    return true;
  }

  //
  // Private methods
  //

  private String getOrGenerateCsrfToken() {
    Subject subject = ThreadContext.getSubject();
    if (subject == null) {
      // return a default token if no subject is associated with the request
      return null;
    }
    Session session = subject.getSession();
    String csrfToken = (String) session.getAttribute(CSRF_TOKEN_COOKIE_NAME);
    if (csrfToken == null) {
      csrfToken = UUID.randomUUID().toString();
      session.setAttribute(CSRF_TOKEN_COOKIE_NAME, csrfToken);
    }
    return csrfToken;
  }

  private String getCookiePath() {
    return Strings.isNullOrEmpty(contextPath) ? "/" : contextPath;
  }
}
