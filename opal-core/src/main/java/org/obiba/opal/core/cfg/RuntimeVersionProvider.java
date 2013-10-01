package org.obiba.opal.core.cfg;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.VersionProvider;

/**
 * Need this as we don't want a spring bean Version that would be autowire to OpalConfiguration automatically
 */
public class RuntimeVersionProvider implements VersionProvider {

  private final String versionString;

  public RuntimeVersionProvider(String versionString) {
    this.versionString = versionString;
  }

  @Override
  public Version getVersion() {
    return new Version(versionString);
  }

}
