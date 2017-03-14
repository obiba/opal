package org.obiba.opal.core.support.vcf;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.spi.vcf.VCFStore;

import java.util.Collection;
import java.util.Set;

public class VCFSamplesSummaryBuilder {

  public VCFSamplesSummaryBuilder() {
  }

  private VCFSamplesMapping mappings;
  private VCFStore store;
  private Datasource datasource;

  public VCFSamplesSummaryBuilder datasource(Datasource value) {
    datasource = value;
    return this;
  }

  public VCFSamplesSummaryBuilder mappings(VCFSamplesMapping value) {
    mappings = value;
    return this;
  }

  public VCFSamplesSummaryBuilder store(VCFStore value) {
    store = value;
    return this;
  }

  public Stats buildSummary() {
    Stats stats = new Stats();

    if (mappings != null) {
      ValueTable vt = datasource.getValueTable(mappings.getTableName());
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Set<String> participantsWithGenotypes = Sets.newHashSet();
      Collection<String> allSamples = store == null ? Lists.newArrayList() : store.getSampleIds();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String role = vt.getValue(roleVariable, v).toString();

        if (!Strings.isNullOrEmpty(participantId)) {
          participants.add(participantId);
          if (allSamples.contains(sampleId)) participantsWithGenotypes.add(participantId);
        }
        if ("sample".equals(role)) stats.samples++;
        else if ("control".equals(role)) stats.controlSamples++;
      }

      // unique genotype participants count
      stats.participants = participants.size();
      stats.participantsWithGenotypes = participantsWithGenotypes.size();
    }

    return stats;
  }

  public Stats buildSummary(String vcfFile) {
    Stats stats = new Stats();

    if (mappings != null) {
      ValueTable vt = datasource.getValueTable(mappings.getTableName());
      Iterable<ValueSet> valueSets = vt.getValueSets();
      Variable participantVariable = vt.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = vt.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Collection<String> allSamples = store == null ? Lists.newArrayList() : store.getSampleIds(vcfFile);

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = vt.getValue(participantVariable, v).toString();
        String role = vt.getValue(roleVariable, v).toString();

        if (allSamples.contains(sampleId)){
          if (Strings.isNullOrEmpty(participantId)) {
            if ("sample".equals(role)) stats.orphanSamples++;
          } else {
            participants.add(participantId);
          }

          if ("control".equals(role)) stats.controlSamples++;
        }

      }

      // unique genotype participants count
      stats.participants = participants.size();
    }

    return stats;
  }

  public static class Stats {
    long participants = 0L;
    long participantsWithGenotypes = 0L;
    long samples = 0L;
    long controlSamples = 0L;
    long orphanSamples = 0L;

    public long getParticipants() {
      return participants;
    }

    public long getParticipantsWithGenotypes() {
      return participantsWithGenotypes;
    }

    public long getSamples() {
      return samples;
    }

    public long getControlSamples() {
      return controlSamples;
    }

    public long getOrphanSamples() {
      return orphanSamples;
    }
  }
}
