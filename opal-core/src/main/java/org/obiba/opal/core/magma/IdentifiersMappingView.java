/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.View;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.service.NoSuchPrivateIdentifierMappingException;
import org.obiba.opal.core.service.NoSuchSystemIdentifierMappingException;
import org.obiba.opal.core.service.OpalPrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When an Opal table is exported, entities identifiers can be mapped. The "public" (or system) identifiers will
 * be replaced by the "private" identifiers.
 * <p/>
 * For a given data table and identifiers mapping, this class provides a {@link View} of that table with identifiers
 * appropriate to that mapping.
 */
public class IdentifiersMappingView extends View {

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

  private static final Logger log = LoggerFactory.getLogger(IdentifiersMappingView.class);

  @NotNull
  private final String idMapping;

  @NotNull
  private final PrivateVariableEntityMap entityMap;

  private final boolean allowIdentifierGeneration;

  private final boolean ignoreUnknownIdentifier;

  @NotNull
  private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

  /**
   * Do not ignore unknown identifiers and do not generate ones.
   *
   * @param idMapping name of the variable in the identifiers table to be used for retrieving the identifiers
   * @param policy the policy of which identifier to make public (opal identifier or unit identifier)
   * @param dataTable data value table to be wrapped
   * @param identifiersTable identifiers table
   */
  public IdentifiersMappingView(@NotNull String idMapping, @NotNull Policy policy, @NotNull ValueTable dataTable,
      @NotNull ValueTable identifiersTable) {
    this(idMapping, policy, dataTable, identifiersTable, null, false);
  }

  /**
   * Wrapped the data table so that the entities identifiers are the ones provided by the identifiers table,
   * considering the given identifiers mapping.
   *
   * @param idMapping name of the variable in the identifiers table to be used for retrieving the identifiers
   * @param policy the policy of which identifier to make public (opal identifier or unit identifier)
   * @param dataTable data value table to be wrapped
   * @param identifiersTable identifiers table
   * @param identifierGenerator strategy for generating missing identifiers. can be null, in which case, identifiers
   * will not be generated
   * @param ignoreUnknownIdentifier error is thrown when an identifier that cannot be mapped is encountered
   */
  public IdentifiersMappingView(@NotNull String idMapping, @NotNull Policy policy, @NotNull ValueTable dataTable,
      @NotNull ValueTable identifiersTable, @Nullable IdentifierGenerator identifierGenerator,
      boolean ignoreUnknownIdentifier) {

    // Null check on dataTable is required. If dataTable is null, we'll get NPE instead of IllegalArgumentException
    super(dataTable == null ? null : dataTable.getName(), dataTable);

    this.idMapping = idMapping;
    allowIdentifierGeneration = identifierGenerator != null;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;

    Variable idVariable = identifiersTable.getVariable(idMapping);

    entityMap = new OpalPrivateVariableEntityMap(identifiersTable, idVariable,
        identifierGenerator == null ? new IdentifierGenerator() {
          @Override
          public String generateIdentifier() {
            throw new UnsupportedOperationException("Identifiers generation not permitted");
          }
        } : identifierGenerator);

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

    log.debug("View created: Table={}, identifiers={}:{}, policy={}", dataTable.getName(), identifiersTable.getName(),
        idMapping, policy);
  }

  public String getIdentifiersMapping() {
    return idMapping;
  }

  @NotNull
  public PrivateVariableEntityMap getPrivateVariableEntityMap() {
    return entityMap;
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return mappingFunction;
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
      if(privateEntity == null) {
        if(allowIdentifierGeneration) {
          privateEntity = entityMap.createPrivateEntity(from);
        } else if(!ignoreUnknownIdentifier) {
          throw new NoSuchSystemIdentifierMappingException(idMapping, from.getIdentifier(), getEntityType());
        }
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
          throw new NoSuchPrivateIdentifierMappingException(idMapping, from.getIdentifier(), getEntityType());
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
