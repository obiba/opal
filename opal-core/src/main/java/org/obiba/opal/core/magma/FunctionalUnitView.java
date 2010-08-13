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

  public enum Policy {

    /**
     * Make the unit's identifiers public. That is, the identifiers exposed by this table are the ones from the
     * functional unit
     */
    UNIT_IDENTIFIERS_ARE_PUBLIC,

    /**
     * Make the unit's identifiers private. That is, the identifiers exposed by this table are the ones to which the
     * functional unit's identifier map to in the keys table.
     */
    UNIT_IDENTIFIERS_ARE_PRIVATE;

  }

  private final FunctionalUnit unit;

  private final PrivateVariableEntityMap entityMap;

  private final boolean allowIdentifierGeneration;

  private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

  public FunctionalUnitView(FunctionalUnit unit, Policy policy, ValueTable dataTable, ValueTable keysTable) {
    this(unit, policy, dataTable, keysTable, null);
  }

  /**
   * Constructor.
   * 
   * @param unit functional unit
   * @param policy the policy of which identifier to make public (opal identifier or unit identifier)
   * @param opalTable opal value table
   * @param keysTable opal keys table
   * @param identifierGenerator strategy for generating missing identifiers. can be null, in which case, identifiers
   * will not be generated
   */
  public FunctionalUnitView(FunctionalUnit unit, Policy policy, ValueTable dataTable, ValueTable keysTable, IParticipantIdentifier identifierGenerator) {
    // Null check on dataTable is required. If dataTable is null, we'll get NPE instead of IllegalArgumentException
    super(dataTable == null ? null : dataTable.getName(), dataTable);
    if(unit == null) throw new IllegalArgumentException("unit cannot be null");
    if(policy == null) throw new IllegalArgumentException("policy cannot be null");
    if(dataTable == null) throw new IllegalArgumentException("dataTable cannot be null");
    if(keysTable == null) throw new IllegalArgumentException("keysTable cannot be null");

    this.unit = unit;
    this.allowIdentifierGeneration = identifierGenerator != null;

    Variable keyVariable = keysTable.getVariable(unit.getKeyVariableName());

    if(allowIdentifierGeneration == false) {
      identifierGenerator = new IParticipantIdentifier() {
        public String generateParticipantIdentifier() {
          throw new UnsupportedOperationException("cannot generate identifier");
        }
      };
    }

    this.entityMap = new OpalPrivateVariableEntityMap(keysTable, keyVariable, identifierGenerator);

    switch(policy) {
    case UNIT_IDENTIFIERS_ARE_PUBLIC:
      this.mappingFunction = new UnitIdentifiersArePublic();
      break;
    case UNIT_IDENTIFIERS_ARE_PRIVATE:
      this.mappingFunction = new UnitIdentifiersArePrivate();
      break;
    default:
      throw new IllegalArgumentException("unknown policy '" + policy + "'");
    }
  }

  public PrivateVariableEntityMap getPrivateVariableEntityMap() {
    return this.entityMap;
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return mappingFunction;
  }

  /**
   * Given an opal identifier, the apply() method will return the corresponding unit identifier.
   * 
   * <pre>
   * apply: publicEntity --> privateEntity
   * unapply: privateEntity --> publicEntity
   * </pre>
   */
  private class UnitIdentifiersArePublic implements BijectiveFunction<VariableEntity, VariableEntity> {
    public VariableEntity apply(VariableEntity from) {
      VariableEntity privateEntity = entityMap.privateEntity(from);
      if(privateEntity == null) {
        throw new RuntimeException("Functional unit '" + unit.getName() + "' does not have an identifier for entity '" + from.getIdentifier() + "'");
      }
      return privateEntity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entityMap.publicEntity(from);
    }
  }

  /**
   * Given a unit identifier, the apply() method will return the corresponding opal identifier. If {@code
   * allowIdentifierGeneration} is true, the apply method will generate opal identifiers when one does not already
   * exist.
   * 
   * <pre>
   * apply: privateEntity --> publicEntity
   * unapply: publicEntity --> privateEntity
   * </pre>
   */
  private class UnitIdentifiersArePrivate implements BijectiveFunction<VariableEntity, VariableEntity> {
    public VariableEntity apply(VariableEntity from) {
      VariableEntity publicEntity = entityMap.publicEntity(from);
      if(publicEntity == null && allowIdentifierGeneration) {
        publicEntity = entityMap.createPublicEntity(from);
      }
      return publicEntity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entityMap.privateEntity(from);
    }
  }

}
