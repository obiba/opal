package org.obiba.opal.core.runtime.upgrade.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

import static org.springframework.util.StringUtils.collectionToDelimitedString;

public class MoveConfigTablesUpgradeStep extends AbstractUpgradeStep {

  private static final String[] TABLES = { "user", "groups", "user_groups", "database_sql", "database_mongodb",
      "unit_key_store", "subject_acl" };

  // order is important because of foreign keys
  private static final String[] DELETE_TABLES = { "user_groups", "user", "groups", "database_sql", "database_mongodb",
      "unit_key_store", "subject_acl" };

  private DatabaseRegistry databaseRegistry;

  private DataSource configDataSource;

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    JdbcTemplate configJdbcTemplate = new JdbcTemplate(configDataSource);
    for(String table : TABLES) {
      copyTable(table, dataJdbcTemplate, configJdbcTemplate);
    }
    for(String table : DELETE_TABLES) {
      dataJdbcTemplate.execute("drop table " + table);
    }
  }

  private void copyTable(String table, JdbcTemplate dataJdbcTemplate, JdbcTemplate configJdbcTemplate) {
    final List<Map<String, Object>> rows = dataJdbcTemplate.queryForList("select * from " + table);

    if(!rows.isEmpty()) {
      Map<String, Object> map = rows.get(0);
      final List<String> columns = Lists.newArrayList(map.keySet());
      String sql = "insert into " + table + " (" + collectionToDelimitedString(columns, ", ") +
          ") values (" + collectionToDelimitedString(Collections.nCopies(map.size(), "?"), ", ") + ")";
      configJdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
          Map<String, Object> row = rows.get(i);
          for(int colIndex = 0; colIndex < columns.size(); colIndex++) {
            ps.setObject(colIndex + 1, row.get(columns.get(colIndex)));
          }
        }

        @Override
        public int getBatchSize() {
          return rows.size();
        }
      });
    }
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

  public void setConfigDataSource(DataSource configDataSource) {
    this.configDataSource = configDataSource;
  }
}
