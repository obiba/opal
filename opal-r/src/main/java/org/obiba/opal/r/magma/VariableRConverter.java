/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.collect.ImmutableSortedSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.spi.r.datasource.magma.MagmaRRuntimeException;
import org.rosuda.REngine.REXP;

import java.util.SortedSet;

/**
 * Build a R vector from a variable: vector of values.
 *
 */
class VariableRConverter extends AbstractMagmaRConverter {

  private ValueTable table;

  private VariableValueSource variableValueSource;

  VariableRConverter(MagmaAssignROperation magmaAssignROperation) {
    super(magmaAssignROperation);
  }

  @Override
  public void doAssign(String symbol, String path) {
    magmaAssignROperation.doAssign(symbol, asVector(path, magmaAssignROperation.withMissings(), magmaAssignROperation.getIdentifiersMapping()));
  }

  private void resolvePath(String path, String idMapping) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(path);

    if (resolver.getVariableName() == null) {
      throw new MagmaRRuntimeException("Variable is not defined in path: " + path);
    }

    if (resolver.getDatasourceName() == null) {
      throw new MagmaRRuntimeException("Datasource is not defined in path: " + path);
    }

    table = applyIdentifiersMapping(resolver.resolveTable((ValueTable) null));
    variableValueSource = table.getVariableValueSource(resolver.getVariableName());
  }

  private REXP asVector(String path, boolean withMissings, String identifiersMapping) {
    resolvePath(path, identifiersMapping);
    SortedSet<VariableEntity> entities = ImmutableSortedSet.copyOf(table.getVariableEntities());
    return getVector(variableValueSource, entities, withMissings);
  }

}
