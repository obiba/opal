package org.obiba.opal.core.runtime.upgrade.database;

import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.jdbc.core.JdbcTemplate;

public class DeleteVersionTableUpgradeStep extends AbstractUpgradeStep {

  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.execute("drop table version");
  }

  public void setDatabaseRegistry(DatabaseRegistry databaseRegistry) {
    this.databaseRegistry = databaseRegistry;
  }

}
