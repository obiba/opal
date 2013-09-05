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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.ServiceNotRunningException;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 *
 */
public class DefaultIdentifiersTableService implements IdentifiersTableService {

  private static final Logger log = LoggerFactory.getLogger(DefaultIdentifiersTableService.class);

  @Nonnull
  private final PlatformTransactionManager txManager;

  @Nonnull
  private final String tableReference;

  @Nonnull
  private final String entityType;

  private MagmaEngineReferenceResolver tableResolver;

  @Nullable
  private Datasource datasource;

  private final DatabaseRegistry databaseRegistry;

  @Autowired
  public DefaultIdentifiersTableService(@Nonnull PlatformTransactionManager txManager,
      @Nonnull @Value("${org.obiba.opal.keys.tableReference}") String tableReference,
      @Nonnull @Value("${org.obiba.opal.keys.entityType}") String entityType, DatabaseRegistry databaseRegistry) {
    Assert.notNull(txManager, "txManager cannot be null");
    Assert.notNull(tableReference, "tableReference cannot be null");
    Assert.notNull(entityType, "entityType cannot be null");
    Assert.notNull(databaseRegistry, "databaseRegistry cannot be null");
    this.txManager = txManager;
    this.tableReference = tableReference;
    this.entityType = entityType;
    this.databaseRegistry = databaseRegistry;
  }

  @Override
  public ValueTable getValueTable() {
    if(getDatasource() == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
    return getDatasource().getValueTable(getTableName());
  }

  @Override
  public ValueTableWriter createValueTableWriter() {
    if(getDatasource() == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
    return getDatasource().createWriter(getTableName(), entityType);
  }

  @Override
  public boolean hasValueTable() {
    if(getDatasource() == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
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

  @Nonnull
  @Override
  public String getEntityType() {
    return entityType;
  }

  @Nonnull
  @Override
  public String getTableReference() {
    return tableReference;
  }

  private MagmaEngineReferenceResolver getTableResolver() {
    if(tableResolver == null) {
      tableResolver = MagmaEngineTableResolver.valueOf(tableReference);
    }
    return tableResolver;
  }

  @Override
  public boolean isRunning() {
    return getDatasource() != null;
  }

  @Override
  public void start() {
    getDatasource();
  }

  @Override
  public void stop() {
    new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          Disposables.dispose(getDatasource());
        } catch(RuntimeException e) {
          log.warn("Ignoring exception during shutdown sequence.", e);
        }
      }
    });
  }

  @Override
  public String getName() {
    return "identifiers";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  private Datasource getDatasource() {
    if(datasource == null) {
      Database database = databaseRegistry.getIdentifiersDatabase();
      if(database != null) {
        datasource = databaseRegistry.createStorageMagmaDatasource(getDatasourceName(), database);
        initialise();
      }
    }
    return datasource;
  }

  private void initialise() {
    if(datasource == null) return;
    new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {

      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          Initialisables.initialise(datasource);
          if(!datasource.hasValueTable(getTableName())) {
            datasource.createWriter(getTableName(), getEntityType()).close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

}
