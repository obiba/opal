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

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.service.impl.OpalPrivateVariableEntityMap;
import org.obiba.opal.core.unit.FunctionalUnit;

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

  /**
   * Override <code>getVariableEntityMappingFunction</code> to transform "public" entities into "private" entities.
   */
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return new BijectiveFunction<VariableEntity, VariableEntity>() {
      public VariableEntity apply(VariableEntity from) {
        VariableEntity privateEntity = entityMap.privateEntity(from);
        if(privateEntity == null) {
          throw new RuntimeException("no private entity exists for public entity " + from);
        }
        return privateEntity;
      }

      @Override
      public VariableEntity unapply(VariableEntity from) {
        VariableEntity publicEntity = entityMap.publicEntity(from);
        if(publicEntity == null) {
          throw new RuntimeException("no public entity exists for private entity " + from);
        }
        return publicEntity;
      }
    };
  }

}
