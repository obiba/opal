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

import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.datasource.mongodb.MongoDBDatasource;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.ServiceNotRunningException;
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

import com.google.common.base.Strings;
import com.mongodb.MongoClientURI;

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

  @Nullable
  private final String mongoURI;

  private MagmaEngineReferenceResolver tableResolver;

  @Nullable
  private SessionFactory keysSession;

  @Nullable
  private Datasource datasource;

  @Autowired
  public DefaultIdentifiersTableService(@Nonnull PlatformTransactionManager txManager,
      @Nonnull @Value("${org.obiba.opal.keys.tableReference}") String tableReference,
      @Nonnull @Value("${org.obiba.opal.keys.entityType}") String entityType,
      @Nullable @Value("${org.obiba.opal.mongo.ids.uri}") String mongoURI) {
    Assert.notNull(txManager, "txManager cannot be null");
    Assert.notNull(tableReference, "tableReference cannot be null");
    Assert.notNull(entityType, "entityType cannot be null");
    this.txManager = txManager;
    this.tableReference = tableReference;
    this.entityType = entityType;
    this.mongoURI = mongoURI;
  }

  public void setKeysSessionFactory(SessionFactory keysSession) {
    this.keysSession = keysSession;
  }

  @Override
  public ValueTable getValueTable() {
    if(datasource == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
    return datasource.getValueTable(getTableName());
  }

  @Override
  public ValueTableWriter createValueTableWriter() {
    if(datasource == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
    return datasource.createWriter(getTableName(), entityType);
  }

  @Override
  public boolean hasValueTable() {
    if(datasource == null) {
      throw new ServiceNotRunningException(getName(), "IdentifiersTableService has no datasource configured");
    }
    return datasource.hasValueTable(getTableName());
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
    return datasource != null;
  }

  @Override
  public void start() {
    if(Strings.isNullOrEmpty(mongoURI) && keysSession != null) {
      log.info("Identifiers table storage is a SQL database");
      datasource = new HibernateDatasource(getDatasourceName(), keysSession);
    } else {
      log.info("Identifiers table storage is a MongoDB database");
      datasource = new MongoDBDatasource(getDatasourceName(), new MongoClientURI(mongoURI));
    }
    initialise();
  }

  @Override
  public void stop() {
    new TransactionTemplate(txManager).execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          Disposables.dispose(datasource);
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
