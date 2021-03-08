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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.opal.r.service.RServerState;

import java.util.List;
import java.util.Set;

class RServerClusterState implements RServerState {

  private final String name;

  private String version;

  private boolean running;

  private int sessionsCount = 0;

  private int busySessionsCount = 0;

  private int systemCores = 0;

  private int systemFreeMemory = 0;

  private final Set<String> tags = Sets.newHashSet();

  public RServerClusterState(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCluster() {
    return getName();
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public String getVersion() {
    return version;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  public void addTags(List<String> tags) {
    this.tags.addAll(tags);
  }

  @Override
  public List<String> getTags() {
    return Lists.newArrayList(tags);
  }

  public void addRSessionsCount(int count) {
    sessionsCount = sessionsCount + count;
  }

  @Override
  public int getRSessionsCount() {
    return sessionsCount;
  }

  public void addBusyRSessionsCount(int count) {
    busySessionsCount = busySessionsCount + count;
  }

  @Override
  public int getBusyRSessionsCount() {
    return busySessionsCount;
  }

  public void addSystemCores(int systemCores) {
    this.systemCores = systemCores + Math.max(systemCores, 0);
  }

  @Override
  public int getSystemCores() {
    return systemCores;
  }

  public void addSystemFreeMemory(int systemFreeMemory) {
    this.systemFreeMemory = systemFreeMemory + Math.max(systemFreeMemory, 0);
  }

  @Override
  public int getSystemFreeMemory() {
    return systemFreeMemory;
  }
}
