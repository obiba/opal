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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.VCFSampleRole;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.event.ValueTableDeletedEvent;
import org.obiba.opal.core.event.ValueTableRenamedEvent;
import org.obiba.opal.core.event.VariableDeletedEvent;
import org.obiba.opal.core.event.VariableRenamedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class VCFSamplesMappingServiceImpl implements VCFSamplesMappingService {

  @Autowired
  private OrientDbService orientDbService;


  @Override
  public void start() {
    orientDbService.createUniqueIndex(VCFSamplesMapping.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<VCFSamplesMapping> getVCFSamplesMappings() {
    return orientDbService.list(VCFSamplesMapping.class);
  }

  @Override
  public VCFSamplesMapping getVCFSamplesMapping(@NotNull String projectName) throws NoSuchProjectException {
    VCFSamplesMapping vcfSamplesMapping = orientDbService.findUnique(new VCFSamplesMapping(projectName));
    if(vcfSamplesMapping == null) throw new NoSuchVCFSamplesMappingException(projectName);
    return vcfSamplesMapping;
  }

  @Override
  public boolean hasVCFSamplesMapping(@NotNull String samplesMapping) {
    try {
      getVCFSamplesMapping(samplesMapping);
      return true;
    } catch(NoSuchVCFSamplesMappingException e) {
      return false;
    }
  }

  @Override
  public void save(@NotNull VCFSamplesMapping vcfSamplesMapping) throws ConstraintViolationException {
    orientDbService.save(vcfSamplesMapping, vcfSamplesMapping);
  }

  @Override
  public void delete(@NotNull String projectName) throws NoSuchVCFSamplesMappingException {
    VCFSamplesMapping vcfSamplesMapping = getVCFSamplesMapping(projectName);
    orientDbService.delete(vcfSamplesMapping, vcfSamplesMapping);
  }

  @Override
  public void deleteProjectSampleMappings(@NotNull String project) {
    Iterable<VCFSamplesMapping> list = orientDbService.list(
        VCFSamplesMapping.class,
        "select from " + VCFSamplesMapping.class.getSimpleName() + " where tableReference like ?",
        project);
    list.forEach(s -> delete(s.getProjectName()));
  }

  @Override
  public List<String> getFilteredSampleIds(@NotNull String projectName, String filteringTable, boolean withControl) {
    List<String> participantIds = Strings.isNullOrEmpty(filteringTable) ?
        Lists.newArrayList() :
        MagmaEngineTableResolver.valueOf(filteringTable).resolveTable()
            .getVariableEntities().stream().map(VariableEntity::getIdentifier).collect(Collectors.toList());

    Map<String, ParticipantRolePair> sampleParticipantMap = getSampleParticipantMap(projectName);
    return sampleParticipantMap.entrySet()
        .stream()
        .filter(e -> (e.getValue().getParticipantId() == null && VCFSampleRole.isControl(e.getValue().getRole()) && withControl) ||
            participantIds.contains(e.getValue().getParticipantId())
        )
        .map(Map.Entry::getKey).collect(Collectors.toList());
  }

  @Override
  public List<String> getControls(@NotNull String projectName) {
    Map<String, ParticipantRolePair> sampleParticipantMap = getSampleParticipantMap(projectName);
    return sampleParticipantMap.entrySet()
        .stream()
        .filter(e -> VCFSampleRole.isControl(e.getValue().getRole()))
        .map(Map.Entry::getKey).collect(Collectors.toList());
  }

  @Override
  public Map<String, ParticipantRolePair> findParticipantIdBySampleId(@NotNull String projectName, @NotNull Collection<String> samplesIds) {
    Map<String, ParticipantRolePair> sampleParticipantAndRoleMap = getSampleParticipantMap(projectName);
    final Map<String, ParticipantRolePair> sampleParticipantMap = new HashMap<>();

    samplesIds.forEach(sampleId -> {
      ParticipantRolePair participantRolePair = sampleParticipantAndRoleMap.get(sampleId);
      sampleParticipantMap.put(sampleId, participantRolePair != null ? participantRolePair : new ParticipantRolePair("", ""));
    });

    return sampleParticipantMap;
  }

  private Map<String, ParticipantRolePair> getSampleParticipantMap(@NotNull String projectName) {
    VCFSamplesMapping vcfSamplesMapping = getVCFSamplesMapping(projectName);
    ValueTable mappingValueTable = MagmaEngineTableResolver.valueOf(vcfSamplesMapping.getTableReference()).resolveTable();
    return mappingValueTable.getVariableEntities().stream()
        .collect(Collectors.toMap(
            VariableEntity::getIdentifier,
            v -> getVariableValue(mappingValueTable, v,
                vcfSamplesMapping.getParticipantIdVariable(), vcfSamplesMapping.getSampleRoleVariable())
        ));
  }

  private ParticipantRolePair getVariableValue(ValueTable valueTable, VariableEntity variableEntity,
                                               String participantVariableColumn, String roleVariableColumn) {
    ValueSet valueSet = valueTable.getValueSet(variableEntity);

    return new ParticipantRolePair(
        valueTable.getValue(valueTable.getVariable(participantVariableColumn), valueSet).toString(),
        valueTable.getValue(valueTable.getVariable(roleVariableColumn), valueSet).toString()
    );
  }

  @Subscribe
  public void onValueTableDeleted(ValueTableDeletedEvent event) {
    ValueTable vt = event.getValueTable();
    if (TABLE_ENTITY_TYPE.equals(vt.getEntityType())) {
      deleteProjectSampleMappings(vt.getDatasource().getName() + "." + vt.getName());
    }
  }

  @Subscribe
  public void onValueTableRenamed(ValueTableRenamedEvent event) {
    getMatchingMappingTables(event.getValueTable()).ifPresent(list -> {
      String newTableReference = String.format("%s.%s", event.getValueTable().getDatasource().getName(), event.getNewName());
      list.forEach(s -> {
        VCFSamplesMapping n = VCFSamplesMapping.newBuilder(s).tableName(newTableReference).build();
        orientDbService.save(n, n);
      });
    });
  }

  @Subscribe
  public void onVariableRenamed(VariableRenamedEvent event) {
    getMatchingMappingTables(event.getValueTable()).ifPresent(list -> {
      String variableName = event.getVariable().getName();
      list.forEach(s -> {
        VCFSamplesMapping.Builder b = VCFSamplesMapping.newBuilder(s);
        if (s.getParticipantIdVariable().equals(variableName)) {
          b.participantIdVariable(event.getNewName());
        } else if (s.getSampleRoleVariable().equals(variableName)) {
          b.sampleRoleVariable(event.getNewName());
        }

        VCFSamplesMapping n = b.build();
        orientDbService.save(n, n);
      });
    });
  }

  @Subscribe
  public void onVariableDeleted(VariableDeletedEvent event) {
    ValueTable vt = event.getValueTable();
    getMatchingMappingTables(vt).ifPresent(list -> {
      String variableName = event.getVariable().getName();
      list.forEach(s -> {
        if (s.getParticipantIdVariable().equals(variableName) || s.getSampleRoleVariable().equals(variableName)) {
          delete(s.getProjectName());
        }
      });
    });
  }

  /**
   * Return the mappings from all projects matching this table
   *
   * @param vt
   * @return
   */
  private Optional<Iterable<VCFSamplesMapping>> getMatchingMappingTables(ValueTable vt) {
    Optional<Iterable<VCFSamplesMapping>> list = Optional.empty();
    if (TABLE_ENTITY_TYPE.equals(vt.getEntityType())) {
      String tableReference = String.format("%s.%s", vt.getDatasource().getName(), vt.getName());
      list = Optional.ofNullable(orientDbService.list(VCFSamplesMapping.class,
          "select from " + VCFSamplesMapping.class.getSimpleName() + " where tableReference like ?",
          tableReference));
    }

    return list;
  }

  private Optional<VCFSamplesMapping> getVCFSamplesMappingBuilder(ValueTable valueTable) {
    String projectName = valueTable.getDatasource().getName();
    if (!hasVCFSamplesMapping(projectName)) return Optional.ofNullable(null);

    String tableReference = String.format("%s.%s", projectName, valueTable.getName());
    VCFSamplesMapping vcfSamplesMapping = getVCFSamplesMapping(projectName);

    return Optional.ofNullable(vcfSamplesMapping.getTableReference().equals(tableReference)
        ? vcfSamplesMapping
        : null);
  }
}
