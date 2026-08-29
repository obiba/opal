/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import jakarta.validation.constraints.NotNull;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.event.OpalGeneralConfigUpdatedEvent;
import org.obiba.opal.core.repository.OpalGeneralConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of System Service
 */
@Component
public class OpalGeneralConfigServiceImpl implements OpalGeneralConfigService {

  private final OpalGeneralConfigRepository opalGeneralConfigRepository;

  private final EventBus eventBus;

  @Autowired
  public OpalGeneralConfigServiceImpl(OpalGeneralConfigRepository opalGeneralConfigRepository, EventBus eventBus) {
    this.opalGeneralConfigRepository = opalGeneralConfigRepository;
    this.eventBus = eventBus;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void save(@NotNull final OpalGeneralConfig config) {
    opalGeneralConfigRepository.upsert(config);
    eventBus.post(new OpalGeneralConfigUpdatedEvent(config));
  }

  @Override
  @NotNull
  public OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException {
    return opalGeneralConfigRepository.findConfig().orElseThrow(OpalGeneralConfigMissingException::new);
  }

}
