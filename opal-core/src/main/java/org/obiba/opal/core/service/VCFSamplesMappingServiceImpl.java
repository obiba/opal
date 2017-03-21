package org.obiba.opal.core.service;

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
  public List<String> getFilteredSampleIds(@NotNull String projectName, @NotNull String filteringTable, boolean withControl) {
    ValueTable filteringValueTable = MagmaEngineTableResolver.valueOf(filteringTable).resolveTable();
    List<String> participantIds = filteringValueTable.getVariableEntities().stream().map(VariableEntity::getIdentifier).collect(Collectors.toList());

    Map<String, ParticipantRolePair> sampleParticipantMap = getSampleParticipantMap(projectName);
    return sampleParticipantMap.entrySet()
        .stream()
        .filter(e -> (e.getValue().getKey() == null && VCFSampleRole.isControl(e.getValue().getValue()) && withControl) || participantIds.contains(e.getValue().getKey()))
        .map(Map.Entry::getKey).collect(Collectors.toList());
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
