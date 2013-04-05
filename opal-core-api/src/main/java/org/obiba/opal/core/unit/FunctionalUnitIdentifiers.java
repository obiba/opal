/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.unit;

import java.util.Iterator;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 *
 */
public class FunctionalUnitIdentifiers implements Iterable<FunctionalUnitIdentifiers.UnitIdentifier> {

  public class UnitIdentifier {

    /**
     * Opal's identifier for the variable entity.
     */
    private final String opalIdentifier;

    /**
     * Unit's identifier for the variable entity (can be null).
     */
    @Nullable
    private final String unitIdentifier;

    UnitIdentifier(String opalIdentifier, @Nullable String unitIdentifier) {
      this.opalIdentifier = opalIdentifier;
      this.unitIdentifier = unitIdentifier;
    }

    public String getOpalIdentifier() {
      return opalIdentifier;
    }

    public VariableEntity getOpalEntity() {
      return entityFor(getOpalIdentifier());
    }

    @Nullable
    public String getUnitIdentifier() {
      return unitIdentifier;
    }

    public VariableEntity getUnitEntity() {
      return entityFor(getUnitIdentifier());
    }

    private VariableEntity entityFor(@Nullable String identifier) {
      return new VariableEntityBean(identifiersTable.getEntityType(), identifier);
    }

    public boolean hasUnitIdentifier() {
      return unitIdentifier != null && unitIdentifier.length() > 0;
    }

  }

  private final ValueTable identifiersTable;

  private final FunctionalUnit unit;

  public FunctionalUnitIdentifiers(ValueTable identifiers, FunctionalUnit unit) {
    identifiersTable = identifiers;
    this.unit = unit;
  }

  public Iterable<VariableEntity> getUnitEntities() {
    return Iterables.filter(Iterables.transform(this, new Function<UnitIdentifier, VariableEntity>() {

      @Override
      public VariableEntity apply(UnitIdentifier from) {
        return from.hasUnitIdentifier() ? from.getUnitEntity() : null;
      }

    }), Predicates.notNull());
  }

  @Override
  public Iterator<UnitIdentifier> iterator() {
    return new Iterator<UnitIdentifier>() {

      private final Iterator<VariableEntity> opalEntities;

      private final Iterator<Value> unitIdentifiers;

      {
        TreeSet<VariableEntity> entities = new TreeSet<VariableEntity>(identifiersTable.getVariableEntities());
        unitIdentifiers = identifiersTable.hasVariable(unit.getKeyVariableName())
            ? identifiersTable.getVariableValueSource(unit.getKeyVariableName()).asVectorSource().getValues(entities)
            .iterator()
            : Iterables.cycle(TextType.get().nullValue()).iterator();
        opalEntities = entities.iterator();
      }

      @Override
      public boolean hasNext() {
        return opalEntities.hasNext();
      }

      @Override
      public UnitIdentifier next() {
        return new UnitIdentifier(opalEntities.next().getIdentifier(), (String) unitIdentifiers.next().getValue());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

}
