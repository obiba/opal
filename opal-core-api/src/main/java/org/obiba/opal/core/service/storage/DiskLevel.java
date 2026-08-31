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
 * How much room is left on a volume Opal writes to, as a decision rather than a number.
 * <p>
 * {@link #UNKNOWN} is deliberately not the least severe level but outside the scale: it means the volume could not be
 * measured, and a checker that cannot see must not be the reason a server stops accepting work. It therefore never
 * satisfies {@link #isAtLeast(DiskLevel)}, whatever is asked of it.
 */
public enum DiskLevel {

  UNKNOWN(-1),

  OK(0),

  /**
   * Reported, and nothing else. There is still room to work.
   */
  WARN(1),

  /**
   * Unbounded, user-initiated, restartable writes are refused: imports, copies, backups, uploads. Reads, login and
   * configuration writes keep working, which is what leaves an administrator a way in to make room.
   */
  DEGRADED(2),

  /**
   * Running writers are cancelled as well. A cancelled import can be run again; an MVStore that panicked on a full
   * disk closes itself without persisting, and only a restart brings it back.
   */
  CRITICAL(3);

  private final int severity;

  DiskLevel(int severity) {
    this.severity = severity;
  }

  public boolean isAtLeast(DiskLevel other) {
    return severity >= 0 && severity >= other.severity;
  }

  public boolean isWorseThan(DiskLevel other) {
    return severity > other.severity;
  }

  /**
   * The more severe of two levels. {@link #UNKNOWN} loses to every measured level, so a single volume that cannot be
   * read does not hide a volume that is genuinely full, nor does it invent a problem on its own.
   */
  public DiskLevel worst(DiskLevel other) {
    return other.severity > severity ? other : this;
  }
}
