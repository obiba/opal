package org.obiba.opal.core.runtime.upgrade.database;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;

public class UpdateOpalConfigurationFlagUpgradeStep extends AbstractUpgradeStep {

  private OpalConfigurationService configurationService;

  @Override
  public void execute(Version currentVersion) {
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setMigratedToOpal2(true);
      }
    });
  }

  public void setConfigurationService(OpalConfigurationService configurationService) {
    this.configurationService = configurationService;
  }
}
