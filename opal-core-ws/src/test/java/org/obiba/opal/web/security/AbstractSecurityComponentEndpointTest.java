package org.obiba.opal.web.security;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class AbstractSecurityComponentEndpointTest {

  @Test
  public void testExactMatch() {
    AbstractSecurityComponent.Endpoint endpoint =
        new AbstractSecurityComponent.Endpoint("PUT", "/system/subject-credential/_current/password");
    assertThat( endpoint.appliesTo("PUT", "/system/subject-credential/_current/password")).isTrue();
    assertThat( endpoint.appliesTo("PUT", "/system/subject-credential/toto/password")).isFalse();
    assertThat( endpoint.appliesTo("DELETE", "/system/subject-credential/_current/password")).isFalse();
  }

  @Test
  public void testWildcardMatch() {
    AbstractSecurityComponent.Endpoint endpoint =
        new AbstractSecurityComponent.Endpoint("PUT", "/system/subject-credential/*");
    assertThat( endpoint.appliesTo("PUT", "/system/subject-credential/toto")).isTrue();
    assertThat( endpoint.appliesTo("DELETE", "/system/subject-credential/toto")).isFalse();
    assertThat( endpoint.appliesTo("POST", "/system/subject-credentials")).isFalse();
  }
}
