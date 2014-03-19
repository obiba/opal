package org.obiba.opal.core.upgrade.v2_0_x.database;

import org.obiba.opal.core.service.database.DatabaseRegistry;
import org.obiba.opal.core.service.database.NoSuchDatabaseException;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class DropVersionTableUpgradeStep extends AbstractUpgradeStep {

  private static final Logger log = LoggerFactory.getLogger(DropVersionTableUpgradeStep.class);

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Override
  public void execute(Version currentVersion) {
    try {
      new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null)).execute("drop table version");
    } catch (NoSuchDatabaseException e) {
      // does not apply to this instance
    } catch(Exception e) {
      log.debug("Ignore error while dropping version table", e);
    }
  }

}
