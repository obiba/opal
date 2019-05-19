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

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Initialisable;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.service.IdentifiersTableService;

public class IdentifiersMappingDatasourceFactory extends AbstractDatasourceFactory implements Initialisable {

  @NotNull
  private final DatasourceFactory wrappedFactory;

  @NotNull
  private final String idMapping;

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  private final IdentifierGenerator identifierGenerator;

  private final boolean ignoreUnknownIdentifier;

  public IdentifiersMappingDatasourceFactory(@NotNull DatasourceFactory wrappedFactory, @NotNull String idMapping,
      @NotNull IdentifiersTableService identifiersTableService, @Nullable IdentifierGenerator identifierGenerator,
      boolean ignoreUnknownIdentifier) {
    this.wrappedFactory = wrappedFactory;
    this.idMapping = idMapping;
    this.identifiersTableService = identifiersTableService;
    this.identifierGenerator = identifierGenerator;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  public void setName(@NotNull String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @NotNull
  @Override
  protected Datasource internalCreate() {
    return new IdentifiersMappingDatasource(wrappedFactory.create(), idMapping,
        IdentifiersMappingView.Policy.UNIT_IDENTIFIERS_ARE_PRIVATE, identifiersTableService, identifierGenerator,
        ignoreUnknownIdentifier);
  }

  @Override
  public void initialise() {
    Initialisables.initialise(wrappedFactory);
  }
}