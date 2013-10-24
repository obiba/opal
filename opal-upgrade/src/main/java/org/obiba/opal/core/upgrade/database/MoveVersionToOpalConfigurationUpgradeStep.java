package org.obiba.opal.core.upgrade.database;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationIo;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.obiba.runtime.upgrade.VersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveVersionToOpalConfigurationUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private VersionProvider versionProvider;

  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void execute(Version currentVersion) {
    OpalConfigurationIo opalConfigurationIo = new OpalConfigurationIo();
    applicationContext.getAutowireCapableBeanFactory().autowireBean(opalConfigurationIo);

    OpalConfiguration configuration = opalConfigurationIo.readConfiguration();
    configuration.setVersion(versionProvider.getVersion());
    opalConfigurationIo.writeConfiguration(configuration);

    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    dataJdbcTemplate.execute("drop table version");
  }

}
