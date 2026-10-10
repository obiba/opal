/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.server.httpd;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import com.google.common.base.Strings;
import org.obiba.opal.web.security.OpalAuth;

/**
 * Session by header, for clients that cannot use the session cookie (browser apps calling Opal cross-site, where
 * SameSite cookies are not sent, or holding several sessions on the same Opal). A client opts in by sending the
 * {@link OpalAuth#SESSION_HEADER} header on every request, empty until it has a session:
 * <ul>
 *   <li>the header value replaces the request cookies with the session cookie, so authentication goes through the
 *   usual session cookie path;</li>
 *   <li>the session cookie set in the response is copied to the same header.</li>
 * </ul>
 * Requests without the header are left untouched.
 */
public class OpalSessionHeaderFilter extends HttpFilter {

  private static final String SESSION_ID_COOKIE_NAME = "opalsid";

  private static final String COOKIE_HEADER = "Cookie";

  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  @Override
  protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String sessionId = request.getHeader(OpalAuth.SESSION_HEADER);
    if (sessionId == null) {
      filterChain.doFilter(request, response);
      return;
    }
    filterChain.doFilter(new SessionRequest(request, sessionId.trim()), new SessionResponse(response));
  }

  private static class SessionRequest extends HttpServletRequestWrapper {

    private final String sessionId;

    private final String cookie;

    SessionRequest(HttpServletRequest request, String sessionId) {
      super(request);
      // ignore the browser cookies: the header is the session
      this.sessionId = Strings.emptyToNull(sessionId);
      this.cookie = this.sessionId == null ? null : SESSION_ID_COOKIE_NAME + "=" + sessionId;
    }

    @Override
    public Cookie[] getCookies() {
      return sessionId == null ? null : new Cookie[] { new Cookie(SESSION_ID_COOKIE_NAME, sessionId) };
    }

    @Override
    public String getHeader(String name) {
      return COOKIE_HEADER.equalsIgnoreCase(name) ? cookie : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if (!COOKIE_HEADER.equalsIgnoreCase(name)) return super.getHeaders(name);
      return cookie == null ? Collections.emptyEnumeration() : Collections.enumeration(Collections.singletonList(cookie));
    }
  }

  private static class SessionResponse extends HttpServletResponseWrapper {

    SessionResponse(HttpServletResponse response) {
      super(response);
    }

    @Override
    public void addHeader(String name, String value) {
      copySessionCookie(name, value);
      super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
      copySessionCookie(name, value);
      super.setHeader(name, value);
    }

    @Override
    public void addCookie(Cookie cookie) {
      if (SESSION_ID_COOKIE_NAME.equals(cookie.getName())) setSessionHeader(cookie.getValue());
      super.addCookie(cookie);
    }

    private void copySessionCookie(String name, String value) {
      if (!SET_COOKIE_HEADER.equalsIgnoreCase(name) || value == null || !value.startsWith(SESSION_ID_COOKIE_NAME + "=")) return;
      // opalsid=<id>;Version=1;Path=/;... or opalsid=;... when the session is deleted
      String cookieValue = value.substring(SESSION_ID_COOKIE_NAME.length() + 1).split(";", 2)[0];
      setSessionHeader(cookieValue);
    }

    private void setSessionHeader(String sessionId) {
      super.setHeader(OpalAuth.SESSION_HEADER, Strings.nullToEmpty(sessionId));
    }
  }

}
