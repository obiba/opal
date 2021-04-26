/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service.event;

import com.google.common.base.Strings;

public class RServerServiceStartedEvent {

  private final String cluster;

  private final String name;

  public RServerServiceStartedEvent(String cluster) {
    this.cluster = cluster;
    this.name = null;
  }

  public RServerServiceStartedEvent(String cluster, String name) {
    this.cluster = cluster;
    this.name = name;
  }

  public String getCluster() {
    return cluster;
  }

  public String getName() {
    return name;
  }

  public boolean hasName() {
    return !Strings.isNullOrEmpty(name);
  }
}
