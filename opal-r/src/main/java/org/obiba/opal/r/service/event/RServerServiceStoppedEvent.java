/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service.event;

public class RServerServiceStoppedEvent {

  private final String cluster;

  private final String name;

  public RServerServiceStoppedEvent(String cluster, String name) {
    this.cluster = cluster;
    this.name = name;
  }

  public String getCluster() {
    return cluster;
  }

  public String getName() {
    return name;
  }
}
