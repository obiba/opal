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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResourceVariableValueSource extends AbstractRVariableValueSource {

  private static final Logger log = LoggerFactory.getLogger(ResourceVariableValueSource.class);

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
    if (column == null || !columnValues.containsKey(column.getPosition()))
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getValue(columnValues.get(column.getPosition()));
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return new ResourceVectorSource();
  }

  public ResourceView getValueTable() {
    return valueTable;
  }

  boolean hasColumn() {
    return column != null;
  }

  boolean hasScript() {
    return variable.hasAttribute("script");
  }

  String getScript() {
    return variable.getAttributeStringValue("script");
  }

  //
  // Private classes
  //

  private class ResourceVectorSource implements VectorSource {

    @Override
    public ValueType getValueType() {
      return variable.getValueType();
    }

    @Override
    public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
      if (column == null) {
        return () -> new Iterator<Value>() {

          private final Iterator<VariableEntity> entityIterator = entities.iterator();

          @Override
          public boolean hasNext() {
            return entityIterator.hasNext();
          }

          @Override
          public Value next() {
            entityIterator.next();
            return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
          }
        };
      } else
        return column.asVector(variable.getValueType(), valueTable.getIdColumn(), entities);
    }

    @Override
    public boolean supportVectorSummary() {
      return true;
    }

    @Override
    public VectorSummarySource getVectorSummarySource(Iterable<VariableEntity> entities) throws VectorSummarySourceNotSupportedException {
      return new ResourceVectorSummarySource(ResourceVariableValueSource.this, entities);
    }

    public ValueTable getValueTable() {
      return valueTable;
    }

    public Variable getVariable() {
      return variable;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof ResourceVariableValueSource.ResourceVectorSource)) return false;

      ResourceVariableValueSource.ResourceVectorSource rvs = (ResourceVariableValueSource.ResourceVectorSource) o;
      return rvs.getValueTable().getDatasource().getName().equals(valueTable.getDatasource().getName()) &&
          rvs.getValueTable().getName().equals(valueTable.getName()) &&
          rvs.getVariable().getName().equals(variable.getName());
    }

    @Override
    public int hashCode() {
      return Objects.hash(valueTable.getDatasource().getName(), valueTable.getName(), variable.getName());
    }
  }

}
