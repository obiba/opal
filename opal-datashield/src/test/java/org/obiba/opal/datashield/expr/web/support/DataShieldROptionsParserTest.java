/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.expr.web.support;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.obiba.opal.web.datashield.support.DataShieldROptionsParser;
import org.obiba.opal.web.model.DataShield;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataShieldROptionsParserTest {

  private static DataShieldROptionsParser parser;

  @BeforeClass
  public static void createParser() {
    parser = new DataShieldROptionsParser();
  }

  @Test
  public void test_stringOption() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro=\"Zorro es un chaval muy simpatico\"");
    DataShield.DataShieldROptionDto dto = options.get(0);
    assertThat(dto.getName()).isEqualTo("zorro");
    assertThat(dto.getValue()).isEqualTo("Zorro es un chaval muy simpatico");
  }

  @Test
  public void test_booleanOptions() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro0=TRUE,zorro1=T,zorro2=FALSE,zorro3=F");
    assertThat(options.get(0).getName()).isEqualTo("zorro0");
    assertThat(options.get(0).getValue()).isEqualTo("TRUE");
    assertThat(options.get(1).getName()).isEqualTo("zorro1");
    assertThat(options.get(1).getValue()).isEqualTo("T");
    assertThat(options.get(2).getName()).isEqualTo("zorro2");
    assertThat(options.get(2).getValue()).isEqualTo("FALSE");
    assertThat(options.get(3).getName()).isEqualTo("zorro3");
    assertThat(options.get(3).getValue()).isEqualTo("F");
  }

  @Test
  public void test_numberOption() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro=5");
    DataShield.DataShieldROptionDto dto = options.get(0);
    assertThat(dto.getName()).isEqualTo("zorro");
    assertThat(dto.getValue()).isEqualTo("5");
  }

  @Test
  public void test_failingOption() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro");
    assertThat(options.size()).isEqualTo(0);
  }

  @Test
  public void test_failingOptions() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro=5,toto,tutu=4");
    assertThat(options.size()).isEqualTo(2);
    DataShield.DataShieldROptionDto dto = options.get(0);
    assertThat(dto.getName()).isEqualTo("zorro");
    assertThat(dto.getValue()).isEqualTo("5");
    dto = options.get(1);
    assertThat(dto.getName()).isEqualTo("tutu");
    assertThat(dto.getValue()).isEqualTo("4");
  }

  @Test
  public void test_functionOption() {
    List<DataShield.DataShieldROptionDto> options = parser.parse("zorro=base:::log(x=10, base=5)");
    DataShield.DataShieldROptionDto dto = options.get(0);
    assertThat(dto.getName()).isEqualTo("zorro");
    assertThat(dto.getValue()).isEqualTo("base:::log(x=10, base=5)");
  }

  @Test
  public void test_functionInFunctionOption() {
    List<DataShield.DataShieldROptionDto> options = parser
        .parse("zorro=quote(q(\"no\", status = 66, runLast = FALSE))");
    DataShield.DataShieldROptionDto dto = options.get(0);
    assertThat(dto.getName()).isEqualTo("zorro");
    assertThat(dto.getValue()).isEqualTo("quote(q(\"no\", status = 66, runLast = FALSE))");
  }

  @Test
  public void test_multipleOptions() {
    String source
        = "zorro0=\"Zorro es un chaval muy simpatico\",zorro1=\"Es un hombre amoroso\",zorro2=TRUE,zorro3=base:::log(x=10, base=5), zorro4=quote({dump.frames(to.file = TRUE); q()}), zorro5=c(\"hey\", \"ho\"),zorro6=quote(q(\"no\", status = 66, runLast = FALSE))";
    List<DataShield.DataShieldROptionDto> options = parser.parse(source);
    assertThat(options.get(0).getName()).isEqualTo("zorro0");
    assertThat(options.get(0).getValue()).isEqualTo("Zorro es un chaval muy simpatico");
    assertThat(options.get(1).getName()).isEqualTo("zorro1");
    assertThat(options.get(1).getValue()).isEqualTo("Es un hombre amoroso");
    assertThat(options.get(2).getName()).isEqualTo("zorro2");
    assertThat(options.get(2).getValue()).isEqualTo("TRUE");
    assertThat(options.get(3).getName()).isEqualTo("zorro3");
    assertThat(options.get(3).getValue()).isEqualTo("base:::log(x=10, base=5)");
    assertThat(options.get(4).getName()).isEqualTo("zorro4");
    assertThat(options.get(4).getValue()).isEqualTo("quote({dump.frames(to.file = TRUE); q()})");
    assertThat(options.get(5).getName()).isEqualTo("zorro5");
    assertThat(options.get(5).getValue()).isEqualTo("c(\"hey\", \"ho\")");
    assertThat(options.get(6).getName()).isEqualTo("zorro6");
    assertThat(options.get(6).getValue()).isEqualTo("quote(q(\"no\", status = 66, runLast = FALSE))");
  }

  @Test
  public void test_multipleOptionsWithWS() {
    String source
        = "\n\t\rzorro0=\"Zorro es un chaval muy simpatico\",\nzorro1=\"Es un hombre amoroso\",zorro2=TRUE, \tzorro3=base:::log(x=10, base=5), zorro4=quote({dump.frames(to.file = TRUE); q()}), \r\nzorro5=c(\"hey\", \"ho\"),\nzorro6=quote(q(\"no\", status = 66, runLast = FALSE))\n";
    List<DataShield.DataShieldROptionDto> options = parser.parse(source);
    assertThat(options.get(0).getName()).isEqualTo("zorro0");
    assertThat(options.get(0).getValue()).isEqualTo("Zorro es un chaval muy simpatico");
    assertThat(options.get(1).getName()).isEqualTo("zorro1");
    assertThat(options.get(1).getValue()).isEqualTo("Es un hombre amoroso");
    assertThat(options.get(2).getName()).isEqualTo("zorro2");
    assertThat(options.get(2).getValue()).isEqualTo("TRUE");
    assertThat(options.get(3).getName()).isEqualTo("zorro3");
    assertThat(options.get(3).getValue()).isEqualTo("base:::log(x=10, base=5)");
    assertThat(options.get(4).getName()).isEqualTo("zorro4");
    assertThat(options.get(4).getValue()).isEqualTo("quote({dump.frames(to.file = TRUE); q()})");
    assertThat(options.get(5).getName()).isEqualTo("zorro5");
    assertThat(options.get(5).getValue()).isEqualTo("c(\"hey\", \"ho\")");
    assertThat(options.get(6).getName()).isEqualTo("zorro6");
    assertThat(options.get(6).getValue()).isEqualTo("quote(q(\"no\", status = 66, runLast = FALSE))");
  }

}
