package org.obiba.opal.core.support.vcf;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.VCFSampleRole;
import org.obiba.opal.core.domain.VCFSamplesMapping;

import java.util.Collection;
import java.util.Set;

public class VCFSamplesSummaryBuilder {

  public VCFSamplesSummaryBuilder() {
  }

  private VCFSamplesMapping mappings;
  private Collection<String> sampleIds;

  public VCFSamplesSummaryBuilder mappings(VCFSamplesMapping value) {
    mappings = value;
    return this;
  }

  public VCFSamplesSummaryBuilder sampleIds(Collection<String> value) {
    sampleIds = value;
    return this;
  }

  public Stats buildGeneralSummary() {
    Stats stats = new Stats();

    if (mappings != null) {
      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Set<String> participantsWithGenotypes = Sets.newHashSet();
      Collection<String> allSamples = sampleIds == null ? Lists.newArrayList() : sampleIds;

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String role = vt.getValue(roleVariable, v).toString();
        boolean sampleFound = allSamples.contains(sampleId);

        if (!Strings.isNullOrEmpty(participantId)) {
          participants.add(participantId);
          if (sampleFound) participantsWithGenotypes.add(participantId);
        }
        if (sampleFound) {
          if (VCFSampleRole.isSample(role)) stats.samples++;
          else if (VCFSampleRole.isControl(role)) stats.controlSamples++;
        }
      }

      // unique genotype participants count
      stats.participants = participants.size();
      stats.participantsWithGenotypes = participantsWithGenotypes.size();
    }

    return stats;
  }

  public Stats buildSummary() {
    Stats stats = new Stats();

    if (mappings != null) {
      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Collection<String> allSamples = sampleIds == null ? Lists.newArrayList() : sampleIds;

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String roleName = vt.getValue(roleVariable, v).toString();

        if (allSamples.contains(sampleId)){
          if (Strings.isNullOrEmpty(participantId)) {
            if (VCFSampleRole.isSample(roleName)) stats.orphanSamples++;
          } else {
            participants.add(participantId);
          }

          if (VCFSampleRole.isSample(roleName)) stats.samples++;
          else if (VCFSampleRole.isControl(roleName)) stats.controlSamples++;
        }

      }

      // unique genotype participants count
      stats.participants = participants.size();
    }

    return stats;
  }

  public static class Stats {
    int participants = 0;
    int participantsWithGenotypes = 0;
    int samples = 0;
    int controlSamples = 0;
    int orphanSamples = 0;

    public int getParticipants() {
      return participants;
    }

    public int getParticipantsWithGenotypes() {
      return participantsWithGenotypes;
    }

    public int getSamples() {
      return samples;
    }

    public int getControlSamples() {
      return controlSamples;
    }

    public int getOrphanSamples() {
      return orphanSamples;
    }

    public boolean hasSamples() {
      return samples + controlSamples > 0;
    }
  }
}
