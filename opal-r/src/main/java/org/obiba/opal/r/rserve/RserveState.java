/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rserve;

import com.google.common.collect.Lists;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerState;

import java.util.List;

public class RserveState implements RServerState {

  private boolean running;

  private Integer port;

  private String encoding;

  private int rSessionCount = 0;

  private int busyRSessionCount = 0;

  public void setRunning(boolean running) {
    this.running = running;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setRSessionCount(int rSessionCount) {
    this.rSessionCount = rSessionCount;
  }

  public void setBusyRSessionCount(int busyRSessionCount) {
    this.busyRSessionCount = busyRSessionCount;
  }

  @Override
  public String getName() {
    return RserveService.RSERVE_NAME;
  }

  @Override
  public String getCluster() {
    return "default";
  }

  @Override
  public String getVersion() {
    return "?";
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  public Integer getPort() {
    return port;
  }

  public String getEncoding() {
    return encoding;
  }

  @Override
  public List<String> getTags() {
    return Lists.newArrayList("legacy");
  }

  @Override
  public int getRSessionsCount() {
    return rSessionCount;
  }

  @Override
  public int getBusyRSessionsCount() {
    return busyRSessionCount;
  }

  @Override
  public int getSystemCores() {
    return 0;
  }

  @Override
  public int getSystemFreeMemory() {
    return 0;
  }
}
