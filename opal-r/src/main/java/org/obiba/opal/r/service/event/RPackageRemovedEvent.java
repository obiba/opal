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

public class RPackageRemovedEvent {

  private final String cluster;

  private final String packageName;

  public RPackageRemovedEvent(String cluster, String packageName) {
    this.cluster = cluster;
    this.packageName = packageName;
  }

  public String getCluster() {
    return cluster;
  }

  public String getPackageName() {
    return packageName;
  }
}
