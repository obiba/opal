/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.vfs2.FileObject;
import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.unit.FunctionalUnit;

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
  int importIdentifiers(@NotNull IdentifiersMapping idMapping, @NotNull IParticipantIdentifier pIdentifier);

  /**
   * Import identifiers from identifiers tables of a given datasource.
   * @param idMapping
   * @param sourceDatasource
   * @param select
   * @throws IOException
   */
  void importIdentifiers(@NotNull IdentifiersMapping idMapping, Datasource sourceDatasource, @Nullable String select)
      throws IOException;

  /**
   * Import the identifiers of the given table's entities (table values are ignored).
   *
   * @param sourceValueTable
   * @throws java.io.IOException
   */
  void importIdentifiers(ValueTable sourceValueTable) throws IOException;

}
