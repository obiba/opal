package org.obiba.opal.core.event;

import org.obiba.opal.core.domain.OpalGeneralConfig;

public class OpalGeneralConfigUpdatedEvent {

  private final OpalGeneralConfig config;

  public OpalGeneralConfigUpdatedEvent(OpalGeneralConfig config) {
    this.config = config;
  }

  public OpalGeneralConfig getConfig() {
    return config;
  }
}
