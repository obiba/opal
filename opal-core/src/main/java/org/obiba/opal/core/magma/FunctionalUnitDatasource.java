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

import java.io.IOException;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.transform.BijectiveFunction;
import org.obiba.magma.views.AbstractTransformingValueTableWrapper;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.service.impl.OpalPrivateVariableEntityMap;

/**
 *
 */
public class FunctionalUnitDatasource extends AbstractTransformingDatasourceWrapper {

  private Datasource wrappedDatasource;

  private final PrivateVariableEntityMap entityMap;

  private final boolean allowIdentifierGeneration;

  private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

  private ValueTable keysTable;

  public FunctionalUnitDatasource(Datasource wrapped, String keyVariableName, ValueTable keysTable, IParticipantIdentifier identifierGenerator) {
    super();
    this.wrappedDatasource = wrapped;
    this.allowIdentifierGeneration = identifierGenerator != null;

    Variable keyVariable = keysTable.getVariable(keyVariableName);
    IParticipantIdentifier identifierGeneratorOrDefault = null;
    if(allowIdentifierGeneration) {
      identifierGeneratorOrDefault = identifierGenerator;
    } else { // default
      identifierGeneratorOrDefault = new IParticipantIdentifier() {
        public String generateParticipantIdentifier() {
          throw new UnsupportedOperationException("cannot generate identifier");
        }
      };
    }

    this.entityMap = new OpalPrivateVariableEntityMap(keysTable, keyVariable, identifierGeneratorOrDefault);
    this.mappingFunction = new VariableEntityMappingFunction();
    this.keysTable = keysTable;
  }

  // make sure variable entities exist in identifiers table by creating them (if allowed)
  void mapIdentifiers() {
    // TODO make it transactional, lock key table etc... see DefaultImportService
    for(ValueTable wrappedTable : wrappedDatasource.getValueTables()) {
      if(wrappedTable.isForEntityType(keysTable.getEntityType())) {
        for(VariableEntity from : wrappedTable.getVariableEntities()) {
          if(mappingFunction.apply(from) == null && allowIdentifierGeneration) {
            entityMap.createPublicEntity(from);
          }
        }
      }
    }
  }

  @Override
  protected ValueTable transformValueTable(ValueTable wrappedTable) {
    if(wrappedTable.isForEntityType(keysTable.getEntityType())) {
      return new FunctionalUnitValueTable(wrappedTable, mappingFunction);
    } else {
      return wrappedTable;
    }
  }

  @Override
  protected ValueTableWriter transformValueTableWritter(ValueTableWriter wrappedTableWritter, String entityType) {
    if(keysTable.isForEntityType(entityType)) {
      return new FunctionalUnitValueTableWritter(wrappedTableWritter, mappingFunction);
    } else
      return wrappedTableWritter;
  }

  @Override
  protected Datasource getWrappedDatasource() {
    return wrappedDatasource;
  }

  private static class FunctionalUnitValueTable extends AbstractTransformingValueTableWrapper {

    private ValueTable wrappedTable;

    private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

    public FunctionalUnitValueTable(ValueTable wrappedValueTable, BijectiveFunction<VariableEntity, VariableEntity> mappingFunction) {
      super();
      this.wrappedTable = wrappedValueTable;
      this.mappingFunction = mappingFunction;
    }

    @Override
    public ValueTable getWrappedValueTable() {
      return wrappedTable;
    }

    @Override
    public BijectiveFunction<VariableEntity, VariableEntity> getVariableEntityMappingFunction() {
      return mappingFunction;
    }

  }

  private static class FunctionalUnitValueTableWritter implements ValueTableWriter {

    private ValueTableWriter wrapped;

    private final BijectiveFunction<VariableEntity, VariableEntity> mappingFunction;

    public FunctionalUnitValueTableWritter(ValueTableWriter wrapped, BijectiveFunction<VariableEntity, VariableEntity> mappingFunction) {
      super();
      this.wrapped = wrapped;
      this.mappingFunction = mappingFunction;
    }

    @Override
    public ValueSetWriter writeValueSet(VariableEntity entity) {
      return wrapped.writeValueSet(mappingFunction.unapply(entity));
    }

    @Override
    public VariableWriter writeVariables() {
      return wrapped.writeVariables();
    }

    @Override
    public void close() throws IOException {
      wrapped.close();
    }

  }

  private class VariableEntityMappingFunction implements BijectiveFunction<VariableEntity, VariableEntity> {

    @Override
    public VariableEntity apply(VariableEntity from) {
      return entityMap.publicEntity(from);
    }

    @Override
    public VariableEntity unapply(VariableEntity from) {
      return entityMap.privateEntity(from);
    }
  }

}
