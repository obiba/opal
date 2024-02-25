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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.RenameValueTable;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.Disposables;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.core.identifiers.IdentifierGenerator;
import org.obiba.opal.core.identifiers.IdentifiersMapping;
import org.obiba.opal.core.identifiers.IdentifiersMaps;
import org.obiba.opal.core.magma.PrivateVariableEntityMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Default implementation of {@link org.obiba.opal.core.service.IdentifiersImportService}.
 */
@Component
public class IdentifiersImportServiceImpl implements IdentifiersImportService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(IdentifiersImportServiceImpl.class);

  @Autowired
  private IdentifierGenerator participantIdentifier;

  @Autowired
  private IdentifiersTableService identifiersTableService;

  @Autowired
  private IdentifierService identifierService;

  @Override
  public int importIdentifiers(@NotNull IdentifiersMapping idMapping, @NotNull IdentifierGenerator pIdentifier) {
    IdentifierGenerator localParticipantIdentifier = pIdentifier == null ? participantIdentifier : pIdentifier;

    ValueTable identifiersTable = identifiersTableService.ensureIdentifiersTable(idMapping.getEntityType());
    Variable variable = identifiersTableService.ensureIdentifiersMapping(idMapping);
    PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(identifiersTable, variable,
        localParticipantIdentifier);

    List<VariableEntity> systemEntities = StreamSupport.stream(new IdentifiersMaps(identifiersTable, idMapping.getName()).spliterator(), false) //
    .filter(unitId -> !unitId.hasPrivateIdentifier()) //
    .map(IdentifiersMaps.IdentifiersMap::getSystemEntity)//
    .collect(Collectors.toList());

    entityMap.createPrivateEntities(systemEntities);

    return systemEntities.size();
  }

  @Override
  public void importIdentifiers(@NotNull IdentifiersMapping idMapping, Datasource sourceDatasource,
      @Nullable String select) throws IOException {
    try {
      for(ValueTable vt : sourceDatasource.getValueTables()) {
        if(idMapping.isForTable(vt)) {
          importIdentifiers(idMapping, vt, select);
        }
      }
    } finally {
      if(MagmaEngine.get().hasTransientDatasource(sourceDatasource.getName()))
        MagmaEngine.get().removeTransientDatasource(sourceDatasource.getName());
    }
  }

  private void importIdentifiers(@NotNull IdentifiersMapping idMapping, ValueTable sourceTable,
      @Nullable String select) {

    Variable variable = identifiersTableService.ensureIdentifiersMapping(idMapping);
    String selectScript = select == null //
        ? variable.hasAttribute("select") ? variable.getAttributeStringValue("select") : null //
        : select;

    ValueTable sourceIdentifiersTable = identifierService
        .createPrivateView(sourceTable.getName(), sourceTable, selectScript);
    Variable identifierVariable = identifierService.createIdentifierVariable(sourceIdentifiersTable, idMapping);

    PrivateVariableEntityMap entityMap = new OpalPrivateVariableEntityMap(
        identifiersTableService.getIdentifiersTable(idMapping.getEntityType()), identifierVariable,
        participantIdentifier);

    try(ValueTableWriter identifiersTableWriter = identifiersTableService
        .createIdentifiersTableWriter(idMapping.getEntityType())) {
      for(VariableEntity privateEntity : sourceIdentifiersTable.getVariableEntities()) {
        if(entityMap.publicEntity(privateEntity) == null) {
          entityMap.createPublicEntity(privateEntity);
        }
        identifierService
            .copyParticipantIdentifiers(entityMap.publicEntity(privateEntity), sourceIdentifiersTable, entityMap,
                identifiersTableWriter);
      }
    }
    Disposables.dispose(entityMap);
  }

  @Override
  public void importIdentifiers(ValueTable sourceValueTable) throws IOException {
    if (!identifiersTableService.hasDatasource()) return;
    ValueTable identifiersTable = identifiersTableService.ensureIdentifiersTable(sourceValueTable.getEntityType());

    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    builder.addAll(Iterables.transform(sourceValueTable.getVariableEntities(), new Function<VariableEntity, String>() {

      @Override
      public String apply(VariableEntity input) {
        return input.getIdentifier();
      }
    }));
    ValueTable sourceIdentifiersTable = new StaticValueTable(sourceValueTable.getDatasource(),
        identifiersTable.getName(), builder.build(), identifiersTable.getEntityType());

    // Don't copy null values otherwise, we'll delete existing mappings
    DatasourceCopier.Builder.newCopier().dontCopyNullValues().dontCopyMetadata().withLoggingListener().build()
        .copy(sourceIdentifiersTable, identifiersTableService.getDatasource());
  }

  @Override
  public void copyIdentifiers(ValueTable identifiersValueTable) throws IOException {
    if (!identifiersTableService.hasDatasource()) return;
    ValueTable destinationIdentifiersTable = identifiersTableService
        .ensureIdentifiersTable(identifiersValueTable.getEntityType());
    ValueTable sourceIdentifiersTable = identifiersValueTable;

    if(!destinationIdentifiersTable.getName().equals(identifiersValueTable.getName())) {
      sourceIdentifiersTable = new RenameValueTable(destinationIdentifiersTable.getName(), identifiersValueTable);
    }

    // Don't copy null values otherwise, we'll delete existing mappings
    DatasourceCopier.Builder.newCopier().dontCopyNullValues().dontCopyMetadata().withLoggingListener().build()
        .copy(sourceIdentifiersTable, identifiersTableService.getDatasource());
  }
}
