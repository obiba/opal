/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import java.io.IOException;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.IdentifiersDatabaseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.base.Predicate;

/**
 *
 */
@Component
public class DefaultIdentifiersTableService implements IdentifiersTableService {

  private static final Logger log = LoggerFactory.getLogger(DefaultIdentifiersTableService.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private TransactionTemplate transactionTemplate;

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
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
        destroy();
      }
    });
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

  @NotNull
  private Datasource getDatasource() throws IdentifiersDatabaseNotFoundException {
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

}
