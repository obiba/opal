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

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.opal.core.identifiers.IdentifiersMapping;

/**
 *
 */
public interface IdentifiersTableService extends SystemService {

  /**
   * Get the identifiers datasource.
   *
   * @return
   */
  @Nullable
  Datasource getDatasource();

  /**
   * Identifiers database may not be defined.
   *
   * @return
   */
  boolean hasDatasource();

  /**
   * Check if any identifiers table exists for the given entity type.
   *
   * @param entityType
   * @return
   */
  boolean hasIdentifiersTable(@NotNull String entityType);

  /**
   * Get the identifiers table for the given entity type.
   *
   * @param entityType
   * @return
   * @throws NoSuchValueTableException
   */
  @NotNull
  ValueTable getIdentifiersTable(@NotNull String entityType) throws NoSuchValueTableException;

  /**
   * Create value table for entity type if not found.
   *
   * @param entityType
   */
  @NotNull
  ValueTable ensureIdentifiersTable(@NotNull String entityType);

  /**
   * Create value table for entity type and the variable with identifiers mapping name if not found.
   *
   * @param idMapping
   * @return
   */
  @NotNull
  Variable ensureIdentifiersMapping(@NotNull IdentifiersMapping idMapping);

  /**
   * Get a writer on the identifiers table.
   *
   * @param entityType
   * @return
   */
  ValueTableWriter createIdentifiersTableWriter(@NotNull String entityType);

  /**
   * Check if there is at least one identifiers table that has a variable with the given name.
   *
   * @param idMapping
   * @return
   */
  boolean hasIdentifiersMapping(@NotNull String idMapping);

  /**
   * Check if there is an identifiers table for the entity type that has a variable with the given name.
   *
   * @param entityType
   * @param idMapping
   * @return
   */
  boolean hasIdentifiersMapping(@NotNull String entityType, @NotNull String idMapping);

  /**
   * Get the javascript select script from the variable matching entity type and identifiers mapping.
   *
   * @param entityType
   * @param idMapping
   * @return
   */
  @Nullable
  String getSelectScript(@NotNull String entityType, @NotNull String idMapping);

  /**
   * Get the table reference as specified by property <code>org.obiba.opal.keys.tableReference</code>.
   *
   * @return
   */
  @NotNull
  String getTableReference(@NotNull String entityType);

  /**
   * Get the identifiers datasource name.
   *
   * @return
   */
  String getDatasourceName();

  /**
   * Check if there are any identifiers tables with entities.
   *
   * @return
   */
  boolean hasEntities();

}
