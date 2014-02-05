package org.obiba.opal.core.upgrade.v2_0_x.database;

import javax.sql.DataSource;

import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public class MoveQuartzTablesUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(MoveQuartzTablesUpgradeStep.class);

  private static final String[] DELETE_TABLES = { "QRTZ_LOCKS", "QRTZ_SCHEDULER_STATE", "QRTZ_FIRED_TRIGGERS",
      "QRTZ_PAUSED_TRIGGER_GRPS", "QRTZ_CALENDARS", "QRTZ_TRIGGER_LISTENERS", "QRTZ_BLOB_TRIGGERS",
      "QRTZ_CRON_TRIGGERS", "QRTZ_SIMPLE_TRIGGERS", "QRTZ_TRIGGERS", "QRTZ_JOB_LISTENERS", "QRTZ_JOB_DETAILS" };

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private DataSource configDataSource;

  @Override
  public void execute(Version currentVersion) {
    JdbcOperations dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    JdbcOperations configJdbcTemplate = new JdbcTemplate(configDataSource);
    // no need to copy data as BIRT reports are removed from config
    for(String table : DELETE_TABLES) {
      dataJdbcTemplate.execute("drop table " + table);
    }
  }

}
