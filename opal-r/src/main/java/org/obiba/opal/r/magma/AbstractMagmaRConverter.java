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

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.opal.core.magma.IdentifiersMappingView;
import org.rosuda.REngine.REXP;

import java.util.List;
import java.util.SortedSet;

/**
 * Base implementation of Magma vector providers.
 */
abstract class AbstractMagmaRConverter implements MagmaRConverter {

  MagmaAssignROperation magmaAssignROperation;

  AbstractMagmaRConverter(MagmaAssignROperation magmaAssignROperation) {
    this.magmaAssignROperation = magmaAssignROperation;
  }

  REXP getVector(VariableValueSource vvs, SortedSet<VariableEntity> entities, boolean withMissings) {
    VectorType vt = VectorTypeRegistry.forValueType(vvs.getValueType());
    return vt.asVector(vvs, entities, withMissings);
  }

  REXP getVector(Variable variable, List<Value> values, SortedSet<VariableEntity> entities, boolean withMissings, boolean withFactors, boolean withLabelled) {
    VectorType vt = VectorTypeRegistry.forValueType(variable.getValueType());
    return vt.asVector(variable, values, entities, withMissings, withFactors, withLabelled);
  }

  ValueTable applyIdentifiersMapping(ValueTable table) {
    String idMapping = magmaAssignROperation.getIdentifiersMapping();
    // If the table contains an entity that requires identifiers separation, create a "identifers view" of the table (replace
    // public (system) identifiers with private identifiers).
    if (!Strings.isNullOrEmpty(idMapping) &&
        magmaAssignROperation.getIdentifiersTableService().hasIdentifiersMapping(table.getEntityType(), idMapping)) {
      // Make a view that converts opal identifiers to unit identifiers
      return new IdentifiersMappingView(idMapping, IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PUBLIC, table,
          magmaAssignROperation.getIdentifiersTableService());
    }
    return table;
  }
}
