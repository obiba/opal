package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.VCFSampleRole;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class VCFSamplesMappingServiceImpl implements VCFSamplesMappingService {

  @Autowired
  private OrientDbService orientDbService;


  @Override
  @PostConstruct
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
        .filter(e -> (e.getValue().getKey() == null && VCFSampleRole.isControl(e.getValue().getValue()) && withControl) ||
            participantIds.contains(e.getValue().getKey())
        )
        .map(Map.Entry::getKey).collect(Collectors.toList());
  }

  @Override
  public List<String> getControls(@NotNull String projectName) {
    Map<String, ParticipantRolePair> sampleParticipantMap = getSampleParticipantMap(projectName);
    return sampleParticipantMap.entrySet()
        .stream()
        .filter(e -> VCFSampleRole.isControl(e.getValue().getValue()))
        .map(Map.Entry::getKey).collect(Collectors.toList());
  }

  @Override
  public Map<String, String> findParticipantIdBySampleId(@NotNull String projectName, @NotNull Collection<String> samplesIds) {
    Map<String, ParticipantRolePair> sampleParticipantAndRoleMap = getSampleParticipantMap(projectName);
    final Map<String, String> sampleParticipantMap = new HashMap<>();

    sampleParticipantAndRoleMap.forEach((sampleId, participantRolePair) -> {
      if (samplesIds.contains(sampleId))
        sampleParticipantMap.put(sampleId, participantRolePair.getKey());
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

  private class ParticipantRolePair implements Map.Entry<String, String> {

    private String participant;
    private String role;

    ParticipantRolePair(String participant, String role) {
      this.participant = participant;
      this.role = role;
    }

    @Override
    public String getKey() {
      return participant;
    }

    @Override
    public String getValue() {
      return role;
    }

    @Override
    public String setValue(String value) {
      String oldValue = role;
      role = value;
      return oldValue;
    }
  }
}
