/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.database;

/**
 * The files of a database Opal owns could not be removed. Thrown before anything has been deleted when the folder
 * cannot be renamed out of the way, so that a caller is told the data is still there rather than left guessing.
 */
public class CannotDeleteDatabaseFilesException extends RuntimeException {

  private static final long serialVersionUID = 7621459022946139744L;

  private final String folder;

  public CannotDeleteDatabaseFilesException(String folder, String reason) {
    super("Cannot delete the database files in '" + folder + "': " + reason);
    this.folder = folder;
  }

  public CannotDeleteDatabaseFilesException(String folder, Throwable cause) {
    super("Cannot delete the database files in '" + folder + "': " + cause.getMessage(), cause);
    this.folder = folder;
  }

  public String getFolder() {
    return folder;
  }
}
