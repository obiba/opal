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
import java.util.List;
import java.util.Map;

import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.lang.Closeables;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.unit.IdentifierAssociations.IdentifierAssociation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class FunctionalUnitIdentifierMapper {

  private final ValueTable identifiersTable;

  private final FunctionalUnit drivingUnit;

  private final List<FunctionalUnit> units;

  private final Map<String, IdentifierAssociation> associations;

  public FunctionalUnitIdentifierMapper(ValueTable identifiersTable, FunctionalUnit drivingUnit, List<FunctionalUnit> units) {
    this.identifiersTable = identifiersTable;
    this.drivingUnit = drivingUnit;
    this.units = units;
    prepareKeysTable();

    final String drivingUnitName = this.drivingUnit.getName();

    // Filters associations that don't have a identifier for the driving unit
    Predicate<IdentifierAssociation> associationsOfDrivingUnit = new Predicate<IdentifierAssociation>() {

      @Override
      public boolean apply(IdentifierAssociation input) {
        return input.hasUnitIdentifier(drivingUnitName);
      }

    };

    // Returns the identifier of the driving unit. Used to build an index.
    Function<IdentifierAssociation, String> identifierOfDrivingUnit = new Function<IdentifierAssociation, String>() {

      @Override
      public String apply(IdentifierAssociation from) {
        return from.getUnitIdentifier(drivingUnitName);
      }

    };

    // Builds an index with driving unit identifiers as keys and IdentifierAssociation as values. The index only
    // contains associations that have an identifier for the driving unit.
    this.associations = Maps.uniqueIndex(Iterables.filter(new IdentifierAssociations(identifiersTable, units), associationsOfDrivingUnit), identifierOfDrivingUnit);
  }

  public int associate(String drivingUnitIdentifier, List<FunctionalUnit> units, String[] identifiers) {
    if(drivingUnitIdentifier == null || drivingUnitIdentifier.isEmpty()) throw new IllegalArgumentException("driving unit identifier cannot be empty");
    int newAssociations = 0;

    IdentifierAssociation association = getAssociationForDrivingUnitIdentifier(drivingUnitIdentifier);
    for(int i = 0; i < units.size(); i++) {
      FunctionalUnit unit = units.get(i);
      if(unit.getName().equals(this.drivingUnit.getName())) continue;

      String identifier = identifiers[i];
      if(this.units.contains(unit) == false) {
        throw new IllegalStateException("unit '" + unit.getName() + "' was not prepared.");
      }
      // Do not associate to empty identifiers
      if(identifier != null && identifier.isEmpty() == false) {
        if(association.set(unit.getName(), identifier)) {
          newAssociations++;
        }
      }
    }
    return newAssociations;
  }

  public void write() throws IOException {
    ValueTableWriter vtw = writeToKeysTable();
    try {
      for(IdentifierAssociation map : associations.values()) {
        map.write(vtw);
      }
    } finally {
      vtw.close();
    }
  }

  private IdentifierAssociation getAssociationForDrivingUnitIdentifier(final String identifier) {
    if(associations.containsKey(identifier) == false) {
      throw new IllegalIdentifierAssociationException("Unit " + drivingUnit.getName() + " does not have an entity identified with " + identifier);
    }
    return associations.get(identifier);
  }

  private void prepareKeysTable() {
    ValueTableWriter writer = writeToKeysTable();
    VariableWriter vw = writer.writeVariables();
    try {
      for(FunctionalUnit unit : units) {
        if(!unit.isOpal() && identifiersTable.hasVariable(unit.getKeyVariableName()) == false) {
          Variable keyVariable = Variable.Builder.newVariable(unit.getKeyVariableName(), TextType.get(), identifiersTable.getEntityType()).build();
          vw.writeVariable(keyVariable);
        }
      }
    } finally {
      Closeables.closeQuietly(writer);
      Closeables.closeQuietly(vw);
    }
  }

  private ValueTableWriter writeToKeysTable() {
    return identifiersTable.getDatasource().createWriter(identifiersTable.getName(), identifiersTable.getEntityType());
  }

}
