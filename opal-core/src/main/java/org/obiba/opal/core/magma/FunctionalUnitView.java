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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.domain.participant.identifier.impl.ParticipantIdentifiers;
import org.obiba.opal.core.service.impl.OpalPrivateVariableEntityMap;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * When an Opal table is exported to some functional unit, entities must be exported with the identifiers understood by
 * that unit. The "public" Opal identifiers must be replaced by the "private" identifiers for the unit in question.
 * <p/>
 * For a given Opal table and functional unit, this class provides a {@link View} of that table with identifiers
 * appropriate to that unit.
 */
public class FunctionalUnitView extends View {

  public enum Policy {

    /**
     * Make the unit's identifiers public.
     * That is, the identifiers exposed by this table are the ones from the functional unit
     * OpalInstance to Unit
     */
    UNIT_IDENTIFIERS_ARE_PUBLIC,

    /**
     * Make the unit's identifiers private.
     * That is, the identifiers exposed by this table are the ones to which the functional unit's identifier map to in the keys table.
     * Unit to OpalInstance
     */
    UNIT_IDENTIFIERS_ARE_PRIVATE

  }

  private static final Logger log = LoggerFactory.getLogger(FunctionalUnitView.class);

  @Nonnull
  private final FunctionalUnit unit;

  @Nonnull
  private final PrivateVariableEntityMap entityMap;

  private final boolean allowIdentifierGeneration;

  private final boolean ignoreUnknownIdentifier;

  @Nonnull
  private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

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
  @SuppressWarnings({ "ConstantConditions", "OverlyLongMethod", "PMD.NcssMethodCount" })
  public FunctionalUnitView(@Nonnull FunctionalUnit unit, @Nonnull Policy policy, @Nonnull ValueTable dataTable,
      @Nonnull ValueTable keysTable, @Nullable IParticipantIdentifier identifierGenerator,
      boolean ignoreUnknownIdentifier) {

    // Null check on dataTable is required. If dataTable is null, we'll get NPE instead of IllegalArgumentException
    super(dataTable == null ? null : dataTable.getName(), dataTable);
    Assert.notNull(unit, "unit cannot be null");
    Assert.notNull(policy, "policy cannot be null");
    Assert.notNull(dataTable, "dataTable cannot be null");
    Assert.notNull(keysTable, "keysTable cannot be null");

    this.unit = unit;
    allowIdentifierGeneration = identifierGenerator != null;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;

    Variable keyVariable = keysTable.getVariable(unit.getKeyVariableName());

    entityMap = new OpalPrivateVariableEntityMap(keysTable, keyVariable,
        identifierGenerator == null ? ParticipantIdentifiers.UNSUPPORTED : identifierGenerator);

    switch(policy) {
      case UNIT_IDENTIFIERS_ARE_PUBLIC:
        mappingFunction = new UnitIdentifiersArePublic();
        break;
      case UNIT_IDENTIFIERS_ARE_PRIVATE:
        mappingFunction = new UnitIdentifiersArePrivate();
        break;
      default:
        throw new IllegalArgumentException("unknown policy '" + policy + "'");
    }

    log.debug("Create FunctionalUnitView. Table: {}, unit: {}, policy: {}", dataTable.getName(), unit.getName(),
        policy);
  }

  public FunctionalUnitView(@Nonnull FunctionalUnit unit, @Nonnull Policy policy, @Nonnull ValueTable dataTable,
      @Nonnull ValueTable keysTable) {
    this(unit, policy, dataTable, keysTable, null, false);
  }

  @Nonnull
  public PrivateVariableEntityMap getPrivateVariableEntityMap() {
    return entityMap;
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return mappingFunction;
  }

  @Nonnull
  public FunctionalUnit getUnit() {
    return unit;
  }

  /**
   * Given an opal identifier, the apply() method will return the corresponding unit identifier.
   * <p/>
   * <pre>
   * apply: publicEntity --> privateEntity
   * unapply: privateEntity --> publicEntity
   * </pre>
   */
  private class UnitIdentifiersArePublic implements BijectiveFunction<VariableEntity, VariableEntity> {
    @Override
    public VariableEntity apply(VariableEntity from) {
      VariableEntity privateEntity = entityMap.privateEntity(from);
      if(privateEntity == null && !ignoreUnknownIdentifier) {
        throw new RuntimeException(
            "Functional unit '" + unit.getName() + "' does not have an identifier for entity '" + from.getIdentifier() +
                "'");
      }
      return privateEntity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entityMap.publicEntity(from);
    }
  }

  /**
   * Given a unit identifier, the apply() method will return the corresponding opal identifier. If
   * {@code allowIdentifierGeneration} is true, the apply method will generate opal identifiers when one does not
   * already exist.
   * <p/>
   * <pre>
   * apply: privateEntity --> publicEntity
   * unapply: publicEntity --> privateEntity
   * </pre>
   */
  private class UnitIdentifiersArePrivate implements BijectiveFunction<VariableEntity, VariableEntity> {
    @Override
    public VariableEntity apply(VariableEntity from) {
      VariableEntity publicEntity = entityMap.publicEntity(from);
      if(publicEntity == null) {
        if(allowIdentifierGeneration) {
          publicEntity = entityMap.createPublicEntity(from);
        } else if(!ignoreUnknownIdentifier) {
          throw new RuntimeException(
              "Functional unit '" + unit.getName() + "' has an unknown entity with identifier '" +
                  from.getIdentifier() + "'");
        }
      }
      return publicEntity;
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entityMap.privateEntity(from);
    }
  }

}
