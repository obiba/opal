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

  private final RockServerInfo info;

  public RockState(RockServerInfo info) {
    this.info = info;
  }

  @Override
  public String getName() {
    return info.getId();
  }

  @Override
  public String getVersion() {
    return info.getVersion();
  }

  @Override
  public boolean isRunning() {
    return info.getRServerStatus().getRunning();
  }

  @Override
  public List<String> getTags() {
    return info.getTags();
  }

  @Override
  public int getRSessionsCount() {
    if (info.getRServerStatus().getRSessionsCounts() != null) return 0;
    return (Integer) info.getRServerStatus().getRSessionsCounts().getAdditionalProperties().getOrDefault("total", 0);
  }

  @Override
  public int getBusyRSessionsCount() {
    if (info.getRServerStatus().getRSessionsCounts() != null) return 0;
    return (Integer) info.getRServerStatus().getRSessionsCounts().getAdditionalProperties().getOrDefault("busy", 0);
  }
}
