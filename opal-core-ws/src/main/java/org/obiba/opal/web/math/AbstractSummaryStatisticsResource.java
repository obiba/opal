/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.math;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class AbstractSummaryStatisticsResource {

  @Nonnull
  private final ValueTable valueTable;

  @Nonnull
  private final Variable variable;

  @Nullable
  private final VectorSource vectorSource;

  protected AbstractSummaryStatisticsResource(@Nonnull ValueTable valueTable, @Nonnull Variable variable,
      @Nullable VectorSource vectorSource) {
    Preconditions.checkNotNull(valueTable);
    Preconditions.checkNotNull(variable);

    this.valueTable = valueTable;
    this.variable = variable;
    this.vectorSource = vectorSource;
  }

  @Nonnull
  public ValueTable getValueTable() {
    return valueTable;
  }

  @Nonnull
  public Variable getVariable() {
    return variable;
  }

  @Nullable
  public VectorSource getVectorSource() {
    return vectorSource;
  }

  @Nonnull
  protected Iterable<Value> getValues() {
    return vectorSource == null
        ? Collections.<Value>emptySet()
        : vectorSource.getValues(Sets.newTreeSet(getValueTable().getVariableEntities()));
  }
}
