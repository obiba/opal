package org.obiba.opal.core.upgrade.support;

import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionModifier;
import org.springframework.beans.factory.annotation.Autowired;

public class OpalVersionModifier implements VersionModifier {

  @Autowired
  private OpalConfigurationService configurationService;

  @Override
  public Version getVersion() {
    return configurationService.getOpalConfiguration().getVersion();
  }

  @Override
  public void setVersion(final Version version) {
    configurationService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {
      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.setVersion(version);
      }
    });
  }
}
