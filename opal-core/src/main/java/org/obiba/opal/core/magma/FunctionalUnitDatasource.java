/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.magma;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasourceWrapperWithCachedTables;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.unit.FunctionalUnit;
import org.springframework.util.Assert;

/**
 *
 */
public class FunctionalUnitDatasource extends AbstractDatasourceWrapperWithCachedTables {

  @Nonnull
  private final FunctionalUnit unit;

  @Nonnull
  private final FunctionalUnitView.Policy policy;

  @Nonnull
  private final ValueTable keysTable;

  @Nullable
  private final IParticipantIdentifier identifierGenerator;

  private final boolean ignoreUnknownIdentifier;

  public FunctionalUnitDatasource(@Nonnull Datasource wrapped, @Nonnull FunctionalUnit unit,
      @Nonnull FunctionalUnitView.Policy policy, @Nonnull ValueTable keysTable,
      @Nullable IParticipantIdentifier identifierGenerator, boolean ignoreUnknownIdentifier) {
    super(wrapped);

    Assert.notNull(wrapped, "wrapped datasource cannot be null");
    Assert.notNull(unit, "unit datasource cannot be null");
    Assert.notNull(policy, "policy datasource cannot be null");
    Assert.notNull(keysTable, "keysTable datasource cannot be null");

    this.unit = unit;
    this.policy = policy;
    this.keysTable = keysTable;
    this.identifierGenerator = identifierGenerator;
    this.ignoreUnknownIdentifier = ignoreUnknownIdentifier;
  }

  @Override
  protected ValueTable createValueTable(ValueTable table) {
    return new FunctionalUnitView(unit, policy, table, keysTable, identifierGenerator, ignoreUnknownIdentifier);
  }

}
