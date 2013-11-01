package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import org.obiba.opal.core.domain.OpalGeneralConfig;

public interface OpalGeneralConfigService extends SystemService {

  void save(@NotNull OpalGeneralConfig config);

  @NotNull
  OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException;
}
