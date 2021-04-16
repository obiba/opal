/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.tools;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class LuhnValidatorTest {

  @Test
  public void test_validate_success() {
    assertThat(LuhnValidator.validate("4916284958948122")).isTrue();
  }

  @Test
  public void test_validate_failure() {
    assertThat(LuhnValidator.validate("1496284958948122")).isFalse();
  }

  @Test
  public void test_validate_alpha() {
    assertThat(LuhnValidator.validate("XX96284958948122")).isFalse();
  }

  @Test
  public void test_validate_null() {
    assertThat(LuhnValidator.validate(null)).isFalse();
  }

  @Test
  public void test_validate_empty() {
    assertThat(LuhnValidator.validate("")).isFalse();
  }

  @Test
  public void test_validate_trim() {
    assertThat(LuhnValidator.validate("  4916284958948122  ")).isTrue();
  }

  @Test
  public void test_validate_length() {
    assertThat(LuhnValidator.validate("4929750633404868", 16)).isTrue();
  }

  @Test
  public void test_validate_zeros() {
    assertThat(LuhnValidator.validate("0000000000000")).isFalse();
  }

}
