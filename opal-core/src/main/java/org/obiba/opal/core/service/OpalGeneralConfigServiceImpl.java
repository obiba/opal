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

import javax.validation.constraints.NotNull;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.event.OpalGeneralConfigUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

/**
 * Default implementation of System Service
 */
@Component
public class OpalGeneralConfigServiceImpl implements OpalGeneralConfigService {

  private static final Logger log = LoggerFactory.getLogger(OpalGeneralConfigServiceImpl.class);

  private final OrientDbService orientDbService;

  private final EventBus eventBus;

  @Autowired
  public OpalGeneralConfigServiceImpl(OrientDbService orientDbService, EventBus eventBus) {
    this.orientDbService = orientDbService;
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
    OpalGeneralConfig existing = getConfigOrNull();
    if (existing == null) {
      orientDbService.save(null, config);
    } else {
      orientDbService.save(existing, config);
    }
    log.debug("save {}", config);
    eventBus.post(new OpalGeneralConfigUpdatedEvent(config));
  }

  @Override
  @NotNull
  public OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException {
    OpalGeneralConfig config = getConfigOrNull();
    if (config == null) {
      throw new OpalGeneralConfigMissingException();
    }
    return config;
  }

  private OpalGeneralConfig getConfigOrNull() {
    return Iterables.getFirst(orientDbService.list(OpalGeneralConfig.class), null);
  }
}
