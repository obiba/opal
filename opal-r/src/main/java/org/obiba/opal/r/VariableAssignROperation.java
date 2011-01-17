/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.rosuda.REngine.REXP;

import com.google.common.collect.Sets;

/**
 *
 */
public class VariableAssignROperation extends AbstractROperation {

  private final String symbol;

  private ValueTable table;

  private VariableValueSource variableValueSource;

  public VariableAssignROperation(String symbol, String path) {
    super();
    this.symbol = symbol;
    setVariableValueSource(path);
  }

  private void setVariableValueSource(String path) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(path);

    Datasource ds = MagmaEngine.get().getDatasource(resolver.getDatasourceName());
    if(!ds.hasValueTable(resolver.getTableName())) {
      // TODO
    }
    table = ds.getValueTable(resolver.getTableName());
    if(!table.hasVariable(resolver.getVariableName())) {
      // TODO
    }
    variableValueSource = table.getVariableValueSource(resolver.getVariableName());
  }

  @Override
  public void doWithConnection() {
    VectorType vt = VectorType.forValueType(variableValueSource.getValueType());
    Set<VariableEntity> entities = table.getVariableEntities();
    REXP rexp = vt.asVector(entities.size(), getValues(entities));
    assign(symbol, rexp);
  }

  private Iterable<Value> getValues(Set<VariableEntity> entities) {
    return variableValueSource.asVectorSource().getValues(Sets.newTreeSet(entities));
  }

}
