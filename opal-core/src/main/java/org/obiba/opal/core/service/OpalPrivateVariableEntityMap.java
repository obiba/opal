/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.obiba.magma.*;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.magma.type.TextType;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An Opal implementation of {@code PrivateVariableEntityMap}, on top of a Magma {@code ValueTable}.
 */
public class OpalPrivateVariableEntityMap implements PrivateVariableEntityMap {

  private static final Logger log = LoggerFactory.getLogger(OpalPrivateVariableEntityMap.class);

  @NotNull
  private final ValueTable keysValueTable;

  @NotNull
  private final Variable ownerVariable;

  @NotNull
  private final IdentifierGenerator participantIdentifier;

  @NotNull
  private final BiMap<VariableEntity, VariableEntity> publicToPrivate = Maps.synchronizedBiMap(HashBiMap.create());

  private ValueTableWriter entitiesValueTableWriter;

  public OpalPrivateVariableEntityMap(@NotNull ValueTable keysValueTable, @NotNull Variable ownerVariable,
                                      @NotNull IdentifierGenerator participantIdentifier) {
    Assert.notNull(keysValueTable, "keysValueTable cannot be null");
    Assert.notNull(ownerVariable, "ownerVariable cannot be null");
    Assert.notNull(participantIdentifier, "participantIdentifier cannot be null");

    this.keysValueTable = keysValueTable;
    this.ownerVariable = ownerVariable;
    this.participantIdentifier = participantIdentifier;

    constructCache();
  }

  @Override
  public VariableEntity publicEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    VariableEntity entity = publicToPrivate.inverse().get(privateEntity);
    log.debug("({}) <--> {}", privateEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public VariableEntity privateEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    VariableEntity entity = publicToPrivate.get(publicEntity);
    log.debug("{} <--> ({})", publicEntity.getIdentifier(), entity == null ? null : entity.getIdentifier());
    return entity;
  }

