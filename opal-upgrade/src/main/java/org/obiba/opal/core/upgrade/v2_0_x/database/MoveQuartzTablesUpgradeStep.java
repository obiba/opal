/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.v2_0_x.database;

import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

public class MoveQuartzTablesUpgradeStep extends AbstractUpgradeStep {

  private static final String[] DELETE_TABLES = { "QRTZ_LOCKS", "QRTZ_SCHEDULER_STATE", "QRTZ_FIRED_TRIGGERS",
      "QRTZ_PAUSED_TRIGGER_GRPS", "QRTZ_CALENDARS", "QRTZ_TRIGGER_LISTENERS", "QRTZ_BLOB_TRIGGERS",
      "QRTZ_CRON_TRIGGERS", "QRTZ_SIMPLE_TRIGGERS", "QRTZ_TRIGGERS", "QRTZ_JOB_LISTENERS", "QRTZ_JOB_DETAILS" };

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    JdbcOperations dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    // no need to copy data as BIRT reports are removed from config
    for(String table : DELETE_TABLES) {
      dataJdbcTemplate.execute("drop table " + table);
    }
  }

}
