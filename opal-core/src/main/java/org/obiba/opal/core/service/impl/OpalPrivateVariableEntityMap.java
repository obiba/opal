/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.Initialisables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;

/**
 * An Opal implementation of {@link PrivateVariableEntityMap}, on top of a Magma Datasource ("key-datasource").
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {
  //
  // Instance Variables
  //

  private String owner;

  private IParticipantIdentifier participantIdentifier;

  //
  // PrivateVariableEntityMap Methods
  //

  public VariableEntity publicEntity(VariableEntity privateEntity) {
    ValueTable keyTable = getKeyValueTable();

    // TODO: Shouldn't have to re-initialize the table over and over again. This is
    // necessary at the moment because certain changes to the table (adding a ValueSet)
    // are not being sync'ed.
    Initialisables.initialise(keyTable);

    Variable ownerVariable = keyTable.getVariable(owner);
    for(ValueSet valueSet : keyTable.getValueSets()) {
      Value ownerVariableValue = keyTable.getValue(ownerVariable, valueSet);
      if(ownerVariableValue.toString().equals(privateEntity.getIdentifier())) {
        return valueSet.getVariableEntity();
      }
    }

    return null;
  }

  public VariableEntity privateEntity(VariableEntity publicEntity) {
    ValueTable keyTable = getKeyValueTable();

    // TODO: Shouldn't have to re-initialize the table over and over again. This is
    // necessary at the moment because certain changes to the table (adding a ValueSet)
    // are not being sync'ed.
    Initialisables.initialise(keyTable);

    Variable ownerVariable = keyTable.getVariable(owner);
    ValueSet publicValueSet = keyTable.getValueSet(publicEntity);
    Value ownerValue = keyTable.getValue(ownerVariable, publicValueSet);

    return new VariableEntityBean(publicValueSet.getVariableEntity().getType(), ownerValue.toString());
  }

  public boolean hasPrivateEntity(VariableEntity privateEntity) {
    ValueTable keyTable = getKeyValueTable();

    for(Variable variable : keyTable.getVariables()) {
      if(variable.getName().equals(owner)) {
        return true;
      }
    }

    return false;
  }

  public VariableEntity createPublicEntity(VariableEntity privateEntity) {
    ValueTable keyTable = getKeyValueTable();

    for(int i = 0; i < 100; i++) {
      VariableEntity publicEntity = new VariableEntityBean(privateEntity.getType(), participantIdentifier.generateParticipantIdentifier());
      if(!keyTable.hasValueSet(publicEntity)) {
        return publicEntity;
      }
    }

    throw new IllegalStateException("Unable to generate a unique public entity for the owner [" + owner + "] and private entity [" + privateEntity.getIdentifier() + "]. " + "One hundred attempts made.");
  }

  //
  // Methods
  //

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setParticipantIdentifier(IParticipantIdentifier participantIdentifier) {
    this.participantIdentifier = participantIdentifier;
  }

  private ValueTable getKeyValueTable() {
    Datasource keyDatasource = MagmaEngine.get().getDatasource("key-datasource");
    return keyDatasource.getValueTable("keys");
  }
}
