/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasourceWrapperWithCachedTables;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.service.IdentifiersTableService;

/**
 *
 */
public class IdentifiersMappingDatasource extends AbstractDatasourceWrapperWithCachedTables {

  @NotNull
  private final String idMapping;

  @NotNull
  private final IdentifiersMappingView.Policy policy;

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  private final IdentifierGenerator identifierGenerator;

  private final boolean ignoreUnknownIdentifier;

  public IdentifiersMappingDatasource(@NotNull Datasource wrapped, @NotNull String idMapping,
      @NotNull IdentifiersMappingView.Policy policy, @NotNull IdentifiersTableService identifiersTableService,
      @Nullable IdentifierGenerator identifierGenerator, boolean ignoreUnknownIdentifier) {
    super(wrapped);

    this.idMapping = idMapping;
    this.policy = policy;
    this.identifiersTableService = identifiersTableService;
    this.identifierGenerator = identifierGenerator;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  protected ValueTable createValueTable(ValueTable table) {
    // verify there is a identifiers mapping for the table's entity type
    if (identifiersTableService.hasIdentifiersTable(table.getEntityType())) {
      return new IdentifiersMappingView(idMapping, policy, table, identifiersTableService, identifierGenerator, ignoreUnknownIdentifier);
    }
    return table;
  }

}
