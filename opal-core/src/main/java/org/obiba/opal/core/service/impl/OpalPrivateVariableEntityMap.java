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

import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;

/**
 * An Opal implementation of {@code PrivateVariableEntityMap}, on top of a Magma {@code ValueTable}.
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {
  //
  // Instance Variables
  //

  private final ValueTable keysValueTable;

  private final Variable ownerVariable;

  private final IParticipantIdentifier participantIdentifier;

  /**
   * 
   */
  public OpalPrivateVariableEntityMap(ValueTable keysValueTable, Variable ownerVariable, IParticipantIdentifier participantIdentifier) {
    if(keysValueTable == null) throw new IllegalArgumentException("keysValueTable cannot be null");
    if(ownerVariable == null) throw new IllegalArgumentException("ownerVariable cannot be null");
    if(participantIdentifier == null) throw new IllegalArgumentException("participantIdentifier cannot be null");

    this.keysValueTable = keysValueTable;
    this.ownerVariable = ownerVariable;
    this.participantIdentifier = participantIdentifier;
  }

  //
  // PrivateVariableEntityMap Methods
  //

  public VariableEntity publicEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");

    ValueTable keyTable = getKeyValueTable();
    VariableValueSource ownerVariableSource = keyTable.getVariableValueSource(ownerVariable.getName());
    for(ValueSet valueSet : keyTable.getValueSets()) {
      Value ownerVariableValue = ownerVariableSource.getValue(valueSet);
      if(ownerVariableValue.toString().equals(privateEntity.getIdentifier())) {
        return valueSet.getVariableEntity();
      }
    }

    return null;
  }

  public VariableEntity privateEntity(VariableEntity publicEntity) {
    if(publicEntity == null) throw new IllegalArgumentException("publicEntity cannot be null");

    ValueTable keyTable = getKeyValueTable();
    ValueSet valueSet = keyTable.getValueSet(publicEntity);
    VariableValueSource ownerVariableSource = keyTable.getVariableValueSource(ownerVariable.getName());
    Value ownerKey = ownerVariableSource.getValue(valueSet);
    return new VariableEntityBean(publicEntity.getType(), ownerKey.toString());
  }

  public boolean hasPrivateEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");

    ValueTable keyTable = getKeyValueTable();
    VariableValueSource ownerVariableSource = keyTable.getVariableValueSource(ownerVariable.getName());
    for(ValueSet keysValueSet : keyTable.getValueSets()) {
      Value privateIdentifier = ownerVariableSource.getValue(keysValueSet);
      if(!privateIdentifier.isNull()) { // ValueSets from different owners will not have a value for this owner variable
        if(privateIdentifier.toString().equals(privateEntity.getIdentifier())) {
          return true;
        }
      }
    }
    return false;
  }

  public VariableEntity createPublicEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");

    ValueTable keyTable = getKeyValueTable();

    for(int i = 0; i < 100; i++) {
      VariableEntity publicEntity = new VariableEntityBean(privateEntity.getType(), participantIdentifier.generateParticipantIdentifier());
      if(!keyTable.hasValueSet(publicEntity)) {
        return publicEntity;
      }
    }

    throw new IllegalStateException("Unable to generate a unique public entity for the owner [" + ownerVariable + "] and private entity [" + privateEntity.getIdentifier() + "]. " + "One hundred attempts made.");
  }

  private ValueTable getKeyValueTable() {
    return keysValueTable;
  }
}
