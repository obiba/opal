/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

public class RServerState {

  private boolean isRunning;

  private Integer port;

  private String encoding;

  public void setRunning(boolean running) {
    isRunning = running;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public Integer getPort() {
    return port;
  }

  public String getEncoding() {
    return encoding;
  }
}
