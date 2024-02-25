/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.identifiers;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.web.support.InvalidRequestException;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.File;

public abstract class AbstractIdentifiersResource {

  protected abstract IdentifiersTableService getIdentifiersTableService();

  protected abstract OpalFileSystemService getOpalFileSystemService();

  protected File resolveLocalFile(String path) {
    try {
      // note: does not ensure that file exists
      return getOpalFileSystemService().getFileSystem().getLocalFile(resolveFileInFileSystem(path));
    } catch (FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return getOpalFileSystemService().getFileSystem().getRoot().resolveFile(path);
  }

  /**
   * Get the identifiers value table of the given entity type, case insensitive.
   *
   * @param entityType
   * @return
   */
  @Nullable
  protected ValueTable getValueTable(@NotNull String entityType) {
    for (ValueTable table : getDatasource().getValueTables()) {
      if (table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return table;
      }
    }
    return null;
  }

  protected void ensureEntityType(String entityType) {
    if (entityType == null || !getIdentifiersTableService().hasIdentifiersTable(entityType)) {
      throw new InvalidRequestException("No such identifiers table for entity type: " + entityType);
    }
  }

  protected Datasource getDatasource() {
    if (!getIdentifiersTableService().hasDatasource())
      throw new NoSuchDatasourceException(getIdentifiersTableService().getDatasourceName());
    return getIdentifiersTableService().getDatasource();
  }

}
