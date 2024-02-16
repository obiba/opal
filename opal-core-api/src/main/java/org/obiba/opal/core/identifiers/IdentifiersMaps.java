/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.identifiers;

import java.util.Iterator;
import java.util.List;

import jakarta.annotation.Nullable;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

/**
 * An IdentifiersMap holds the mapping between a system identifier to a private identifier for a given entity type.
 * Both entity type and system/private identifiers are extracted from a given identifiers table
 * and an identifiers mapping name.
 */
public class IdentifiersMaps implements Iterable<IdentifiersMaps.IdentifiersMap> {

  public class IdentifiersMap {

    /**
     * Opal's identifier for the variable entity (the public identifier).
     */
    private final String systemIdentifier;

    /**
     * Private identifier for the variable entity (can be null).
     */
    @Nullable
    private final String privateIdentifier;

    IdentifiersMap(String systemIdentifier, @Nullable String privateIdentifier) {
      this.systemIdentifier = systemIdentifier;
      this.privateIdentifier = privateIdentifier;
    }

    public String getSystemIdentifier() {
      return systemIdentifier;
    }

    public VariableEntity getSystemEntity() {
      return entityFor(getSystemIdentifier());
    }

    @Nullable
    public String getPrivateIdentifier() {
      return privateIdentifier;
    }

    public VariableEntity getPrivateEntity() {
      return entityFor(getPrivateIdentifier());
    }

    private VariableEntity entityFor(@Nullable String identifier) {
      return new VariableEntityBean(identifiersTable.getEntityType(), identifier);
    }

    public boolean hasPrivateIdentifier() {
      return privateIdentifier != null && !privateIdentifier.isEmpty();
    }

  }

  private final ValueTable identifiersTable;

  private final String idMapping;

  /**
   * For each entities in the identifiers table, extract the private identifiers corresponding to the named
   * identifiers mapping.
   *
   * @param identifiersTable
   * @param idMapping variable name which values are the private identifiers
   */
  public IdentifiersMaps(ValueTable identifiersTable, String idMapping) {
    this.identifiersTable = identifiersTable;
    this.idMapping = idMapping;
  }

  /**
   * Get all private entities: entities with non-null private identifiers.
   *
   * @return
   */
  public Iterable<VariableEntity> getPrivateEntities() {
    return Iterables.filter(Iterables.transform(this, new Function<IdentifiersMap, VariableEntity>() {

      @Override
      public VariableEntity apply(IdentifiersMap from) {
        return from.hasPrivateIdentifier() ? from.getPrivateEntity() : null;
      }

    }), Predicates.notNull());
  }

  @Override
  public Iterator<IdentifiersMap> iterator() {
    return new Iterator<IdentifiersMap>() {

      private final Iterator<VariableEntity> systemEntities;

      private final Iterator<Value> privateIdentifiers;

      {
        List<VariableEntity> entities = identifiersTable.getVariableEntities();

        privateIdentifiers = identifiersTable.hasVariable(idMapping)
            ? identifiersTable.getVariableValueSource(idMapping).asVectorSource().getValues(entities).iterator()
            : Iterables.cycle(TextType.get().nullValue()).iterator();
        systemEntities = entities.iterator();
      }

      @Override
      public boolean hasNext() {
        return systemEntities.hasNext();
      }

      @Override
      public IdentifiersMap next() {
        Value value = privateIdentifiers.next();
        return new IdentifiersMap(systemEntities.next().getIdentifier(),
            value.isNull() ? null : (String) value.getValue());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

}
