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

import jakarta.annotation.Nullable;
import org.obiba.opal.core.service.SystemService;

import java.io.File;
import java.util.List;

/**
 * Watches the free space on the volumes Opal writes to, and refuses the large writes before the small ones start
 * failing.
 * <p>
 * The check has to be made before the write, not around it. When an MVStore hits a full disk it calls
 * {@code panic()} and closes itself without persisting: there is no failed statement to retry and no recovery short of
 * a restart. Keeping a floor of free space is also what lets the store close cleanly, since compaction and the clean
 * shutdown mark need somewhere to go.
 * <p>
 * The asymmetry between the levels is the design: at {@link DiskLevel#DEGRADED} the writes that are refused are the
 * unbounded, user-initiated, restartable ones — imports, copies, backups, uploads — while reads, login and
 * configuration writes keep working. That is what leaves an administrator a way in to make room.
 */
public interface DiskSpaceService extends SystemService {

  /**
   * The most severe level across the watched volumes, from the last sample.
   */
  DiskLevel getLevel();

  /**
   * The last reading of every watched volume, one entry per volume.
   */
  List<DiskStatus> getStatuses();

  /**
   * The last reading of the volume holding the given path, or {@code null} if that volume is not watched.
   */
  @Nullable
  DiskStatus getStatus(File path);

  /**
   * Whether the levels are acted upon. When false the readings are still taken and reported, and nothing is refused:
   * the thresholds can then be observed against a real deployment before they start failing jobs.
   */
  boolean isEnforced();

  /**
   * Refuse an unbounded write of unknown size, on the strength of the worst watched volume.
   *
   * @throws InsufficientStorageException if the level is {@link DiskLevel#DEGRADED} or worse, and enforcement is on
   */
  void checkWritable() throws InsufficientStorageException;

  /**
   * Refuse a write whose size is known in advance, against the volume it would actually land on. A single large upload
   * is invisible to a sampler that runs once a minute, so a size that is known up front is checked up front.
   *
   * @param path          where the bytes would be written; the volume holding it is what gets checked
   * @param requiredBytes how many bytes the write needs, before any safety margin
   * @throws InsufficientStorageException if the write would not leave the reserved floor, and enforcement is on
   */
  void checkWritable(File path, long requiredBytes) throws InsufficientStorageException;

  /**
   * Refuse a write of known size to the Opal file system, which is where uploads, exports and reports go.
   *
   * @see #checkWritable(File, long)
   */
  void checkFileSystemWritable(long requiredBytes) throws InsufficientStorageException;
}
