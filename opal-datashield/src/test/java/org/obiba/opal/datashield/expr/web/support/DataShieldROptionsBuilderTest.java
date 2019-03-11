/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.expr.web.support;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.obiba.datashield.core.impl.DefaultDSOption;
import org.obiba.opal.web.datashield.support.DataShieldROptionsScriptBuilder;
import org.springframework.util.StringUtils;

import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataShieldROptionsBuilderTest {


  @Test
  public void test_validBooleanOptions() {
    testOption("TRUE");
    testOption("T");
    testOption("FALSE");
    testOption("F");
  }

  @Test
  public void test_validIntegers() {
    testOption("1");
    testOption("1234");
    testOption("0");
  }

  @Test
  public void test_validDecimals() {
    testOption("0.0");
    testOption("1.00");
    testOption("123.21");
  }

  @Test
  public void test_validNull() {
    testOption("NULL");
  }

  @Test
  public void test_validTexts() {
    testTextOption("Hello");
    testTextOption("12,23");
    testTextOption("Null");
    testTextOption("False");
    testTextOption("true");
    testTextOption("This is how you do it!");
    testTextOption("\"YoYo\"");
    testTextOption("t");
    testTextOption("null");
    testTextOption("");
    testOption(" ", "''");
    testTextOption("12.34px");
    testTextOption("12.34px");
    testTextOption("1, 122.34");
    testTextOption("50%");
    testTextOption("'50'");
  }

  @Test
  public void test_functionValues() {
    testOption("quote({dump.frames(to.file = TRUE); q()})");
    testOption("c(\"contr.helmert\", \"contr.poly\")");
  }

  @Test
  public void test_emptyOptions() {
    DataShieldROptionsScriptBuilder builder = DataShieldROptionsScriptBuilder.newBuilder()
        .setROptions(Lists.newArrayList());
    assertThat(builder.build()).isEqualTo("");
  }

  private void testOption(String value) {
    testOption(value, value);
  }

  private void testTextOption(String value) {
    testOption(value, StringUtils
        .quote(StringUtils.trimTrailingCharacter((StringUtils.trimLeadingCharacter(value, '\'')), '\'')));
  }

  private void testOption(String value, String expectedValue) {
    String expected = String.format("options(var=%s)", expectedValue);
    DataShieldROptionsScriptBuilder builder = DataShieldROptionsScriptBuilder.newBuilder()
        .setROptions(Collections.singletonList(new DefaultDSOption("var", value) {
        }));
    assertThat(builder.build()).isEqualTo(expected);
  }

}
