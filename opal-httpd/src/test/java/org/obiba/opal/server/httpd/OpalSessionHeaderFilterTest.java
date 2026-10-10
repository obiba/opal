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

import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OpalSessionHeaderFilterTest {

  private final OpalSessionHeaderFilter filter = new OpalSessionHeaderFilter();

  @Test
  public void test_header_replaces_cookies_and_session_cookie_is_copied_to_header() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/system/version");
    request.addHeader("X-Opal-Session", "abc");
    request.setCookies(new Cookie("opalsid", "browser"), new Cookie("other", "x"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<HttpServletRequest> seen = new AtomicReference<>();

    filter.doFilter(request, response, (req, res) -> {
      seen.set((HttpServletRequest) req);
      ((HttpServletResponse) res).addHeader("Set-Cookie", "opalsid=def;Version=1;Path=/;HttpOnly");
    });

    assertEquals(1, seen.get().getCookies().length);
    assertEquals("abc", seen.get().getCookies()[0].getValue());
    assertEquals("opalsid=abc", seen.get().getHeader("Cookie"));
    assertEquals("def", response.getHeader("X-Opal-Session"));
    assertEquals(Boolean.TRUE, seen.get().getAttribute("org.obiba.opal.sessionHeader"));
  }

  @Test
  public void test_empty_header_means_no_session_and_deleted_session_gives_empty_header() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/system/version");
    request.addHeader("X-Opal-Session", "");
    request.setCookies(new Cookie("opalsid", "browser"));
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicReference<HttpServletRequest> seen = new AtomicReference<>();

    filter.doFilter(request, response, (req, res) -> {
      seen.set((HttpServletRequest) req);
      ((HttpServletResponse) res).addHeader("Set-Cookie", "opalsid=;Version=1;Path=/;Max-Age=0");
    });

    assertNull(seen.get().getCookies());
    assertNull(seen.get().getHeader("Cookie"));
    assertEquals("", response.getHeader("X-Opal-Session"));
  }

  @Test
  public void test_requests_without_header_are_untouched() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/ws/system/version");
    request.setCookies(new Cookie("opalsid", "browser"));
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, (req, res) ->
        ((HttpServletResponse) res).addHeader("Set-Cookie", "opalsid=def;Version=1;Path=/"));

    assertNull(response.getHeader("X-Opal-Session"));
    assertNull(request.getAttribute("org.obiba.opal.sessionHeader"));
  }

}
