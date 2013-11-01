package org.obiba.opal.core.service;

import javax.annotation.Nonnull;

import org.obiba.opal.core.domain.OpalGeneralConfig;

public interface OpalGeneralConfigService extends SystemService {

  void save(@Nonnull OpalGeneralConfig config);

  @Nonnull
  OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException;
}
