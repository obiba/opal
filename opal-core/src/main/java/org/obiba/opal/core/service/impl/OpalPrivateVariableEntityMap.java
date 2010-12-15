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

import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * An Opal implementation of {@code PrivateVariableEntityMap}, on top of a Magma {@code ValueTable}.
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {

  private static final Logger log = LoggerFactory.getLogger(OpalPrivateVariableEntityMap.class);

  private final ValueTable keysValueTable;

  private final Variable ownerVariable;

  private final IParticipantIdentifier participantIdentifier;

  private final BiMap<VariableEntity, VariableEntity> publicToPrivate = HashBiMap.create();

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

    constructCache();
  }

  //
  // PrivateVariableEntityMap Methods
  //

  @Override
  public VariableEntity publicEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");
    VariableEntity entity = publicToPrivate.inverse().get(privateEntity);
    log.debug("({})<-->{}", privateEntity.getIdentifier(), entity != null ? entity.getIdentifier() : null);
    return entity;
  }

  @Override
  public VariableEntity privateEntity(VariableEntity publicEntity) {
    if(publicEntity == null) throw new IllegalArgumentException("publicEntity cannot be null");
    VariableEntity entity = publicToPrivate.get(publicEntity);
    log.debug("{}<-->({})", publicEntity.getIdentifier(), entity != null ? entity.getIdentifier() : null);
    return entity;
  }

  @Override
  public boolean hasPrivateEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");
    return publicToPrivate.inverse().containsKey(privateEntity);
  }

  @Override
  public boolean hasPublicEntity(VariableEntity publicEntity) {
    if(publicEntity == null) throw new IllegalArgumentException("publicEntity cannot be null");
    return publicToPrivate.containsKey(publicEntity);
  }

  @Override
  public VariableEntity createPrivateEntity(VariableEntity publicEntity) {
    if(publicEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");
    ValueTable keyTable = getKeyValueTable();
    for(int i = 0; i < 100; i++) {
      VariableEntity privateEntity = entityFor(participantIdentifier.generateParticipantIdentifier());
      if(publicToPrivate.inverse().containsKey(privateEntity) == false) {
        try {
          writeEntities(keyTable, publicEntity, privateEntity);
          log.debug("{}<-->({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
          publicToPrivate.put(entityFor(publicEntity.getIdentifier()), privateEntity);
        } catch(IOException e) {
          throw new MagmaRuntimeException(e);
        }
        return privateEntity;
      }
    }
    throw new IllegalStateException("Unable to generate a unique private entity for the owner [" + ownerVariable + "] and public entity [" + publicEntity.getIdentifier() + "]. " + "One hundred attempts made.");
  }

  @Override
  public VariableEntity createPublicEntity(VariableEntity privateEntity) {
    if(privateEntity == null) throw new IllegalArgumentException("privateEntity cannot be null");
    ValueTable keyTable = getKeyValueTable();
    for(int i = 0; i < 100; i++) {
      VariableEntity publicEntity = entityFor(participantIdentifier.generateParticipantIdentifier());
      if(publicToPrivate.containsKey(publicEntity) == false) {
        try {
          writeEntities(keyTable, publicEntity, privateEntity);
          publicToPrivate.put(publicEntity, entityFor(privateEntity.getIdentifier()));
        } catch(IOException e) {
          throw new MagmaRuntimeException(e);
        }
        return publicEntity;
      }
    }
    throw new IllegalStateException("Unable to generate a unique public entity for the owner [" + ownerVariable + "] and private entity [" + privateEntity.getIdentifier() + "]. " + "One hundred attempts made.");
  }

  private void writeEntities(ValueTable keyTable, VariableEntity publicEntity, VariableEntity privateEntity) throws IOException {
    ValueTableWriter vtw = keyTable.getDatasource().createWriter(keyTable.getName(), keyTable.getEntityType());
    ValueSetWriter vsw = vtw.writeValueSet(publicEntity);
    vsw.writeValue(this.ownerVariable, TextType.get().valueOf(privateEntity.getIdentifier()));
    vsw.close();
    vtw.close();
    log.debug("{}<-->({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
  }

  private ValueTable getKeyValueTable() {
    return keysValueTable;
  }

  private VariableEntity entityFor(String identifier) {
    return new VariableEntityBean(keysValueTable.getEntityType(), identifier);
  }

  private void constructCache() {
    log.info("Constructing participant identifier cache");
    ValueTable keyTable = getKeyValueTable();
    VariableValueSource ownerVariableSource = keyTable.getVariableValueSource(ownerVariable.getName());
    if(ownerVariableSource.asVectorSource() != null) {
      constructCacheFromVector(keyTable, ownerVariableSource.asVectorSource());
    } else {
      constructCacheFromTable(keyTable, ownerVariableSource);
    }
    log.info("Done");
  }

  private void constructCacheFromTable(ValueTable keyTable, VariableValueSource ownerVariableSource) {
    for(ValueSet valueSet : keyTable.getValueSets()) {
      Value value = ownerVariableSource.getValue(valueSet);
      // OPAL-619: The value could be null, in which case don't cache it. Whenever new participant
      // data are imported, the key variable is written first and its corresponding VariableValueSource
      // is added *without a value* (see DefaultImportService.prepareKeysTable()). The key variable's
      // value is written afterwards, in the process of copying the participant data to the destination
      // datasource. Also, more obviously, it is not necessarily the case that all value sets have a value
      // for all key variables.
      if(!value.isNull()) {
        log.debug("{}<-->({}) cached", valueSet.getVariableEntity().getIdentifier(), value.toString());
        publicToPrivate.put(entityFor(valueSet.getVariableEntity().getIdentifier()), entityFor(value.toString()));
      }
    }
  }

  private void constructCacheFromVector(ValueTable keyTable, VectorSource vs) {
    SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(keyTable.getVariableEntities());
    Iterator<Value> values = vs.getValues(entities).iterator();
    for(VariableEntity opalEntity : entities) {
      Value value = values.next();
      if(!value.isNull()) {
        log.debug("{}<-->({}) cached", opalEntity.getIdentifier(), value.toString());
        publicToPrivate.put(entityFor(opalEntity.getIdentifier()), entityFor(value.toString()));
      }
    }
  }
}
