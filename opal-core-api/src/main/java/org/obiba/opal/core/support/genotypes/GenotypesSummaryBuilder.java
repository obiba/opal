package org.obiba.opal.core.support.genotypes;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.domain.GenotypesMapping;
import org.obiba.opal.spi.vcf.VCFStore;

import java.util.Collection;
import java.util.Set;

public class GenotypesSummaryBuilder {

  public GenotypesSummaryBuilder() {
  }

  private GenotypesMapping mappings;
  private VCFStore store;
  private Datasource datasource;

  public GenotypesSummaryBuilder datasource(Datasource value) {
    datasource = value;
    return this;
  }

  public GenotypesSummaryBuilder mappings(GenotypesMapping value) {
    mappings = value;
    return this;
  }

  public GenotypesSummaryBuilder store(VCFStore value) {
    store = value;
    return this;
  }

  public Stats buildSummary() {
    Stats stats = new Stats();

    if (mappings != null) {
      Iterable<ValueSet> valueSets = datasource.getValueTable(mappings.getTableName()).getValueSets();
      String participantIdVariable = mappings.getParticipantIdVariable();
      String roleVariable = mappings.getSampleRoleVariable();
      Set<String> participants = Sets.newHashSet();
      Set<String> participantsWithGenotypes = Sets.newHashSet();
      Collection<String> allSamples = store == null ? Lists.newArrayList() : store.getSampleIds();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        ValueTable vt = v.getValueTable();
        String participantIdVariableValue = vt.getValue(vt.getVariable(participantIdVariable), v).toString();
        String roleVariableValue = vt.getValue(vt.getVariable(roleVariable), v).toString();

        if (!Strings.isNullOrEmpty(participantIdVariableValue)) {
          participants.add(participantIdVariableValue);
          if (allSamples.contains(sampleId)) participantsWithGenotypes.add(participantIdVariableValue);
        }
        if ("sample".equals(roleVariableValue)) stats.samples++;
        else if ("control".equals(roleVariableValue)) stats.controlSamples++;
      }

      // unique genotype participants count
      stats.participants = participants.size();
      stats.participantsWithGenotypes = participantsWithGenotypes.size();
    }

    return stats;
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
      return participantsWithGenotypes;
    }

    public long getSamples() {
      return samples;
    }

    public long getControlSamples() {
      return controlSamples;
    }
  }
}
