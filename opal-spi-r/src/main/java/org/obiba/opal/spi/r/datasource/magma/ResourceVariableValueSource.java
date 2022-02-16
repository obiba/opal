/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.*;
import org.obiba.opal.spi.resource.TabularResourceConnector;

import java.util.List;
import java.util.Map;

public class ResourceVariableValueSource extends AbstractRVariableValueSource {

  private Variable variable;

  private final ResourceView valueTable;

  private final TabularResourceConnector.Column column;

  public ResourceVariableValueSource(Variable variable, TabularResourceConnector.Column column, ResourceView valueTable) {
    this.variable = variable;
    this.column = column;
    this.valueTable = valueTable;
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    // note: no column name vs. variable name mapping for now (such info could be one of the variable's attribute?)
    // or could also be derived from an R script
    Map<Integer, List<Object>> columnValues = ((RValueSet) valueSet).getValuesByPosition();
    if (!columnValues.containsKey(column.getPosition()))
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getValue(columnValues.get(column.getPosition()));
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return new VectorSource() {
      @Override
      public ValueType getValueType() {
        return variable.getValueType();
      }

      @Override
      public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
        // TODO filter by entity and make batch queries
        return valueTable.getConnector().getColumn(valueTable.getColumnName(variable)).asVector(variable.getValueType(), false, 0, -1);
      }
    };
  }
}
