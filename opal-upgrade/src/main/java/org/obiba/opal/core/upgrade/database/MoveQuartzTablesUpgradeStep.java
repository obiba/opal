package org.obiba.opal.core.upgrade.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

import static org.springframework.util.StringUtils.collectionToDelimitedString;

public class MoveQuartzTablesUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MoveQuartzTablesUpgradeStep.class);

  private static final String[] TABLES = { "QRTZ_BLOB_TRIGGERS", "QRTZ_CALENDARS", "QRTZ_CRON_TRIGGERS",
      "QRTZ_FIRED_TRIGGERS", "QRTZ_JOB_DETAILS", "QRTZ_JOB_LISTENERS", "QRTZ_PAUSED_TRIGGER_GRPS",
      "QRTZ_SCHEDULER_STATE", "QRTZ_SIMPLE_TRIGGERS", "QRTZ_TRIGGER_LISTENERS", "QRTZ_TRIGGERS" };

  private static final String[] DELETE_TABLES = { "QRTZ_BLOB_TRIGGERS", "QRTZ_CALENDARS", "QRTZ_CRON_TRIGGERS",
      "QRTZ_FIRED_TRIGGERS", "QRTZ_JOB_LISTENERS", "QRTZ_PAUSED_TRIGGER_GRPS", "QRTZ_SCHEDULER_STATE",
      "QRTZ_SIMPLE_TRIGGERS", "QRTZ_TRIGGER_LISTENERS", "QRTZ_TRIGGERS", "QRTZ_JOB_DETAILS", "QRTZ_LOCKS" };

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

  private void copyTable(final String table, JdbcTemplate dataJdbcTemplate, JdbcTemplate configJdbcTemplate) {
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
          log.debug("{} row {}: {}", table, i, row);
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
