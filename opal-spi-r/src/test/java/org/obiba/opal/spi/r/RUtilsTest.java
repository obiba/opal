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
  public void testValidSymbol() {
    assertThat(RUtils.getValidSymbol("1000G_covarsView")).isEqualTo("table_1000G_covarsView");
    assertThat(RUtils.getValidSymbol("NA")).isEqualTo("table_NA");
    assertThat(RUtils.getValidSymbol("my-table (v1.2)")).isEqualTo("table_my_table__v1.2_");
  }

  @Test
  public void testNormalizeLabel() {
    assertThat(RUtils.normalizeLabel("I don't know")).isEqualTo("I don\\'t know");
    assertThat(RUtils.normalizeLabel("I don't know, I'm not sure")).isEqualTo("I don\\'t know, I\\'m not sure");
    assertThat(RUtils.normalizeLabel("Total BMD (g\\c)")).isEqualTo("Total BMD (g\\\\c)");
    assertThat(RUtils.normalizeLabel("I don't know\\nI'm not sure")).isEqualTo("I don\\'t know\\\\nI\\'m not sure");
  }
}
