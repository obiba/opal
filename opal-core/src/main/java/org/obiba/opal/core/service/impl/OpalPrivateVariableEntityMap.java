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

import javax.annotation.Nonnull;

import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.Value;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueSource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VectorSource;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.domain.participant.identifier.IParticipantIdentifier;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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

  public OpalPrivateVariableEntityMap(@Nonnull ValueTable keysValueTable, @Nonnull Variable ownerVariable,
      @Nonnull IParticipantIdentifier participantIdentifier) {
    Assert.notNull(keysValueTable, "keysValueTable cannot be null");
    Assert.notNull(ownerVariable, "ownerVariable cannot be null");
    Assert.notNull(participantIdentifier, "participantIdentifier cannot be null");

    this.keysValueTable = keysValueTable;
    this.ownerVariable = ownerVariable;
    this.participantIdentifier = participantIdentifier;

    constructCache();
  }

  @Override
  public VariableEntity publicEntity(@Nonnull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    VariableEntity entity = publicToPrivate.inverse().get(privateEntity);
    log.debug("({}) <--> {}", privateEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public VariableEntity privateEntity(@Nonnull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    VariableEntity entity = publicToPrivate.get(publicEntity);
    log.debug("{} <--> ({})", publicEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public boolean hasPrivateEntity(@Nonnull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    return publicToPrivate.inverse().containsKey(privateEntity);
  }

  @Override
  public boolean hasPublicEntity(@Nonnull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    return publicToPrivate.containsKey(publicEntity);
  }

  @Override
  public VariableEntity createPrivateEntity(@Nonnull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    for(int i = 0; i < 100; i++) {
      VariableEntity privateEntity = entityFor(participantIdentifier.generateParticipantIdentifier());
      if(!publicToPrivate.inverse().containsKey(privateEntity)) {
        try {
          writeEntities(keysValueTable, publicEntity, privateEntity);
          log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
          publicToPrivate.put(entityFor(publicEntity.getIdentifier()), privateEntity);
        } catch(IOException e) {
          throw new MagmaRuntimeException(e);
        }
        return privateEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique private entity for the owner [" + ownerVariable + "] and public entity [" +
            publicEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  @Override
  public VariableEntity createPublicEntity(@Nonnull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    for(int i = 0; i < 100; i++) {
      VariableEntity publicEntity = entityFor(participantIdentifier.generateParticipantIdentifier());
      if(!publicToPrivate.containsKey(publicEntity)) {
        try {
          writeEntities(keysValueTable, publicEntity, privateEntity);
          publicToPrivate.put(publicEntity, entityFor(privateEntity.getIdentifier()));
        } catch(IOException e) {
          throw new MagmaRuntimeException(e);
        }
        return publicEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique public entity for the owner [" + ownerVariable + "] and private entity [" +
            privateEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  private void writeEntities(ValueTable keyTable, VariableEntity publicEntity, VariableEntity privateEntity)
      throws IOException {
    ValueTableWriter vtw = keyTable.getDatasource().createWriter(keyTable.getName(), keyTable.getEntityType());
    ValueSetWriter vsw = vtw.writeValueSet(publicEntity);
    vsw.writeValue(ownerVariable, TextType.get().valueOf(privateEntity.getIdentifier()));
    vsw.close();
    vtw.close();
    log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
  }

  private VariableEntity entityFor(String identifier) {
    return new VariableEntityBean(keysValueTable.getEntityType(), identifier);
  }

  private void constructCache() {
    log.info("Constructing participant identifier cache for keysValueTable: {}, ownerVariable: {}",
        keysValueTable.getName(), ownerVariable.getName());
    VariableValueSource ownerVariableSource = keysValueTable.getVariableValueSource(ownerVariable.getName());
    if(ownerVariableSource.asVectorSource() == null) {
      constructCacheFromTable(keysValueTable, ownerVariableSource);
    } else {
      constructCacheFromVector(keysValueTable, ownerVariableSource.asVectorSource());
    }
    log.debug("Cache constructed: {}", publicToPrivate);
  }

  private void constructCacheFromTable(@Nonnull ValueTable keyTable, @Nonnull ValueSource ownerVariableSource) {
    for(ValueSet valueSet : keyTable.getValueSets()) {
      Value value = ownerVariableSource.getValue(valueSet);
      // OPAL-619: The value could be null, in which case don't cache it. Whenever new participant
      // data are imported, the key variable is written first and its corresponding VariableValueSource
      // is added *without a value* (see DefaultImportService.createKeyVariable()). The key variable's
      // value is written afterwards, in the process of copying the participant data to the destination
      // datasource. Also, more obviously, it is not necessarily the case that all value sets have a value
      // for all key variables.
      if(!value.isNull()) {
        log.debug("{} <--> ({}) cached", valueSet.getVariableEntity().getIdentifier(), value.toString());
        publicToPrivate.put(entityFor(valueSet.getVariableEntity().getIdentifier()), entityFor(value.toString()));
      }
    }
  }

  private void constructCacheFromVector(@Nonnull ValueTable keyTable, @Nonnull VectorSource vs) {
    SortedSet<VariableEntity> entities = new TreeSet<VariableEntity>(keyTable.getVariableEntities());
    Iterator<Value> values = vs.getValues(entities).iterator();
    for(VariableEntity opalEntity : entities) {
      Value value = values.next();
      if(!value.isNull()) {
        log.debug("{} <--> ({}) cached", opalEntity.getIdentifier(), value.toString());
        publicToPrivate.put(entityFor(opalEntity.getIdentifier()), entityFor(value.toString()));
      }
    }
  }
}
