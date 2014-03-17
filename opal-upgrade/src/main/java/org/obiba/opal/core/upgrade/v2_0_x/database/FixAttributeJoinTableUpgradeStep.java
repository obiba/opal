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
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class FixAttributeJoinTableUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(FixAttributeJoinTableUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

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

    alter(jdbcTemplate, "ALTER TABLE datasource_state_attributes RENAME TO datasource_attributes");
    alter(jdbcTemplate, "ALTER TABLE datasource_attributes RENAME COLUMN datasource_state_id to datasource_id");
    alter(jdbcTemplate, "ALTER TABLE datasource_attributes RENAME COLUMN datasource to datasource_id");

    alter(jdbcTemplate, "ALTER TABLE variable_state_attributes RENAME TO variable_attributes");
    alter(jdbcTemplate, "ALTER TABLE variable_attributes RENAME COLUMN variable_state_id to variable_id");
    alter(jdbcTemplate, "ALTER TABLE variable_attributes RENAME COLUMN variable to variable_id");

    alter(jdbcTemplate, "ALTER TABLE category_state_attributes RENAME TO category_attributes");
    alter(jdbcTemplate, "ALTER TABLE category_attributes RENAME COLUMN category_state_id to category_id");
    alter(jdbcTemplate, "ALTER TABLE category_attributes RENAME COLUMN category to category_id");

    touchValueTablesTimestamps(database);
  }

  private void alter(JdbcOperations jdbcTemplate, String sql) {
    try {
      log.debug("Execute '{}'", sql);
      jdbcTemplate.execute(sql);
    } catch(DataAccessException e) {
      log.debug("Ignore error during '{}'", sql, e);
    }
  }

  @SuppressWarnings("unchecked")
  private void touchValueTablesTimestamps(Database database) {
    SessionFactory sessionFactory = databaseRegistry.getSessionFactory(database.getName(), null);
    Session currentSession = sessionFactory.getCurrentSession();
    for(ValueTableState table : (List<ValueTableState>) currentSession.createCriteria(ValueTableState.class).list()) {
      log.debug("Touch {} last update", table.getName());
      currentSession.buildLockRequest(new LockOptions(LockMode.PESSIMISTIC_FORCE_INCREMENT)).lock(table);
    }
  }

}
