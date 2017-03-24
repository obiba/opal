package org.obiba.opal.core.support.vcf;

import com.google.common.base.Strings;
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
      int identifiedSamples = 0;
      int controlSamples = 0;
      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String role = vt.getValue(roleVariable, v).toString();
        // check if the mapped sample is one of the samples observed in the vcf files
        if (sampleIds.contains(sampleId)) {
          if (VCFSampleRole.isControl(role)) controlSamples++;
          if (!Strings.isNullOrEmpty(participantId)) {
            participants.add(participantId);
            identifiedSamples++;
          }
        }
      }

      if (identifiedSamples + controlSamples > 0) {
        stats.setIdentifiedSamples(identifiedSamples);
        stats.setControlSamples(controlSamples);
      }
      stats.setParticipants(participants.size());
    }

    return stats;
  }

  public Stats buildSummary() {
    Stats stats = createStats();

    if (mappings != null) {
      int orphanSamples = 0;
      int identifiedSamples = 0;
      int controlSamples = 0;

      ValueTable vt = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Set<String> mappedSampleIds = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        mappedSampleIds.add(sampleId);
        String participantId = vt.getValue(participantVariable, v).toString();
        String roleName = vt.getValue(roleVariable, v).toString();
        // check if the mapped sample is one of the samples observed in the vcf file
        if (sampleIds.contains(sampleId)) {
          if (VCFSampleRole.isControl(roleName)) controlSamples++;
          if (!Strings.isNullOrEmpty(participantId)) {
            participants.add(participantId);
            identifiedSamples++;
          }
        }
      }

      if (identifiedSamples + controlSamples > 0) {
        stats.setIdentifiedSamples(identifiedSamples);
        stats.setControlSamples(controlSamples);
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
    Integer identifiedSamples = null;
    Integer controlSamples = null;

    public int getSamplesIdCount() {
      return samplesIdCount;
    }

    public boolean hasParticipants() {
      return participants != null;
    }

    public void setParticipants(int value) {
      participants = value;
    }

    public int getParticipants() {
      return participants;
    }

    public boolean hasIdentifiedSamples() {
      return identifiedSamples != null;
    }

    public void setIdentifiedSamples(int value) {
      identifiedSamples = value;
    }

    public int getIdentifiedSamples() {
      return identifiedSamples;
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

  }
}
