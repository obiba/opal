/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.datashield.cfg;

import com.google.common.base.Strings;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.opal.core.service.SystemService;
import org.obiba.opal.r.service.RServerManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class DatashieldConfigService implements SystemService {

  private static final Logger log = LoggerFactory.getLogger(DatashieldConfigService.class);

  private final OrientDbService orientDbService;

  private final Lock configLock = new ReentrantLock();

  public DatashieldConfigService(OrientDbService orientDbService) {
    this.orientDbService = orientDbService;
  }

  public DatashieldConfig getConfiguration(String profile) {
    configLock.lock();
    try {
      String p = Strings.isNullOrEmpty(profile) ? RServerManagerService.DEFAULT_CLUSTER_NAME : profile;
      DatashieldConfig config = orientDbService.findUnique(new DatashieldConfig(p));
      if (config == null)
        config = new DatashieldConfig(p);
      return config;
    } finally {
      configLock.unlock();
    }
  }

  public void saveConfiguration(DatashieldConfig config) {
    configLock.lock();
    try {
      orientDbService.save(config, config);
    } finally {
      configLock.unlock();
    }
  }

  public void deleteConfiguration(DatashieldConfig config) {
    configLock.lock();
    try {
      orientDbService.delete(config);
    } finally {
      configLock.unlock();
    }
  }

  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(DatashieldConfig.class);
  }

  @Override
  @PreDestroy
  public void stop() {

  }
}
