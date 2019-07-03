package org.obiba.opal.spi.r;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class RUtilsTest {

  @Test
  public void testLocale() {
    assertThat(RUtils.isLocaleValid("en")).isTrue();
    assertThat(RUtils.isLocaleValid("xx")).isFalse();
  }

  @Test
  public void testNormalizeLabel() {
    assertThat(RUtils.normalizeLabel("I don't know")).isEqualTo("I don\\'t know");
    assertThat(RUtils.normalizeLabel("I don't know, I'm not sure")).isEqualTo("I don\\'t know, I\\'m not sure");
    assertThat(RUtils.normalizeLabel("Total BMD (g\\c)")).isEqualTo("Total BMD (g\\\\c)");
    assertThat(RUtils.normalizeLabel("I don't know\\nI'm not sure")).isEqualTo("I don\\'t know\\\\nI\\'m not sure");
  }
}
