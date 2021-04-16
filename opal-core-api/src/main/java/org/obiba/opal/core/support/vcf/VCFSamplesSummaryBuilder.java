/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.support.vcf;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.obiba.magma.*;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.VCFSampleRole;
import org.obiba.opal.core.domain.VCFSamplesMapping;

import java.util.Collection;
import java.util.Set;

public class VCFSamplesSummaryBuilder {

  public VCFSamplesSummaryBuilder() {
  }

  private VCFSamplesMapping mappings;

  private ValueTable mappingsTable;

  private Set<String> sampleIds;

  public VCFSamplesSummaryBuilder mappings(VCFSamplesMapping value) {
    mappings = value;
    try {
      mappingsTable = MagmaEngineTableResolver.valueOf(mappings.getTableReference()).resolveTable();
    } catch (NoSuchValueTableException|NoSuchDatasourceException e) {
      // ignore: table could be not accessible because of permission or because it was removed
      // so act like there was none
    }
    return this;
  }

  public VCFSamplesSummaryBuilder sampleIds(Collection<String> value) {
    sampleIds = value == null ? Sets.newHashSet() : Sets.newHashSet(value);
    return this;
  }

  public Stats buildGeneralSummary() {
    Stats stats = createStats();

    if (mappings != null && mappingsTable != null) {
      int identifiedSamples = 0;
      int controlSamples = 0;
      Iterable<ValueSet> valueSets = mappingsTable.getValueSets();
      Variable participantVariable = mappingsTable.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = mappingsTable.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        String participantId = mappingsTable.getValue(participantVariable, v).toString();
        String roleName = mappingsTable.getValue(roleVariable, v).toString();
        // check if the mapped sample is one of the samples observed in the vcf files
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
      }
      stats.setParticipants(participants.size());
    }

    return stats;
  }

  public Stats buildSummary() {
    Stats stats = createStats();

    if (mappings != null && mappingsTable != null) {
      int identifiedSamples = 0;
      int controlSamples = 0;

      Iterable<ValueSet> valueSets = mappingsTable.getValueSets();
      Variable participantVariable = mappingsTable.getVariable(mappings.getParticipantIdVariable());
      Variable roleVariable = mappingsTable.getVariable(mappings.getSampleRoleVariable());
      Set<String> participants = Sets.newHashSet();
      Set<String> mappedSampleIds = Sets.newHashSet();

      for (ValueSet v : valueSets) {
        String sampleId = v.getVariableEntity().getIdentifier();
        mappedSampleIds.add(sampleId);
        String participantId = mappingsTable.getValue(participantVariable, v).toString();
        String roleName = mappingsTable.getValue(roleVariable, v).toString();
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
