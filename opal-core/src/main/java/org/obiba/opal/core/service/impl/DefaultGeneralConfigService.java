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

import javax.annotation.PostConstruct;

import org.obiba.opal.core.cfg.OrientDbService;
import org.obiba.opal.core.cfg.OrientDbTransactionCallback;
import org.obiba.opal.core.domain.server.OpalGeneralConfig;
import org.obiba.opal.core.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Default implementation of System Service
 */
@Component
public class DefaultGeneralConfigService implements SystemService {

  @Autowired
  private OrientDbService orientDbService;

  @Override
  @PostConstruct
  public void start() {
    orientDbService.registerEntityClass(OpalGeneralConfig.class);
  }

  @Override
  public void stop() {
  }

  public void updateServerConfig(final OpalGeneralConfig config) {

    orientDbService.execute(new OrientDbTransactionCallback<Object>() {
      @Override
      public Object doInTransaction(OObjectDatabaseTx db) {
        return db.save(config);
      }
    });
  }

  public OpalGeneralConfig getServerConfig() {
    // TODO: Once there is a default server config already created, we won't need to check if it exists.
    if(orientDbService.count(OpalGeneralConfig.class) == 0) {
      updateServerConfig(new OpalGeneralConfig());
    }

    return orientDbService.list(OpalGeneralConfig.class).iterator().next();
  }

}
