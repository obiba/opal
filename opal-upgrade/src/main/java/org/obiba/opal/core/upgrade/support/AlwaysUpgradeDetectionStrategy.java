package org.obiba.opal.core.upgrade.support;

import org.obiba.runtime.upgrade.VersionProvider;
import org.obiba.runtime.upgrade.support.NewInstallationDetectionStrategy;

public class AlwaysUpgradeDetectionStrategy implements NewInstallationDetectionStrategy {

  @Override
  @SuppressWarnings("MethodReturnAlwaysConstant")
  public boolean isNewInstallation(VersionProvider runtimeVersionProvider) {
    return false;
  }

}
