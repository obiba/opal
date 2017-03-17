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
  private Set<String> sampleIds;

  public VCFSamplesSummaryBuilder mappings(VCFSamplesMapping value) {
    mappings = value;
    return this;
  }

  public VCFSamplesSummaryBuilder sampleIds(Collection<String> value) {
    sampleIds = value == null ? Sets.newHashSet() : Sets.newHashSet(value);
    return this;
  }

  public Stats buildGeneralSummary() {
    Stats stats = createStats();

    if (mappings != null) {
      int samples = 0;
      int controlSamples = 0;
      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Set<String> participantsWithGenotypes = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String role = vt.getValue(roleVariable, v).toString();
        boolean sampleFound = sampleIds.contains(sampleId);

        if (!Strings.isNullOrEmpty(participantId)) {
          participants.add(participantId);
          if (sampleFound) participantsWithGenotypes.add(participantId);
        }

        if (sampleFound) {
          if (VCFSampleRole.isSample(role)) samples++;
          else if (VCFSampleRole.isControl(role)) controlSamples++;
        }
      }

      if (samples + controlSamples > 0) {
        stats.setSamples(samples);
        stats.setControlSamples(controlSamples);
        stats.setParticipantsWithGenotypes(participantsWithGenotypes.size());
      }

      stats.setParticipants(participants.size());
    } else if (!sampleIds.isEmpty()){
      stats.setSamples(sampleIds.size());
    }


    return stats;
  }

  public Stats buildSummary() {
    Stats stats = createStats();

    if (mappings != null) {
      int orphanSamples = 0;
      int samples = 0;
      int controlSamples = 0;

      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String roleName = vt.getValue(roleVariable, v).toString();

        if (sampleIds.contains(sampleId)){
          if (Strings.isNullOrEmpty(participantId)) {
            if (VCFSampleRole.isSample(roleName)) orphanSamples++;
          } else {
            participants.add(participantId);
          }

          if (VCFSampleRole.isSample(roleName)) samples++;
          else if (VCFSampleRole.isControl(roleName)) controlSamples++;
        }

      }

      if (samples + controlSamples > 0) {
        // no samples matched
        stats.setSamples(samples);
        stats.setControlSamples(controlSamples);
        stats.setOrphanSamples(orphanSamples);
        stats.setParticipants(participants.size());
      }
    }

    return stats;
  }

  private Stats createStats() {
    Stats stats = new Stats();
    stats.samplesIdCount = sampleIds.size();
    return stats;
  }

  public static class Stats {
    int samplesIdCount = 0;

    Integer participants = null;
    Integer participantsWithGenotypes = null;
    Integer samples = null;
    Integer controlSamples = null;
    Integer orphanSamples = null;

    public int getSamplesIdCount() {
      return samplesIdCount;
    }

    public boolean hasParticipants() {
      return participants != null;
    }

    public void setParticipants(int value ) {
      participants = value;
    }

    public int getParticipants() {
      return participants;
    }

    public boolean hasParticipantsWithGenotypes() {
      return participantsWithGenotypes != null;
    }

    public void setParticipantsWithGenotypes(int value) {
      participantsWithGenotypes = value;
    }

    public int getParticipantsWithGenotypes() {
      return participantsWithGenotypes;
    }

    public boolean hasSamples() {
      return samples != null;
    }

    public void setSamples(int value) {
      samples = value;
    }

    public int getSamples() {
      return samples;
    }

    public boolean hasControlSamples() {
      return controlSamples != null;
    }

    public void setControlSamples(int value) {
      controlSamples = value;
    }

    public int getControlSamples() {
      return controlSamples;
    }

    public boolean hasOrphanSamples() {
      return orphanSamples != null;
    }

    public void setOrphanSamples(int value) {
      orphanSamples = value;
    }

    public int getOrphanSamples() {
      return orphanSamples;
    }
  }
}
