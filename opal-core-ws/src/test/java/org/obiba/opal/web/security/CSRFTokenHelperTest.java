package org.obiba.opal.web.security;

import jakarta.ws.rs.core.NewCookie;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.junit.Test;


import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class CSRFTokenHelperTest {
  @Test
  public void createCsrfTokenCookieReturnsNullWhenNoSubject() {
    ThreadContext.unbindSubject();
    NewCookie cookie = new CSRFTokenHelper().createCsrfTokenCookie();
    assertThat(cookie).isNull();
  }

  @Test
  public void createCsrfTokenCookieGeneratesNewTokenWhenSessionHasNoToken() {
    Session mockSession = createMock(Session.class);
    expect(mockSession.getAttribute(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME)).andReturn(null);
    mockSession.setAttribute(EasyMock.eq(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME), EasyMock.anyString());
    replay(mockSession);

    Subject mockSubject = createMock(Subject.class);
    expect(mockSubject.getSession()).andReturn(mockSession);
    ThreadContext.bind(mockSubject);
    replay(mockSubject);

    NewCookie cookie = new CSRFTokenHelper().createCsrfTokenCookie();
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME);
    assertThat(cookie.getValue()).isNotEmpty();
    assertThat(cookie.getPath()).isEqualTo("/");
    assertThat(cookie.isSecure()).isTrue();
    assertThat(cookie.isHttpOnly()).isFalse();

    verify(mockSession, mockSubject);
  }

  @Test
  public void deleteCsrfTokenCookieReturnsCookieWithMaxAgeZero() {
    NewCookie cookie = new CSRFTokenHelper().deleteCsrfTokenCookie();
    assertThat(cookie).isNotNull();
    assertThat(cookie.getName()).isEqualTo(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME);
    assertThat(cookie.getValue()).isNull();
    assertThat(cookie.getMaxAge()).isEqualTo(0);
    assertThat(cookie.getPath()).isEqualTo("/");
    assertThat(cookie.isSecure()).isTrue();
    assertThat(cookie.isHttpOnly()).isFalse();
  }

  @Test
  public void validateXsrfTokenReturnsTrueWhenNoSubject() {
    ThreadContext.unbindSubject();
    boolean isValid = new CSRFTokenHelper().validateXsrfToken("any-token");
    assertThat(isValid).isTrue();
  }

  @Test
  public void validateXsrfTokenReturnsTrueWhenSessionTokenIsNull() {
    Session mockSession = createMock(Session.class);
    expect(mockSession.getAttribute(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME)).andReturn(null);
    replay(mockSession);

    Subject mockSubject = createMock(Subject.class);
    expect(mockSubject.getSession(false)).andReturn(mockSession);
    ThreadContext.bind(mockSubject);
    replay(mockSubject);

    boolean isValid = new CSRFTokenHelper().validateXsrfToken("any-token");
    assertThat(isValid).isTrue();

    verify(mockSession, mockSubject);
  }

  @Test
  public void validateXsrfTokenReturnsTrueWhenHeaderTokenMatchesSessionToken() {
    Session mockSession = createMock(Session.class);
    expect(mockSession.getAttribute(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME)).andReturn("valid-token");
    replay(mockSession);

    Subject mockSubject = createMock(Subject.class);
    expect(mockSubject.getSession(false)).andReturn(mockSession);
    ThreadContext.bind(mockSubject);
    replay(mockSubject);

    boolean isValid = new CSRFTokenHelper().validateXsrfToken("valid-token");
    assertThat(isValid).isTrue();

    verify(mockSession, mockSubject);
  }

  @Test
  public void validateXsrfTokenReturnsFalseWhenHeaderTokenDoesNotMatchSessionToken() {
    Session mockSession = createMock(Session.class);
    expect(mockSession.getAttribute(CSRFTokenHelper.CSRF_TOKEN_COOKIE_NAME)).andReturn("valid-token");
    replay(mockSession);

    Subject mockSubject = createMock(Subject.class);
    expect(mockSubject.getSession(false)).andReturn(mockSession);
    ThreadContext.bind(mockSubject);
    replay(mockSubject);

    boolean isValid = new CSRFTokenHelper().validateXsrfToken("invalid-token");
    assertThat(isValid).isFalse();

    verify(mockSession, mockSubject);
  }

}
