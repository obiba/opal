/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.impl;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.obiba.opal.core.domain.HasUniqueProperties;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of System Service
 */
@Component
public class DefaultGeneralConfigService implements SystemService {

  private static final HasUniqueProperties TEMPLATE = new OpalGeneralConfig();

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(OpalGeneralConfig.class);
  }

  @Override
  public void stop() {
  }

  public void save(@SuppressWarnings("TypeMayBeWeakened") @Nonnull OpalGeneralConfig config) {
    orientDbService.save(config, config);
  }

  @Nonnull
  public OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException {
    OpalGeneralConfig config = orientDbService.findUnique(TEMPLATE);
    if(config == null) throw new OpalGeneralConfigMissingException();
    return config;
  }

}
