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

import javax.annotation.Nonnull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.VariableStatsService;
import org.springframework.util.Assert;

public class AbstractSummaryResource {

  protected final VariableStatsService variableStatsService;

  @Nonnull
  private final ValueTable valueTable;

  @Nonnull
  private final Variable variable;

  @Nonnull
  private final ValueSource variableValueSource;

  protected AbstractSummaryResource(@Nonnull VariableStatsService variableStatsService, @Nonnull ValueTable valueTable,
      @Nonnull Variable variable, @Nonnull ValueSource variableValueSource) {
    Assert.notNull(variableStatsService);
    Assert.notNull(valueTable);
    Assert.notNull(variable);
    Assert.notNull(variableValueSource);

    this.variableValueSource = variableValueSource;
    this.variableStatsService = variableStatsService;
    this.valueTable = valueTable;
    this.variable = variable;
  }

  @Nonnull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Nonnull
  public Variable getVariable() {
    return variable;
  }

  @Nonnull
  public ValueSource getVariableValueSource() {
    return variableValueSource;
  }

}
