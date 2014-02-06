/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.identifiers;

import java.io.File;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.identifiers.IdentifiersMaps;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.web.support.InvalidRequestException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import au.com.bytecode.opencsv.CSVWriter;

public abstract class AbstractIdentifiersResource {

  protected abstract IdentifiersTableService getIdentifiersTableService();

  protected abstract OpalRuntime getOpalRuntime();

  protected File resolveLocalFile(String path) {
    try {
      // note: does not ensure that file exists
      return getOpalRuntime().getFileSystem().getLocalFile(resolveFileInFileSystem(path));
    } catch(FileSystemException e) {
      throw new IllegalArgumentException(e);
    }
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return getOpalRuntime().getFileSystem().getRoot().resolveFile(path);
  }

  /**
   * Get the identifiers value table of the given entity type, case insensitive.
   *
   * @param entityType
   * @return
   */
  @Nullable
  protected ValueTable getValueTable(@NotNull String entityType) {
    for(ValueTable table : getDatasource().getValueTables()) {
      if(table.getEntityType().toLowerCase().equals(entityType.toLowerCase())) {
        return table;
      }
    }
    return null;
  }

  protected void ensureEntityType(String entityType) {
    if(entityType == null || !getIdentifiersTableService().hasIdentifiersTable(entityType))
      throw new InvalidRequestException("No such identifiers table for entity type: " + entityType);
  }

  protected Datasource getDatasource() {
    return getIdentifiersTableService().getDatasource();
  }

  protected Iterable<IdentifiersMaps.IdentifiersMap> getUnitIdentifiers(String entityType, String name) {
    return Iterables
        .filter(new IdentifiersMaps(getValueTable(entityType), name), new Predicate<IdentifiersMaps.IdentifiersMap>() {
          @Override
          public boolean apply(@Nullable IdentifiersMaps.IdentifiersMap input) {
            return input.hasPrivateIdentifier();
          }
        });
  }

  protected void writeCSVValues(CSVWriter writer, ValueTable table, Variable variable) {
    // header
    writer.writeNext(new String[] { table.getEntityType(), variable.getName() });
    for(IdentifiersMaps.IdentifiersMap unitId : getUnitIdentifiers(table.getEntityType(), variable.getName())) {
      writer.writeNext(new String[] { unitId.getSystemIdentifier(), unitId.getPrivateIdentifier() });
    }
  }

}
