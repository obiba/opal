/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.kubernetes;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.r.cluster.RServerCluster;
import org.obiba.opal.r.service.RServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rock instances provided by an App (either discovered or self-registered) are grouped in a cluster.
 */
public class RServerPodsCluster extends RServerCluster {

  private static final Logger log = LoggerFactory.getLogger(RServerPodsCluster.class);

  public RServerPodsCluster(String name, EventBus eventBus) {
    super(name, eventBus);
  }

  @Override
  public void addRServerService(RServerService service) {
    if (!rServerServices.isEmpty()) rServerServices.removeFirst();
    super.addRServerService(service);
  }

  public void removeRServerService() {
    if (!rServerServices.isEmpty()) rServerServices.removeFirst();
  }
}
