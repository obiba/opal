/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import org.obiba.opal.r.service.RServerState;

import java.util.List;

public class RockState implements RServerState {

  private final String name;

  private final RockServerStatus info;

  public RockState(RockServerStatus info, String name) {
    this.info = info;
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getCluster() {
    return info.getCluster();
  }

  @Override
  public String getVersion() {
    return info.getVersion();
  }

  @Override
  public boolean isRunning() {
    return info.getRunning();
  }

  @Override
  public List<String> getTags() {
    return info.getTags();
  }

  @Override
  public int getRSessionsCount() {
    if (info.getSessions() == null) return 0;
    return (Integer) info.getSessions().getAdditionalProperties().getOrDefault("total", 0);
  }

  @Override
  public int getBusyRSessionsCount() {
    if (info.getSessions() == null) return 0;
    return (Integer) info.getSessions().getAdditionalProperties().getOrDefault("busy", 0);
  }

  @Override
  public int getSystemCores() {
    if (info.getSystem() == null) return 0;
    return (Integer) info.getSystem().getAdditionalProperties().getOrDefault("cores", 0);
  }

  @Override
  public int getSystemFreeMemory() {
    if (info.getSystem() == null) return 0;
    return (Integer) info.getSystem().getAdditionalProperties().getOrDefault("freeMemory", 0);
  }
}
