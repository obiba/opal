/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.storage;

/**
 * The last reading taken of one volume Opal writes to. One reading per volume, not per folder: several of the folders
 * Opal watches are usually on the same mount, and reporting them separately would report the same number several
 * times.
 */
public class DiskStatus {

  /**
   * What Opal keeps on this volume, as a comma separated list of the watched folder names.
   */
  private final String name;

  /**
   * One of the watched folders on this volume, as an absolute path.
   */
  private final String path;

  private final long totalSpace;

  private final long usableSpace;

  private final DiskLevel level;

  public DiskStatus(String name, String path, long totalSpace, long usableSpace, DiskLevel level) {
    this.name = name;
    this.path = path;
    this.totalSpace = totalSpace;
    this.usableSpace = usableSpace;
    this.level = level;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public long getTotalSpace() {
    return totalSpace;
  }

  public long getUsableSpace() {
    return usableSpace;
  }

  public DiskLevel getLevel() {
    return level;
  }

  @Override
  public String toString() {
    return name + " (" + path + "): " + usableSpace + " of " + totalSpace + " bytes free, " + level;
  }
}
