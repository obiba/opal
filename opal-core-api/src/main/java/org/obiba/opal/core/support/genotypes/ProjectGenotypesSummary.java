package org.obiba.opal.core.support.genotypes;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.obiba.magma.*;
import org.obiba.opal.core.domain.GenotypesMapping;
import org.obiba.opal.core.domain.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class ProjectGenotypesSummary {

  private static final Logger log = LoggerFactory.getLogger(ProjectGenotypesSummary.class);
  private final Project project;
  private final Datasource datasource;
  private Stats stats;

  public ProjectGenotypesSummary(Project source) {
    project = source;
    datasource = project.getDatasource();
  }

  public Stats calculate() {
    stats = new Stats();
    countParticipants();
    countGenotypesData();
    return stats;
  }

  private void countParticipants() {
    Set<VariableEntity> vts = datasource.getValueTables().stream()
      .filter(vt -> "Participant".equals(vt.getEntityType()))
      .map(ValueTable::getVariableEntities)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());

    // unique phenotype participants count
    stats.participants = vts.size();
  }

  private void countGenotypesData() {
    if (project.hasGenotypesMapping()) {
      GenotypesMapping gm = project.getGenotypesMapping();
      Iterable<ValueSet> valueSets = datasource.getValueTable(gm.getTableName()).getValueSets();
      String participantIdVariable = gm.getParticipantIdVariable();
      String roleVariable = gm.getSampleRoleVariable();
      Set<String> participantsWithGenotypes = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        ValueTable vt = v.getValueTable();
        String participantIdVariableValue = vt.getValue(vt.getVariable(participantIdVariable), v).toString();
        String roleVariableValue = vt.getValue(vt.getVariable(roleVariable), v).toString();

        if (!Strings.isNullOrEmpty(participantIdVariableValue)) participantsWithGenotypes.add(participantIdVariableValue);
        if ("sample".equals(roleVariableValue)) stats.samples++;
        else if ("control".equals(roleVariableValue)) stats.controlSamples++;
      }

      // unique genotype participants count
      stats.participantsWithGenotypes = participantsWithGenotypes.size();
    }
  }

  public static class Stats {
    long participants = 0L;
    long participantsWithGenotypes = 0L;
    long samples = 0L;
    long controlSamples = 0L;

    public long getParticipants() {
      return participants;
    }

    public long getParticipantsWithGenotypes() {
      return  participantsWithGenotypes;
    }

    public long getSamples() {
      return samples;
    }

    public long getControlSamples() {
      return controlSamples;
    }
  }
}
