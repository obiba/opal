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

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBackupRestoreCommand<T> extends AbstractOpalRuntimeDependentCommand<T> {

  private static final Logger log = LoggerFactory.getLogger(AbstractBackupRestoreCommand.class);

  protected static final String PERMISSIONS_DOMAIN = "opal";

  private static final Pattern DATASOURCE_ITEM_NODE = Pattern.compile("^/datasource/[^/]+/(table|view)/([^/]+)(?:/variable/(.+))?$");

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

  protected File getPermissionsFile() {
    return new File(getArchiveFolder(), "permissions.json");
  }

  /**
   * Whether the table, view or variable that a datasource permission node designates is in the project. Looked up by
   * name in the project's datasource, as table and variable names may contain characters that a Magma reference would
   * misread. Other datasource nodes are considered to be in the project.
   */
  protected boolean isDatasourceItem(String node) {
    Matcher matcher = DATASOURCE_ITEM_NODE.matcher(node);
    if (!matcher.matches()) return true;
    try {
      Datasource datasource = MagmaEngine.get().getDatasource(getProjectName());
      if (!datasource.hasValueTable(matcher.group(2))) return false;
      ValueTable table = datasource.getValueTable(matcher.group(2));
      if (table.isView() != "view".equals(matcher.group(1))) return false;
      return matcher.group(3) == null || table.hasVariable(matcher.group(3));
    } catch (Exception e) {
      // a broken view for instance
      log.warn("Cannot access the item of the permission {}: {}", node, e.getMessage());
      return false;
    }
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
