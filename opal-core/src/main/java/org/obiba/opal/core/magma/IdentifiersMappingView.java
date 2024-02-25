/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.type.TextType;
import org.obiba.magma.views.View;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.NoSuchPrivateIdentifierMappingException;
import org.obiba.opal.core.service.NoSuchSystemIdentifierMappingException;
import org.obiba.opal.core.service.OpalPrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.stream.Collectors;

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

  private final IdentifiersTableService identifiersTableService;

  private final boolean allowIdentifierGeneration;

  private final boolean ignoreUnknownIdentifier;

  private final Policy policy;

  private final IdentifierGenerator identifierGenerator;

  @NotNull
  private final UnitIdentifiersMappingFunction mappingFunction;

  private final Map<String, UnitIdentifiersMappingFunction> entityTypeMappingFunctions = Maps.newHashMap();

  /**
   * Do not ignore unknown identifiers and do not generate ones.
   *
   * @param idMapping name of the variable in the identifiers table to be used for retrieving the identifiers
   * @param policy the policy of which identifier to make public (opal identifier or unit identifier)
   * @param dataTable data value table to be wrapped
   * @param identifiersTableService identifiers table service
   */
  public IdentifiersMappingView(@NotNull String idMapping, @NotNull Policy policy, @NotNull ValueTable dataTable,
                                @NotNull IdentifiersTableService identifiersTableService) {
    this(idMapping, policy, dataTable, identifiersTableService, null, false);
  }

  /**
   * Wrapped the data table so that the entities identifiers are the ones provided by the identifiers table,
   * considering the given identifiers mapping.
   *
   * @param idMapping name of the variable in the identifiers table to be used for retrieving the identifiers
   * @param policy the policy of which identifier to make public (opal identifier or unit identifier)
   * @param dataTable data value table to be wrapped
   * @param identifiersTableService identifiers table service
   * @param identifierGenerator strategy for generating missing identifiers. can be null, in which case, identifiers
   * will not be generated
   * @param ignoreUnknownIdentifier error is thrown when an identifier that cannot be mapped is encountered
   */
  public IdentifiersMappingView(@NotNull String idMapping, @NotNull Policy policy, @NotNull ValueTable dataTable,
      @NotNull IdentifiersTableService identifiersTableService, @Nullable IdentifierGenerator identifierGenerator,
      boolean ignoreUnknownIdentifier) {

    // Null check on dataTable is required. If dataTable is null, we'll get NPE instead of IllegalArgumentException
    super(dataTable == null ? null : dataTable.getName(), dataTable);
    this.identifiersTableService = identifiersTableService;
    this.idMapping = idMapping;
    this.identifierGenerator = identifierGenerator;
    this.allowIdentifierGeneration = identifierGenerator != null;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
    this.policy = policy;
    this.mappingFunction = getMappingFunction(dataTable.getEntityType());
  }

  private UnitIdentifiersMappingFunction getMappingFunction(String entityType) {
    if (entityTypeMappingFunctions.containsKey(entityType)) return entityTypeMappingFunctions.get(entityType);

    if (identifiersTableService.hasIdentifiersTable(entityType)) {
      ValueTable identifiersTable = identifiersTableService.getIdentifiersTable(entityType);
      if (identifiersTable.hasVariable(idMapping)) {
        Variable idVariable = identifiersTable.getVariable(idMapping);
        return makeMappingFunction(identifiersTable, idVariable, identifierGenerator, policy);
      }
    }

    throw new IllegalArgumentException("No identifier mapping for type " + entityType + " with name " + idMapping);
  }

  private UnitIdentifiersMappingFunction makeMappingFunction(ValueTable identifiersTable, Variable idVariable, IdentifierGenerator identifierGenerator, Policy policy) {
    UnitIdentifiersMappingFunction mappingFunction;
    PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(identifiersTable, idVariable,
        identifierGenerator == null ? new IdentifierGenerator() {
          @Override
          public String generateIdentifier() {
            throw new UnsupportedOperationException("Identifiers generation not permitted");
          }
        } : identifierGenerator);

    switch (policy) {
      case UNIT_IDENTIFIERS_ARE_PUBLIC:
        mappingFunction = new UnitIdentifiersArePublic(entityMap);
        break;
      case UNIT_IDENTIFIERS_ARE_PRIVATE:
        mappingFunction = new UnitIdentifiersArePrivate(entityMap);
        break;
      default:
        throw new IllegalArgumentException("unknown policy '" + policy + "'");
    }
    log.debug("View created: Table={}, identifiers={}:{}, policy={}", getName(), identifiersTable.getName(), idMapping, policy);
    entityTypeMappingFunctions.put(identifiersTable.getEntityType(), mappingFunction);
    return mappingFunction;
  }

  public String getIdentifiersMapping() {
    return idMapping;
  }

  @NotNull
  public PrivateVariableEntityMap getPrivateVariableEntityMap() {
    return mappingFunction.getPrivateVariableEntityMap();
  }

  @Override
  public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
    return mappingFunction;
  }

  @NotNull
  @Override
  public BijectiveFunction<VariableValueSource, VariableValueSource> getVariableValueSourceMappingFunction() {
    return new BijectiveFunction<VariableValueSource, VariableValueSource>() {
      @Override
      public VariableValueSource apply(VariableValueSource from) {
        return new IdentifiersMappingViewVariableValueSource(from);
      }

      @Override
      public VariableValueSource unapply(@SuppressWarnings("ParameterHidesMemberVariable") VariableValueSource from) {
        return ((VariableValueSourceWrapper) from).getWrapped();
      }
    };
  }

  @Override
  public void dispose() {
    super.dispose();
    Disposables.dispose(mappingFunction);
  }

  //
  // Private classes
  //
  
  /**
   * Map values that represent entity IDs.
   */
  private class IdentifiersMappingViewVariableValueSource extends ViewVariableValueSource {

    private final Variable idVariable;

    private final String refEntityType;

    public IdentifiersMappingViewVariableValueSource(VariableValueSource wrapped) {
      super(wrapped);
      this.refEntityType = wrapped.getVariable().getReferencedEntityType();

      if (Strings.isNullOrEmpty(refEntityType)) {
        this.idVariable = null;
      } else {
        this.idVariable = VariableBean.Builder.sameAs(super.getVariable()).type(TextType.get()).build();
      }
    }

    @NotNull
    @Override
    public Value getValue(ValueSet valueSet) {
      Value value = super.getValue(valueSet);
      if (idVariable == null || value.isNull()) return value;

      if (value.isSequence()) {
        return TextType.get().sequenceOf(value.asSequence().getValues().stream()
            .map(this::mapIdentifierValue).collect(Collectors.toList()));
      }
      return mapIdentifierValue(value);
    }

    @NotNull
    @Override
    public ValueType getValueType() {
      return idVariable == null ? super.getValueType() : idVariable.getValueType();
    }

    @NotNull
    @Override
    public Variable getVariable() {
      return idVariable == null ? super.getVariable() : idVariable;
    }

    /**
     * Map value that represents an ID to a new value.
     *
     * @param value
     * @return
     */
    private Value mapIdentifierValue(Value value) {
      String identifier = value.toString();
      if (Strings.isNullOrEmpty(identifier)) return TextType.get().nullValue();
      VariableEntity entity = new VariableEntityBean(refEntityType, identifier);
      VariableEntity mappedEntity = getMappingFunction(refEntityType).apply(entity);
      if (mappedEntity == null) return TextType.get().nullValue();
      return TextType.get().valueOf(mappedEntity.getIdentifier());
    }

  }

  private interface UnitIdentifiersMappingFunction extends BijectiveFunction<VariableEntity, VariableEntity>, Disposable {

    PrivateVariableEntityMap getPrivateVariableEntityMap();

  }

  /**
   * Given an opal identifier, the apply() method will return the corresponding unit identifier.
   * <p/>
   * <pre>
   * apply: publicEntity --> privateEntity
   * unapply: privateEntity --> publicEntity
   * </pre>
   */
  private class UnitIdentifiersArePublic implements UnitIdentifiersMappingFunction {

    private final PrivateVariableEntityMap entityMap;

    private UnitIdentifiersArePublic(PrivateVariableEntityMap entityMap) {
      this.entityMap = entityMap;
    }

    @Override
    public PrivateVariableEntityMap getPrivateVariableEntityMap() {
      return entityMap;
    }

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

    @Override
    public void dispose() {
      Disposables.dispose(entityMap);
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
  private class UnitIdentifiersArePrivate implements UnitIdentifiersMappingFunction {

    private final PrivateVariableEntityMap entityMap;

    private UnitIdentifiersArePrivate(PrivateVariableEntityMap entityMap) {
      this.entityMap = entityMap;
    }

    @Override
    public PrivateVariableEntityMap getPrivateVariableEntityMap() {
      return entityMap;
    }

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

    @Override
    public void dispose() {
      Disposables.dispose(entityMap);
    }
  }

}
