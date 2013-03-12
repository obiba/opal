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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Reads and caches rows in the identifiers table
 */
public class IdentifierAssociations implements Iterable<IdentifierAssociations.IdentifierAssociation> {

  public class IdentifierAssociation {

    private final String opalIdentifier;

    private final Map<String, Value> unitIdentifiers = Maps.newHashMap();

    private boolean dirty = false;

    IdentifierAssociation(String opalIdentifier, Map<String, Iterator<Value>> unitIdentifiers) {
      this.opalIdentifier = opalIdentifier;
      for(Map.Entry<String, Iterator<Value>> e : unitIdentifiers.entrySet()) {
        String unitName = e.getKey();
        Value unitIdentifier = e.getValue().next();

        this.unitIdentifiers.put(unitName, unitIdentifier);
        index.get(unitName).put(unitIdentifier, opalIdentifier);
      }
    }

    public String getOpalIdentifier() {
      return opalIdentifier;
    }

    public VariableEntity getOpalEntity() {
      return entityFor(getOpalIdentifier());
    }

    public String getUnitIdentifier(String unitName) {
      if(unitIdentifiers.containsKey(unitName) == false) {
        return null;
      }
      return (String) unitIdentifiers.get(unitName).getValue();
    }

    public VariableEntity getUnitEntity(String unitName) {
      return entityFor(getUnitIdentifier(unitName));
    }

    public boolean hasUnitIdentifier(String unitName) {
      return unitIdentifiers.containsKey(unitName) && unitIdentifiers.get(unitName).isNull() == false;
    }

    void write(ValueTableWriter vtw) throws IOException {
      if(dirty) {
        ValueSetWriter vsw = vtw.writeValueSet(getOpalEntity());
        for(FunctionalUnit unit : units) {
          if(unit.isOpal() == false) {
            vsw.writeValue(unitVariable(unit), unitIdentifiers.get(unit.getName()));
          }
        }
        vsw.close();
      }
    }

    boolean set(String unitName, String identifier) {
      Value currentIdentifier = this.unitIdentifiers.get(unitName);
      if(currentIdentifier.isNull() == false && currentIdentifier.getValue().equals(identifier) == false) {
        throw new IllegalIdentifierAssociationException(
            "Cannot override current unit identifier: " + unitName + ":" + currentIdentifier + " <--> opal:" +
                opalIdentifier);
      }
      // Check duplicates
      if(index.get(unitName).unitIdentifierIsNewOrFor(identifier, opalIdentifier) == false) {
        throw new IllegalIdentifierAssociationException(
            "Cannot create duplicate unit identifier: " + unitName + ":" + identifier + " <--> opal:" + opalIdentifier);
      }

      if(currentIdentifier.isNull()) {
        // Index
        index.get(unitName).put(identifier, opalIdentifier);
        this.unitIdentifiers.put(unitName, TextType.get().valueOf(identifier));
        dirty = true;
        return true;
      }
      return false;
    }

    private Variable unitVariable(FunctionalUnit unit) {
      return identifiersTable.getVariable(unit.getKeyVariableName());
    }
  }

  private static class UnitIdentifiers {

    private Map<String, String> unitIdentifierToOpalIdentifier = Maps.newHashMap();

    /**
     * @param opalIdentifier
     * @return
     */
    public boolean unitIdentifierIsNewOrFor(String unitIdentifier, String opalIdentifier) {
      return unitIdentifierToOpalIdentifier.containsKey(unitIdentifier) == false ||
          unitIdentifierToOpalIdentifier.get(unitIdentifier).equals(opalIdentifier);
    }

    /**
     * @param unitIdentifier
     * @param opalIdentifier
     */
    public void put(Value unitIdentifier, String opalIdentifier) {
      if(unitIdentifier.isNull() == false) {
        put((String) unitIdentifier.getValue(), opalIdentifier);
      }
    }

    public void put(String unitIdentifier, String opalIdentifier) {
      if(unitIdentifier == null) throw new IllegalArgumentException("unitIdentifier cannot be null");
      if(opalIdentifier == null) throw new IllegalArgumentException("opalIdentifier cannot be null");
      unitIdentifierToOpalIdentifier.put(unitIdentifier, opalIdentifier);
    }
  }

  private final ValueTable identifiersTable;

  private final FunctionalUnit[] units;

  private final Map<String, UnitIdentifiers> index = Maps.newHashMap();

  public IdentifierAssociations(ValueTable identifiers, FunctionalUnit... units) {
    this.identifiersTable = identifiers;
    this.units = units;
    for(FunctionalUnit unit : units) {
      index.put(unit.getName(), new UnitIdentifiers());
    }
  }

  public IdentifierAssociations(ValueTable identifiers, Collection<FunctionalUnit> units) {
    this(identifiers, units.toArray(new FunctionalUnit[units.size()]));
  }

  @Override
  public Iterator<IdentifierAssociation> iterator() {
    return new IdentifierAssociationIterator();
  }

  private VariableEntity entityFor(String identifier) {
    return new VariableEntityBean(identifiersTable.getEntityType(), identifier);
  }

  private final class IdentifierAssociationIterator implements Iterator<IdentifierAssociation> {

    private final Iterator<VariableEntity> opalEntities;

    private final Map<String, Iterator<Value>> unitIdentifiers = Maps.newHashMap();

    {
      TreeSet<VariableEntity> opalEntities = new TreeSet<VariableEntity>(identifiersTable.getVariableEntities());

      Iterator<Value> idIterator;
      for(FunctionalUnit unit : units) {
        if(identifiersTable.hasVariable(unit.getKeyVariableName())) {
          idIterator = identifiersTable.getVariableValueSource(unit.getKeyVariableName()).asVectorSource()
              .getValues(opalEntities).iterator();
        } else if(unit.isOpal()) {
          idIterator = Iterables.transform(opalEntities, new Function<VariableEntity, Value>() {

            @Override
            public Value apply(VariableEntity from) {
              return TextType.get().valueOf(from.getIdentifier());
            }
          }).iterator();
        } else {
          // Make sure not to loop on iterators returned by this method call, or an infinite loop will happen
          idIterator = Iterables.cycle(TextType.get().nullValue()).iterator();
        }
        unitIdentifiers.put(unit.getName(), idIterator);
      }

      this.opalEntities = opalEntities.iterator();
    }

    @Override
    public boolean hasNext() {
      return opalEntities.hasNext();
    }

    @Override
    public IdentifierAssociation next() {
      return new IdentifierAssociation(opalEntities.next().getIdentifier(), unitIdentifiers);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  }

  ;

}
