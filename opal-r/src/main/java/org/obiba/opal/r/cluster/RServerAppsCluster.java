/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.cluster;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.r.service.RServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Rock instances provided by an App (either discovered or self-registered) are grouped in a cluster.
 */
public class RServerAppsCluster extends RServerCluster {

  private static final Logger log = LoggerFactory.getLogger(RServerAppsCluster.class);

  public RServerAppsCluster(String name, EventBus eventBus) {
    super(name, eventBus);
  }

  public void removeRServerService(App app) {
    try {
      Optional<RServerService> service = rServerServices.stream()
          .filter(s -> s.isFor(app)).findFirst();
      service.ifPresent(rServerServices::remove);
    } catch (Exception e) {
      // ignored
    }
  }
}
