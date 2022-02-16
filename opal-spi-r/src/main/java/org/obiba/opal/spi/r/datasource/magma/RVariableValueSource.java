/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import org.obiba.magma.*;
import org.obiba.opal.spi.r.RServerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The R variable represents the column of a tibble.
 */
class RVariableValueSource extends AbstractRVariableValueSource implements VariableValueSource, VectorSource {

  private static final Logger log = LoggerFactory.getLogger(RVariableValueSource.class);

  private final int position;

  private Variable variable;

  private final RVariableHelper helper;

  RVariableValueSource(TibbleTable valueTable, RServerResult columnDesc, int position) {
    this.position = position;
    this.helper = new RVariableHelper(columnDesc.asNamedList(), valueTable, position);
    this.variable = helper.newVariable();
  }

  @Override
  public Variable getVariable() {
    return variable;
  }

  @Override
  public ValueType getValueType() {
    return variable.getValueType();
  }

  @Override
  public Iterable<Value> getValues(Iterable<VariableEntity> entities) {
    return null;
  }

  @Override
  public Value getValue(ValueSet valueSet) {
    Map<Integer, List<Object>> columnValues = ((RValueSet) valueSet).getValuesByPosition();
    if (!columnValues.containsKey(position))
      return variable.isRepeatable() ? getValueType().nullSequence() : getValueType().nullValue();
    return getValue(columnValues.get(position));
  }

  @Override
  public boolean supportVectorSource() {
    return true;
  }

  @Override
  public VectorSource asVectorSource() throws VectorSourceNotSupportedException {
    return this;
  }

}
