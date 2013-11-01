/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.VariableStatsService;
import org.springframework.util.Assert;

public class AbstractSummaryResource {

  protected final VariableStatsService variableStatsService;

  @NotNull
  private final ValueTable valueTable;

  @NotNull
  private final Variable variable;

  @NotNull
  private final ValueSource variableValueSource;

  protected AbstractSummaryResource(@NotNull VariableStatsService variableStatsService, @NotNull ValueTable valueTable,
      @NotNull Variable variable, @NotNull ValueSource variableValueSource) {
    Assert.notNull(variableStatsService);
    Assert.notNull(valueTable);
    Assert.notNull(variable);
    Assert.notNull(variableValueSource);

    this.variableValueSource = variableValueSource;
    this.variableStatsService = variableStatsService;
    this.valueTable = valueTable;
    this.variable = variable;
  }

  @NotNull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @NotNull
  public Variable getVariable() {
    return variable;
  }

  @NotNull
  public ValueSource getVariableValueSource() {
    return variableValueSource;
  }

}
