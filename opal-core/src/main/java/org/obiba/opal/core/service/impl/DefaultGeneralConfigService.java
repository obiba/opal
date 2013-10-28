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

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of System Service
 */
@Component
public class DefaultGeneralConfigService implements SystemService {

  @Autowired
  private OrientDbDocumentService orientDbDocumentService;

  @Override
  @PostConstruct
  public void start() {
    orientDbDocumentService.createUniqueStringIndex(OpalGeneralConfig.class, "name");
  }

  @Override
  public void stop() {
  }

  public void save(@Nonnull OpalGeneralConfig config) {
    orientDbDocumentService.save(config);
  }

  @Nonnull
  public OpalGeneralConfig getConfig() throws OpalGeneralConfigMissingException {
    Iterator<OpalGeneralConfig> iterator = orientDbDocumentService.list(OpalGeneralConfig.class).iterator();
    if(iterator.hasNext()) return iterator.next();
    throw new OpalGeneralConfigMissingException();
  }

}
