/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import java.io.IOException;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.identifiers.IdentifiersMapping;

/**
 * Service for identifiers import-related operations.
 */
public interface IdentifiersImportService {

  /**
   * Import identifiers from a identifiers provider.
   * @param idMapping
   * @param pIdentifier
   * @return
   */
  int importIdentifiers(@NotNull IdentifiersMapping idMapping, @NotNull IdentifierGenerator pIdentifier);

  /**
   * Import identifiers from identifiers tables of a given data datasource.
   * @param idMapping
   * @param dataDatasource
   * @param select
   * @throws IOException
   */
  void importIdentifiers(@NotNull IdentifiersMapping idMapping, Datasource dataDatasource, @Nullable String select)
      throws IOException;

  /**
   * Import the identifiers of the given data table's entities (table values are ignored).
   *
   * @param dataValueTable
   * @throws java.io.IOException
   */
  void importIdentifiers(ValueTable dataValueTable) throws IOException;

  /**
   * Copy the identifiers entries from the given table of identifiers.
   * @param identifiersValueTable
   * @throws IOException
   */
  void copyIdentifiers(ValueTable identifiersValueTable) throws IOException;

}
