/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.magma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.Initialisable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.Initialisables;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.unit.FunctionalUnit;

public class FunctionalUnitDatasourceFactory extends AbstractDatasourceFactory implements Initialisable {

  @Nonnull
  private final DatasourceFactory wrappedFactory;

  @Nonnull
  private final FunctionalUnit unit;

  @Nonnull
  private final ValueTable keysTable;

  @Nullable
  private final IParticipantIdentifier identifierGenerator;

  private final boolean ignoreUnknownIdentifier;

  public FunctionalUnitDatasourceFactory(@Nonnull DatasourceFactory wrappedFactory, @Nonnull FunctionalUnit unit,
      @Nonnull ValueTable keysTable, @Nullable IParticipantIdentifier identifierGenerator,
      boolean ignoreUnknownIdentifier) {
    this.wrappedFactory = wrappedFactory;
    this.unit = unit;
    this.keysTable = keysTable;
    this.identifierGenerator = identifierGenerator;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  public void setName(String name) {
    wrappedFactory.setName(name);
  }

  @Override
  public String getName() {
    return wrappedFactory.getName();
  }

  @Nonnull
  @Override
  protected Datasource internalCreate() {
    return new FunctionalUnitDatasource(wrappedFactory.create(), unit,
        FunctionalUnitView.Policy.UNIT_IDENTIFIERS_ARE_PRIVATE, keysTable, identifierGenerator,
        ignoreUnknownIdentifier);
  }

  @Override
  public void initialise() {
    Initialisables.initialise(wrappedFactory);
  }
}