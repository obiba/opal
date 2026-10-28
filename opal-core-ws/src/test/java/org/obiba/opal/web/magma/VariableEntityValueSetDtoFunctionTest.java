/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.opal.web.magma.VariableEntityValueSetDtoFunction.valueLink;

public class VariableEntityValueSetDtoFunctionTest {

  @Test
  public void valueLink_replaces_only_the_trailing_value_sets_segments() {
    assertThat(valueLink("/datasource/p/table/t/valueSets", "1", "v"))
        .isEqualTo("/datasource/p/table/t/valueSet/entity/1/variable/v/value");
    assertThat(valueLink("/datasource/p/table/t/valueSets/variable/a%20b", "1", "a b"))
        .isEqualTo("/datasource/p/table/t/valueSet/entity/1/variable/a%20b/value");
    assertThat(valueLink("/datasource/p/table/t/valueSets/", "1", "v"))
        .isEqualTo("/datasource/p/table/t/valueSet/entity/1/variable/v/value");
    assertThat(valueLink("/datasource/p/table/valueSets/valueSets", "1", "v"))
        .isEqualTo("/datasource/p/table/valueSets/valueSet/entity/1/variable/v/value");
  }
}
