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

public interface SummaryResource {
  void setValueTable(@NotNull ValueTable valueTable);

  void setVariable(@NotNull Variable variable);

  void setVariableValueSource(@NotNull ValueSource variableValueSource);

  @NotNull
  ValueTable getValueTable();

  @NotNull
  Variable getVariable();

  @NotNull
  ValueSource getVariableValueSource();
}
