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

import org.hibernate.SessionFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.datasource.hibernate.HibernateDatasource;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public class DefaultIdentifiersTableService implements IdentifiersTableService {

  private static final Logger log = LoggerFactory.getLogger(DefaultIdentifiersTableService.class);

  private final PlatformTransactionManager txManager;

  private final String tableReference;

  private final String entityType;

  private MagmaEngineReferenceResolver tableResolver;

  private SessionFactory keysSession;

  private Datasource datasource;

  @Autowired
  public DefaultIdentifiersTableService(PlatformTransactionManager txManager,
      @Value("${org.obiba.opal.keys.tableReference}") String keysTableReference,
      @Value("${org.obiba.opal.keys.entityType}") String keysTableEntityType) {
    if(txManager == null) throw new IllegalArgumentException("txManager cannot be null");
    if(keysTableReference == null) throw new IllegalArgumentException("keysTableReference cannot be null");
    if(keysTableEntityType == null) throw new IllegalArgumentException("keysTableEntityType cannot be null");

    this.txManager = txManager;
    tableReference = keysTableReference;
    entityType = keysTableEntityType;
  }

  public void setKeysSessionFactory(SessionFactory keysSession) {
    this.keysSession = keysSession;
  }

  @Override
  public ValueTable getValueTable() {
    return datasource.getValueTable(getTableName());
  }

  @Override
  public ValueTableWriter createValueTableWriter() {
    return datasource.createWriter(getTableName(), entityType);
  }

  @Override
  public boolean hasValueTable() {
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

  @Override
  public String getEntityType() {
    return entityType;
  }

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
    initialise(new HibernateDatasource(getDatasourceName(), keysSession));
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

  private void initialise(Datasource ds) {
    datasource = ds;
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
