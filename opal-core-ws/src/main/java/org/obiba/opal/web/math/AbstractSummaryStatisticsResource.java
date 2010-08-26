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

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VectorSource;

import com.google.common.collect.Sets;

public class AbstractSummaryStatisticsResource {

  private final ValueTable valueTable;

  private final Variable variable;

  private final VectorSource vectorSource;

  protected AbstractSummaryStatisticsResource(ValueTable valueTable, Variable variable, VectorSource vectorSource) {
    this.valueTable = valueTable;
    this.variable = variable;
    this.vectorSource = vectorSource;
  }

  public ValueTable getValueTable() {
    return valueTable;
  }

  public Variable getVariable() {
    return variable;
  }

  public VectorSource getVectorSource() {
    return vectorSource;
  }

  protected Iterable<Value> getValues() {
    return vectorSource.getValues(Sets.newTreeSet(getValueTable().getVariableEntities()));
  }
}
