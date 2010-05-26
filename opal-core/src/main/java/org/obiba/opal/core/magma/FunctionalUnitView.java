/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import org.obiba.magma.NoSuchValueSetException;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.service.impl.OpalPrivateVariableEntityMap;
import org.obiba.opal.core.unit.FunctionalUnit;

import com.google.common.base.Function;

/**
 * When an Opal table is exported to some functional unit, entities must be exported with the identifiers understood by
 * that unit. The "public" Opal identifiers must be replaced by the "private" identifiers for the unit in question.
 * 
 * For a given Opal table and functional unit, this class provides a {@link View} of that table with identifiers
 * appropriate to that unit.
 */
public class FunctionalUnitView extends View {
  //
  // Instance Variables
  //

  private PrivateVariableEntityMap entityMap;

  //
  // Constructors
  //

  /**
   * Constructor.
   * 
   * @param unit functional unit
   * @param opalTable opal value table
   * @param keysTable opal keys table
   */
  public FunctionalUnitView(FunctionalUnit unit, ValueTable opalTable, ValueTable keysTable) {
    super(opalTable.getName(), opalTable);

    Variable keyVariable = keysTable.getVariable(unit.getKeyVariableName());

    IParticipantIdentifier nonGeneratingParticipantIdentifier = new IParticipantIdentifier() {
      public String generateParticipantIdentifier() {
        throw new UnsupportedOperationException("cannot generate identifier");
      }
    };

    this.entityMap = new OpalPrivateVariableEntityMap(keysTable, keyVariable, nonGeneratingParticipantIdentifier);
  }

  //
  // View Methods
  //

  /**
   * Override <code>hasValueSet</code> to map the specified "private" entity (unit) to its "public" entity (opal).
   */
  @Override
  public boolean hasValueSet(VariableEntity privateEntity) {
    return super.hasValueSet(entityMap.publicEntity(privateEntity));
  }

  /**
   * Override <code>getValueSet</code> to return the {@link ValueSet} with a reference to its "private" entity.
   */
  @Override
  public ValueSet getValueSet(VariableEntity privateEntity) throws NoSuchValueSetException {
    return super.getValueSet(entityMap.publicEntity(privateEntity));
  }

  /**
   * Override <code>getVariableEntityTransformer</code> to transform "public" entities into "private" entities.
   */
  public Function<VariableEntity, VariableEntity> getVariableEntityTransformer() {
    return new Function<VariableEntity, VariableEntity>() {
      public VariableEntity apply(VariableEntity from) {
        VariableEntity privateEntity = entityMap.privateEntity(from);
        if(privateEntity == null) {
          throw new RuntimeException("no private entity corresponding to public entity " + from);
        }
        return privateEntity;
      }
    };
  }
}
