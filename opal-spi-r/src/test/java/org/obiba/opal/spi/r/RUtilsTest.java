package org.obiba.opal.spi.r;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class RUtilsTest {

  @Test
  public void testLocale() {
    assertThat(RUtils.isLocaleValid("en")).isTrue();
    assertThat(RUtils.isLocaleValid("xx")).isFalse();
  }
}