  @Override
  public boolean hasPrivateEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    return publicToPrivate.inverse().containsKey(privateEntity);
  }

  @Override
  public boolean hasPublicEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    return publicToPrivate.containsKey(publicEntity);
  }

  @Override
  public VariableEntity createPrivateEntity(@NotNull VariableEntity publicEntity) {
    Assert.notNull(publicEntity, "publicEntity cannot be null");
    for (int i = 0; i < 100; i++) {
      VariableEntity privateEntity = entityFor(participantIdentifier.generateIdentifier());
      if (!publicToPrivate.inverse().containsKey(privateEntity)) {
        writeEntities(getEntitiesValueTableWriter(keysValueTable), publicEntity, privateEntity);
        log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
        publicToPrivate.put(entityFor(publicEntity.getIdentifier()), privateEntity);
        return privateEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique private entity for the owner [" + ownerVariable + "] and public entity [" +
            publicEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  @Override
  public List<VariableEntity> createPrivateEntities(@NotNull List<VariableEntity> publicEntities) {
    Assert.notNull(publicEntities, "publicEntity cannot be null");

    Stopwatch stopwatch = Stopwatch.createStarted();
    log.info("IDs generation started for {} entities", publicEntities.size());
    Map<VariableEntity, VariableEntity> publicPrivateEntities = Maps.newHashMap();
    publicEntities.forEach(publicEntity -> {
      boolean generated = false;
      int i = 0;
      while (!generated && i < 100) {
        VariableEntity privateEntity = entityFor(participantIdentifier.generateIdentifier());
        if (!publicToPrivate.inverse().containsKey(privateEntity)) {
          publicToPrivate.put(entityFor(publicEntity.getIdentifier()), privateEntity);
          publicPrivateEntities.put(publicEntity, privateEntity);
          generated = true;
        }
        i++;
      }
      if (!generated)
        throw new IllegalStateException(
            "Unable to generate a unique private entity for the owner [" + ownerVariable + "] and public entity [" +
                publicEntity.getIdentifier() + "]. One hundred attempts made.");
    });
    log.info("IDs generated for {} entities in {} ms", publicEntities.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

    // take advantage of batch writing of the ids datasource
    ValueTableWriter vtw = keysValueTable.getDatasource().createWriter(keysValueTable.getName(), keysValueTable.getEntityType());
    publicPrivateEntities.forEach((publicEntity, privateEntity) -> writeEntities(vtw, publicEntity, privateEntity));
    vtw.close();

    log.info("IDs generation done for {} entities in {}", publicEntities.size(), stopwatch.stop());
    return new ArrayList<>(publicPrivateEntities.values());
  }

  @Override
  public VariableEntity createPublicEntity(@NotNull VariableEntity privateEntity) {
    Assert.notNull(privateEntity, "privateEntity cannot be null");
    for (int i = 0; i < 100; i++) {
      VariableEntity publicEntity = entityFor(participantIdentifier.generateIdentifier());
      if (!publicToPrivate.containsKey(publicEntity)) {
        writeEntities(getEntitiesValueTableWriter(keysValueTable), publicEntity, privateEntity);
        publicToPrivate.put(publicEntity, entityFor(privateEntity.getIdentifier()));
        return publicEntity;
      }
    }
    throw new IllegalStateException(
        "Unable to generate a unique public entity for the owner [" + ownerVariable + "] and private entity [" +
            privateEntity.getIdentifier() + "]. One hundred attempts made.");
  }

  private ValueTableWriter getEntitiesValueTableWriter(ValueTable keyTable) {
    if (entitiesValueTableWriter == null)
      entitiesValueTableWriter = keyTable.getDatasource().createWriter(keyTable.getName(), keyTable.getEntityType());
    return entitiesValueTableWriter;
  }

  private void writeEntities(ValueTableWriter vtw, VariableEntity publicEntity, VariableEntity privateEntity) {
    ValueSetWriter vsw = vtw.writeValueSet(publicEntity);
    vsw.writeValue(ownerVariable, TextType.get().valueOf(privateEntity.getIdentifier()));
    vsw.close();
    log.debug("{} <--> ({}) added", publicEntity.getIdentifier(), privateEntity.getIdentifier());
  }

  private VariableEntity entityFor(String identifier) {
    return new VariableEntityBean(keysValueTable.getEntityType(), identifier);
  }

  private void constructCache() {
    log.info("Constructing participant identifier cache for keysValueTable: {}, ownerVariable: {}",
        keysValueTable.getName(), ownerVariable.getName());
    VariableValueSource ownerVariableSource = keysValueTable.getVariableValueSource(ownerVariable.getName());
    if (ownerVariableSource.supportVectorSource()) {
      constructCacheFromVector(keysValueTable, ownerVariableSource.asVectorSource());
    } else {
      constructCacheFromTable(keysValueTable, ownerVariableSource);
    }
    log.debug("Cache constructed: {}", publicToPrivate);
  }

  private void constructCacheFromTable(@NotNull ValueTable keyTable, @NotNull ValueSource ownerVariableSource) {
    for (ValueSet valueSet : keyTable.getValueSets()) {
      Value value = ownerVariableSource.getValue(valueSet);
      // OPAL-619: The value could be null, in which case don't cache it. Whenever new participant
      // data are imported, the key variable is written first and its corresponding VariableValueSource
      // is added *without a value* (see DefaultImportService.createKeyVariable()). The key variable's
      // value is written afterwards, in the process of copying the participant data to the destination
      // datasource. Also, more obviously, it is not necessarily the case that all value sets have a value
      // for all key variables.
      if (!value.isNull()) {
        log.debug("{} <--> ({}) cached", valueSet.getVariableEntity().getIdentifier(), value);
        publicToPrivate.put(entityFor(valueSet.getVariableEntity().getIdentifier()), entityFor(value.toString()));
      }
    }
  }

  private void constructCacheFromVector(@NotNull ValueTable keyTable, @NotNull VectorSource vs) {
    List<VariableEntity> entities = keyTable.getVariableEntities();
    Iterator<Value> values = vs.getValues(entities).iterator();
    for (VariableEntity opalEntity : entities) {
      Value value = values.next();
      if (!value.isNull()) {
        log.debug("{} <--> ({}) cached", opalEntity.getIdentifier(), value);
        publicToPrivate.put(entityFor(opalEntity.getIdentifier()), entityFor(value.toString()));
      }
    }
  }

  @Override
  public void dispose() {
    if (entitiesValueTableWriter != null) {
      entitiesValueTableWriter.close();
      entitiesValueTableWriter = null;
    }
  }
}
