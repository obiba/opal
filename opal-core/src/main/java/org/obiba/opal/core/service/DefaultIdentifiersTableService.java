/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.lang.Closeables;
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

import com.google.common.base.Predicate;

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

  @NotNull
  @Value("${org.obiba.opal.keys.entityType}")
  private String entityType;

  private MagmaEngineReferenceResolver tableResolver;

  @Nullable
  private Datasource datasource;

  @Override
  public ValueTable getValueTable() throws IdentifiersDatabaseNotFoundException {
    return getDatasource().getValueTable(getTableName());
  }

  @Override
  public ValueTableWriter createValueTableWriter() throws IdentifiersDatabaseNotFoundException {
    return getDatasource().createWriter(getTableName(), entityType);
  }

  @Override
  public boolean hasValueTable() throws IdentifiersDatabaseNotFoundException {
    return getDatasource().hasValueTable(getTableName());
  }

  @Override
  public String getDatasourceName() {
    return getTableResolver().getDatasourceName();
  }

  @Override
  public String getTableName() {
    return getTableResolver().getTableName();
  }

  @NotNull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @NotNull
  @Override
  public String getTableReference() {
    return tableReference;
  }

  @Override
  public boolean hasEntities(Predicate<ValueTable> predicate) {
    return getDatasource().hasEntities(predicate);
  }

  @Override
  public void unregisterDatabase() {
    destroy();
    databaseRegistry.unregister(databaseRegistry.getIdentifiersDatabase().getName(), getDatasourceName());
  }

  private MagmaEngineReferenceResolver getTableResolver() {
    if(tableResolver == null) {
      tableResolver = MagmaEngineTableResolver.valueOf(tableReference);
    }
    return tableResolver;
  }

  @PreDestroy
  public void destroy() {
    if(datasource == null) return;
    try {
      Disposables.dispose(datasource);
      datasource = null;
    } catch(RuntimeException e) {
      log.warn("Ignoring exception during shutdown sequence.", e);
    }
  }

  @Override
  @NotNull
  public Datasource getDatasource() throws IdentifiersDatabaseNotFoundException {
    if(datasource == null) {
      DatasourceFactory datasourceFactory = databaseRegistry
          .createDataSourceFactory(getDatasourceName(), databaseRegistry.getIdentifiersDatabase());
      Initialisables.initialise(datasourceFactory);
      datasource = datasourceFactory.create();
      Initialisables.initialise(datasource);
      if(!datasource.hasValueTable(getTableName())) {
        try {
          datasource.createWriter(getTableName(), getEntityType()).close();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return datasource;
  }

  @Override
  public boolean hasValueTable(String entityType) {
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ValueTable getValueTable(String entityType) {
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return table;
      }
    }
    throw new NoSuchValueTableException("");
  }

  @Override
  public ValueTable ensureValueTable(String entityType) {
    if(!hasValueTable(entityType)) {
      ValueTableWriter vtw = null;
      try {
        vtw = getDatasource().createWriter(entityType, entityType);
      } finally {
        Closeables.closeQuietly(vtw);
      }
    }
    return getValueTable(entityType);
  }

  @Override
  public Variable ensureVariable(IdentifiersMapping idMapping) {
    ValueTable table = ensureValueTable(idMapping.getEntityType());
    if(!table.hasVariable(idMapping.getName())) {
      ValueTableWriter vtw = null;
      ValueTableWriter.VariableWriter vw = null;
      try {
        vtw = getDatasource().createWriter(entityType, entityType);
        vw = vtw.writeVariables();
        vw.writeVariable(
            Variable.Builder.newVariable(idMapping.getName(), TextType.get(), idMapping.getEntityType()).build());
      } finally {
        Closeables.closeQuietly(vw);
        Closeables.closeQuietly(vtw);
      }
    }
    return getValueTable(idMapping.getEntityType()).getVariable(idMapping.getName());
  }

  @Override
  public ValueTableWriter createValueTableWriter(@NotNull String entityType) {
    ValueTable table = ensureValueTable(entityType);
    return getDatasource().createWriter(table.getName(), table.getEntityType());
  }
}
