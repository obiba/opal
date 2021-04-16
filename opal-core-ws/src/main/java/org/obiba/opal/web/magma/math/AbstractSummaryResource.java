/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.magma.math;

import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.service.VariableSummaryService;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSummaryResource implements SummaryResource {

  @NotNull
  private ValueTable valueTable;

  @NotNull
  private Variable variable;

  @NotNull
  private ValueSource variableValueSource;

  @Autowired
  protected VariableSummaryService variableSummaryService;

  @Override
  public void setValueTable(@NotNull ValueTable valueTable) {
    this.valueTable = valueTable;
  }

  @Override
  public void setVariable(@NotNull Variable variable) {
    this.variable = variable;
  }

  @Override
  public void setVariableValueSource(@NotNull ValueSource variableValueSource) {
    this.variableValueSource = variableValueSource;
  }

  @Override
  @NotNull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Override
  @NotNull
  public Variable getVariable() {
    return variable;
  }

  @Override
  @NotNull
  public ValueSource getVariableValueSource() {
    return variableValueSource;
  }

}
