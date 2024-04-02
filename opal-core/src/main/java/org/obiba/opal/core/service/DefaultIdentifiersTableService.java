/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 *
 */
@Component
@Transactional
public class DefaultIdentifiersTableService implements IdentifiersTableService {

  private static final Logger log = LoggerFactory.getLogger(DefaultIdentifiersTableService.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @NotNull
  @Value("${org.obiba.opal.keys.tableReference}")
  private String tableReference;

  @Value("${org.obiba.opal.keys.entityType}")
  private String participantEntityType;

  private MagmaEngineReferenceResolver tableResolver;

  private Datasource datasource;

  @Override
  public void start() { }

  @Override
  public void stop() {
    if(datasource == null) return;
    try {
      Disposables.dispose(datasource);
    } catch(RuntimeException e) {
      log.debug("Ignoring exception during shutdown sequence.", e);
    }
    datasource = null;
  }

  @Override
  public String getDatasourceName() {
    return getTableResolver().getDatasourceName();
  }

  private String getParticipantTableName() {
    return getTableResolver().getTableName();
  }

  @NotNull
  private String getParticipantEntityType() {
    return participantEntityType;
  }

  @NotNull
  @Override
  public String getTableReference(@NotNull String entityType) {
    ValueTable table = getIdentifiersTable(entityType);
    return table.getDatasource().getName() + "." + getIdentifiersTable(entityType).getName();
  }

  @Override
  public boolean hasEntities() {
    if (!hasDatasource()) return false;
    for (ValueTable table : getDatasource().getValueTables()) {
      if (table.getVariableEntityCount() > 0) return true;
    }
    return false;
  }

  private MagmaEngineReferenceResolver getTableResolver() {
    if(tableResolver == null) {
      tableResolver = MagmaEngineTableResolver.valueOf(tableReference);
    }
    return tableResolver;
  }

  @Override
  @Nullable
  public Datasource getDatasource() throws IdentifiersDatabaseNotFoundException {
    if(datasource == null && databaseRegistry.hasIdentifiersDatabase()) {
      DatasourceFactory datasourceFactory = databaseRegistry
          .createDatasourceFactory(getDatasourceName(), databaseRegistry.getIdentifiersDatabase());
      Initialisables.initialise(datasourceFactory);
      datasource = datasourceFactory.create();
      Initialisables.initialise(datasource);
      if (!Strings.isNullOrEmpty(getParticipantEntityType())) {
        try {
          getIdentifiersTable(getParticipantEntityType());
        } catch (NoSuchValueTableException e) {
          datasource.createWriter(getParticipantTableName(), getParticipantEntityType()).close();
        }
      }

    }
    return datasource;
  }

  @Override
  public boolean hasDatasource() {
    return databaseRegistry.hasIdentifiersDatabase();
  }

  @Override
  public boolean hasIdentifiersTable(@NotNull String entityType) {
    if (!hasDatasource()) return false;
    if (databaseRegistry.hasIdentifiersDatabase()) {
      try {
        for (ValueTable table : getDatasource().getValueTables()) {
          if (table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
            return true;
          }
        }
      } catch (Exception e) {
        log.error("Failed at getting identifiers database table for entity type {}", entityType, e);
        return false;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public ValueTable getIdentifiersTable(@NotNull String entityType) {
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return table;
      }
    }
    throw new NoSuchValueTableException("");
  }

  @NotNull
  @Override
  public ValueTable ensureIdentifiersTable(@NotNull String entityType) {
    if(!hasIdentifiersTable(entityType)) {
      getDatasource().createWriter(entityType, entityType).close();
    }
    return getIdentifiersTable(entityType);
  }

  @NotNull
  @Override
  public Variable ensureIdentifiersMapping(@NotNull IdentifiersMapping idMapping) {
    ValueTable table = ensureIdentifiersTable(idMapping.getEntityType());
    if(!table.hasVariable(idMapping.getName())) {
      try(ValueTableWriter tableWriter = getDatasource()
          .createWriter(idMapping.getEntityType(), idMapping.getEntityType());
          ValueTableWriter.VariableWriter variableWriter = tableWriter.writeVariables()) {
        variableWriter.writeVariable(
            Variable.Builder.newVariable(idMapping.getName(), TextType.get(), idMapping.getEntityType()).build());
      }
    }
    return getIdentifiersTable(idMapping.getEntityType()).getVariable(idMapping.getName());
  }

  @Override
  public ValueTableWriter createIdentifiersTableWriter(@NotNull String entityType) {
    ValueTable table = ensureIdentifiersTable(entityType);
    return getDatasource().createWriter(table.getName(), table.getEntityType());
  }

  @Nullable
  @Override
  public String getSelectScript(@NotNull String entityType, @NotNull String idMapping) {
    if(!hasIdentifiersTable(entityType)) return null;
    ValueTable table = getIdentifiersTable(entityType);
    if(!table.hasVariable(idMapping)) return null;
    Variable variable = table.getVariable(idMapping);
    if(!variable.hasAttribute("select")) return null;

    return variable.getAttributeStringValue("select");
  }

  @Override
  public boolean hasIdentifiersMapping(@NotNull String idMapping) {
    if (!hasDatasource()) return false;
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.hasVariable(idMapping)) return true;
    }
    return false;
  }

  @Override
  public boolean hasIdentifiersMapping(@NotNull String entityType, @NotNull String idMapping) {
    if (!hasDatasource()) return false;
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase()) && table.hasVariable(idMapping))
        return true;
    }
    return false;
  }
}
