/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.magma.datasource.hibernate.domain.ValueTableState;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class FixAttributeJoinTableUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(FixAttributeJoinTableUpgradeStep.class);

  private static final int FK_NAME_COLUMN_INDEX = 12;

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Override
  public void execute(Version currentVersion) {
    process(databaseRegistry.getIdentifiersDatabase());
    for(Database database : databaseRegistry.listSqlDatabases()) {
      process(database);
    }
  }

  private void process(Database database) {
    if(database.getSqlSettings() == null ||
        database.getSqlSettings().getSqlSchema() != SqlSettings.SqlSchema.HIBERNATE) {
      return;
    }

    log.info("Upgrade database {}: {}", database.getName(), database.getSqlSettings().getUrl());

    JdbcOperations jdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource(database.getName(), null));

    switch(database.getSqlSettings().getDriverClass()) {
      case "com.mysql.jdbc.Driver":
        alterMySql(jdbcTemplate);
        break;
      case "org.hsqldb.jdbc.JDBCDriver":
        alterHsql(jdbcTemplate);
        break;
    }

    touchValueTablesTimestamps(database);
  }

  private void alterMySql(JdbcOperations jdbcTemplate) {
    alterAttributeMySql(jdbcTemplate, "datasource");
    alterAttributeMySql(jdbcTemplate, "variable");
    alterAttributeMySql(jdbcTemplate, "category");
  }

  private void alterAttributeMySql(JdbcOperations jdbcTemplate, String attributeAware) {
    // copy from variable_attributes to variable_state_attributes if exists
    copyAttributes(jdbcTemplate, attributeAware);

    // drop variable_attributes if exists
    execute(jdbcTemplate, "DROP TABLE " + attributeAware + "_attributes");

    // rename variable_state_attributes to variable_attributes if exists
    execute(jdbcTemplate,
        "ALTER TABLE " + attributeAware + "_state_attributes RENAME TO " + attributeAware + "_attributes");

    // drop variable_attributes foreign key if exists
    execute(jdbcTemplate, "ALTER TABLE " + attributeAware + "_attributes DROP FOREIGN KEY " +
        getForeignKey(jdbcTemplate, attributeAware + "_attributes"));

    // rename variable_state_id to variable_id if exists
    execute(jdbcTemplate, "ALTER TABLE " + attributeAware +
        "_attributes CHANGE COLUMN " + attributeAware + "_state_id " + attributeAware + "_id bigint(20) NOT NULL");

    // rename variable to variable_id if exists
    execute(jdbcTemplate,
        "ALTER TABLE " + attributeAware + "_attributes CHANGE COLUMN " + attributeAware + " " + attributeAware +
            "_id bigint(20) NOT NULL");
  }

  private void alterHsql(JdbcOperations jdbcTemplate) {
    alterAttributeHsql(jdbcTemplate, "datasource");
    alterAttributeHsql(jdbcTemplate, "variable");
    alterAttributeHsql(jdbcTemplate, "category");
  }

  private void alterAttributeHsql(JdbcOperations jdbcTemplate, String attributeAware) {
    // copy from variable_attributes to variable_state_attributes if exists
    copyAttributes(jdbcTemplate, attributeAware);

    // drop variable_attributes if exists
    execute(jdbcTemplate, "DROP TABLE " + attributeAware + "_attributes");

    // rename variable_state_attributes to variable_attributes if exists
    execute(jdbcTemplate,
        "ALTER TABLE " + attributeAware + "_state_attributes RENAME TO " + attributeAware + "_attributes");

    // drop variable_attributes foreign key if exists
    execute(jdbcTemplate, "ALTER TABLE " + attributeAware + "_attributes DROP CONSTRAINT " +
        getForeignKey(jdbcTemplate, attributeAware + "_attributes"));

    // rename variable_state_id to variable_id if exists
    execute(jdbcTemplate, "ALTER TABLE " + attributeAware +
        "_attributes ALTER COLUMN " + attributeAware + "_state_id RENAME TO " + attributeAware + "_id");

    // rename variable to variable_id if exists
    execute(jdbcTemplate,
        "ALTER TABLE " + attributeAware + "_attributes ALTER COLUMN " + attributeAware + " RENAME TO " +
            attributeAware +
            "_id");
  }

  private String getForeignKey(JdbcOperations jdbcTemplate, final String tableName) {
    return jdbcTemplate.execute(new ConnectionCallback<String>() {
      @Override
      public String doInConnection(Connection connection) throws SQLException, DataAccessException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getImportedKeys(null, null, tableName);
        return rs.next() ? rs.getString(FK_NAME_COLUMN_INDEX) : null;
      }
    });
  }

  private void copyAttributes(final JdbcOperations jdbcTemplate, final String attributeAware) {
    try {
      jdbcTemplate
          .query("select " + attributeAware + "_id, locale, name, namespace, value_type, is_sequence, value from " +
              attributeAware + "_attributes", new RowCallbackHandler() {
            @Override
            public void processRow(final ResultSet rs) throws SQLException {
              transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                  try {
                    long id = rs.getLong(attributeAware + "_id");
                    String locale = rs.getString("locale");
                    String name = rs.getString("name");
                    String namespace = rs.getString("namespace");
                    String value_type = rs.getString("value_type");
                    boolean is_sequence = rs.getBoolean("is_sequence");
                    String value = rs.getString("value");
                    int count = jdbcTemplate.queryForObject(
                        "SELECT count(*) FROM " + attributeAware + "_state_attributes WHERE " + attributeAware +
                            "_state_id = ? AND locale = ? AND name = ? AND namespace = ? AND value_type = ? " +
                            "AND is_sequence = ? AND value = ?", Integer.class, id, locale, name, namespace, value_type,
                        is_sequence, value);

                    log.info("SELECT count(*) FROM " + attributeAware + "_state_attributes WHERE " + attributeAware +
                        "_state_id = {} AND locale = '{}' AND name = '{}' AND namespace = '{}' AND value_type = '{}' " +
                        "AND is_sequence = {} AND value = '{}'", id, locale, name, namespace, value_type, is_sequence,
                        value);
                    if(count == 0) {
                      jdbcTemplate.update("insert into " + attributeAware + "_state_attributes (" + attributeAware +
                          "_state_id, locale, name, namespace, value_type, is_sequence, value) VALUES (?, ?, ?, ?, ?, ?, ?)",
                          id, locale, name, namespace, value_type, is_sequence, value);
                    }
                  } catch(SQLException e) {
                    log.info("Ignore attribute copy error", e);
                  }
                }
              });
            }
          });
    } catch(DataAccessException e) {
      log.info("Ignore attribute copy error", e);
    }
  }

  private void execute(final JdbcOperations jdbcTemplate, final String sql) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {
          jdbcTemplate.execute(sql);
          log.debug("Executed '{}'", sql);
        } catch(DataAccessException ignored) {
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  private void touchValueTablesTimestamps(final Database database) {
    transactionTemplate.execute(new TransactionCallbackWithoutResult() {
      @Override
      protected void doInTransactionWithoutResult(TransactionStatus status) {
        SessionFactory sessionFactory = databaseRegistry.getSessionFactory(database.getName(), null);
        Session currentSession = sessionFactory.getCurrentSession();
        for(ValueTableState table : (List<ValueTableState>) currentSession.createCriteria(ValueTableState.class)
            .list()) {
          log.debug("Touch {} last update", table.getName());
          currentSession.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(table);
        }
      }
    });
  }

}
