package org.obiba.opal.core.upgrade.database;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveVersionToOpalConfigurationUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private VersionProvider versionProvider;

  @Autowired
  private OpalConfigurationService configurationService;

  @Override
  public void execute(Version currentVersion) {
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setVersion(versionProvider.getVersion());
      }
    });

    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.execute("drop table version");
  }

}
