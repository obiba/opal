/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class AbstractBackupRestoreCommand<T> extends AbstractOpalRuntimeDependentCommand<T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractBackupRestoreCommand.class);

  /**
   * Get archive folder from options.
   *
   * @return
   */
  protected abstract File getArchiveFolder();

  /**
   * Get the project name from options.
   *
   * @return
   */
  protected abstract String getProjectName();

  /**
   * Depends whether it is a backup or restore command.
   *
   * @return
   */
  protected abstract boolean isReadOnly();

  protected File getProjectFolder() {
    return new File(getLocalFile(getFileSystemRoot()), "projects" + File.separator + getProjectName());
  }

  protected File getTablesFolder() {
    return getFolder("tables");
  }

  protected File getTableFolder(String tableName) {
    File folder = new File(getTablesFolder(), tableName);
    if (!folder.exists() && !isReadOnly())
      if (!folder.mkdirs())
        log.warn("Cannot create folder {}", folder.getAbsolutePath());
    return folder;
  }

  protected File getViewsFolder() {
    return getFolder("tables");
  }

  protected File getResourcesFolder() {
    return getFolder("resources");
  }

  protected File getFilesFolder() {
    return getFolder("files");
  }

  protected File getReportsFolder() {
    return getFolder("reports");
  }

  /**
   * Get or create a folder with provided name.
   *
   * @param name
   * @return
   */
  private File getFolder(String name) {
    File folder = new File(getArchiveFolder(), name);
    if (!folder.exists() && !isReadOnly())
      if (!folder.mkdirs())
        log.warn("Cannot create folder {}", folder.getAbsolutePath());
    return folder;
  }
}
