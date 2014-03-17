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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class FixAttributeJoinTableUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(FixAttributeJoinTableUpgradeStep.class);

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
    alter(jdbcTemplate, "ALTER TABLE datasource_state_attributes RENAME TO datasource_attributes");
    alter(jdbcTemplate,
        "ALTER TABLE datasource_attributes DROP FOREIGN KEY " + getForeignKey(jdbcTemplate, "datasource_attributes"));
    alter(jdbcTemplate,
        "ALTER TABLE datasource_attributes CHANGE COLUMN datasource_state_id datasource_id bigint(20) NOT NULL");
    alter(jdbcTemplate, "ALTER TABLE datasource_attributes CHANGE COLUMN datasource datasource_id bigint(20) NOT NULL");

    alter(jdbcTemplate, "ALTER TABLE variable_state_attributes RENAME TO variable_attributes");
    alter(jdbcTemplate,
        "ALTER TABLE variable_attributes DROP FOREIGN KEY " + getForeignKey(jdbcTemplate, "variable_attributes"));
    alter(jdbcTemplate,
        "ALTER TABLE variable_attributes CHANGE COLUMN variable_state_id variable_id bigint(20) NOT NULL");
    alter(jdbcTemplate, "ALTER TABLE variable_attributes CHANGE COLUMN variable variable_id bigint(20) NOT NULL");

    alter(jdbcTemplate, "ALTER TABLE category_state_attributes RENAME TO category_attributes");
    alter(jdbcTemplate,
        "ALTER TABLE category_attributes DROP FOREIGN KEY " + getForeignKey(jdbcTemplate, "category_attributes"));
    alter(jdbcTemplate,
        "ALTER TABLE category_attributes CHANGE COLUMN category_state_id category_id bigint(20) NOT NULL");
    alter(jdbcTemplate, "ALTER TABLE category_attributes CHANGE COLUMN category category_id bigint(20) NOT NULL");
  }

  private void alterHsql(JdbcOperations jdbcTemplate) {
    alter(jdbcTemplate, "ALTER TABLE datasource_state_attributes RENAME TO datasource_attributes");
    alter(jdbcTemplate, "ALTER TABLE datasource_attributes ALTER COLUMN datasource_state_id RENAME TO datasource_id");
    alter(jdbcTemplate, "ALTER TABLE datasource_attributes ALTER COLUMN datasource RENAME TO datasource_id");
    alter(jdbcTemplate, "ALTER TABLE variable_state_attributes RENAME TO variable_attributes");
    alter(jdbcTemplate, "ALTER TABLE variable_attributes ALTER COLUMN variable_state_id RENAME TO variable_id");
    alter(jdbcTemplate, "ALTER TABLE variable_attributes ALTER COLUMN variable RENAME TO variable_id");

    alter(jdbcTemplate, "ALTER TABLE category_state_attributes RENAME TO category_attributes");
    alter(jdbcTemplate, "ALTER TABLE category_attributes ALTER COLUMN category_state_id RENAME TO category_id");
    alter(jdbcTemplate, "ALTER TABLE category_attributes ALTER COLUMN category RENAME TO category_id");
  }

  private String getForeignKey(JdbcOperations jdbcTemplate, final String tableName) {
    return jdbcTemplate.execute(new ConnectionCallback<String>() {
      @Override
      public String doInConnection(Connection connection) throws SQLException, DataAccessException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet rs = metaData.getImportedKeys(null, null, tableName);
        return rs.next() ? rs.getString(12) : null;
      }
    });
  }

  private void alter(final JdbcOperations jdbcTemplate, final String sql) {
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
