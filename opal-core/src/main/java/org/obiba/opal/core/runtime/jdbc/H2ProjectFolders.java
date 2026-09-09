/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.jdbc;

import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.database.CannotDeleteDatabaseFilesException;

import java.io.File;
import java.io.IOException;

/**
 * Removal of the folder holding the H2 database a project owns.
 * <p>
 * The folder is renamed before it is deleted, to {@code <project name>.deleted-<epochMillis>}, so that a removal
 * interrupted half way cannot leave behind something that still looks like a live project database. A folder marked
 * that way is unambiguous: nothing opens it, and the next start finishes the job. The rename is also what fails first
 * where the file system still holds the store open — and it fails before anything has been removed, so the caller is
 * told the data is still there rather than left with part of it.
 */
public final class H2ProjectFolders {

  /**
   * Marks a folder whose removal has begun. A project name holds no dot ({@code ProjectsResource} sees to that), so
   * the marker cannot be mistaken for part of one.
   */
  public static final String DELETED_MARKER = ".deleted-";

  private H2ProjectFolders() {
  }

  /**
   * Whether this folder is the remains of a deletion rather than a project's database.
   */
  public static boolean isPendingDeletion(File folder) {
    return folder.getName().indexOf(DELETED_MARKER) > 0;
  }

  /**
   * Delete a project's database folder, and everything H2 wrote in it beside the store. Silent when it does not
   * exist: the folder is only created at the first connection, so a project deleted right after it was created has a
   * database row and nothing on disk.
   *
   * @throws CannotDeleteDatabaseFilesException if the folder is still held, or if the removal fails
   */
  public static void delete(File folder) {
    if(!folder.exists()) return;

    File pending = new File(folder.getParentFile(), folder.getName() + DELETED_MARKER + System.currentTimeMillis());
    if(!folder.renameTo(pending)) {
      throw new CannotDeleteDatabaseFilesException(folder.getAbsolutePath(),
          "it could not be renamed to '" + pending.getName() + "', the database may still be open");
    }
    deletePending(pending);
  }

  /**
   * Delete a folder a previous removal renamed out of the way. Separate from {@link #delete(File)} so that the retry
   * of an interrupted deletion does not rename what has already been renamed.
   *
   * @throws CannotDeleteDatabaseFilesException if the removal fails
   */
  public static void deletePending(File folder) {
    try {
      FileUtil.delete(folder);
    } catch(IOException e) {
      throw new CannotDeleteDatabaseFilesException(folder.getAbsolutePath(), e);
    }
  }
}
