/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.cfg;

import com.google.common.eventbus.Subscribe;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableStatus;
import org.obiba.magma.views.DefaultViewManagerImpl;
import org.obiba.opal.audit.OpalUserProvider;
import org.obiba.opal.core.service.event.ResourceProvidersServiceStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OpalViewManager extends DefaultViewManagerImpl {

  private static final Logger log = LoggerFactory.getLogger(OpalViewManager.class);

  @Autowired
  public OpalViewManager(OpalViewPersistenceStrategy viewPersistenceStrategy) {
    super(viewPersistenceStrategy);
  }

  @Subscribe
  public synchronized void onResourceProvidersServiceStarted(ResourceProvidersServiceStartedEvent event) {
    log.info("Resource providers ready, scanning for views to initialise...");
    MagmaEngine.get().getDatasources().stream()
        .forEach(datasource -> {
          datasource.getValueTables().stream()
              .filter(ValueTable::isView)
              .filter(view -> ValueTableStatus.ERROR.equals(view.getStatus()))
              .forEach(view -> {
                log.info("Initialise {}.{}", datasource.getName(), view.getName());
                try {
                  initView(datasource.getName(), view.getName());
                } catch (Exception e) {
                  log.error("{}", e.getMessage());
                }
              });
        });
  }
}
