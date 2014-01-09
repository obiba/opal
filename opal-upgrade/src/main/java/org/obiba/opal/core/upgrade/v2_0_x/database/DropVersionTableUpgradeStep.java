package org.obiba.opal.core.upgrade.v2_0_x.database;

import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class DropVersionTableUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.execute("drop table version");
  }

}
