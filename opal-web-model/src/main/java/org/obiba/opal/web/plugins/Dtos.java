/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;

import com.google.common.collect.Lists;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.runtime.Plugin;
import org.obiba.opal.core.support.vcf.VCFSamplesSummaryBuilder;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.web.model.Plugins;

import java.util.List;
import java.util.Properties;

public class Dtos {

  private static List<String> reservedProperties = Lists.newArrayList("OPAL_HOME", "install.dir", "data.dir", "work.dir");

  public static Plugins.PluginDto asDto(Plugin plugin) {
    Plugins.PluginDto.Builder builder = Plugins.PluginDto.newBuilder();
    Properties properties = plugin.getProperties();
    properties.entrySet().stream().filter(entry -> !reservedProperties.contains(entry.getKey().toString())).forEach(entry -> {
      if ("name".equals(entry.getKey())) builder.setName(entry.getValue().toString());
      else if ("title".equals(entry.getKey())) builder.setTitle(entry.getValue().toString());
      else if ("description".equals(entry.getKey())) builder.setDescription(entry.getValue().toString());
      else if ("version".equals(entry.getKey())) builder.setVersion(entry.getValue().toString());
      else if ("opal.version".equals(entry.getKey())) builder.setOpalVersion(entry.getValue().toString());
      else builder.addProperties(Plugins.PropertyDto.newBuilder().setKey(entry.getKey().toString()).setValue(entry.getValue().toString()));
    });
    return builder.build();
  }

  public static Plugins.VCFStoreDto asDto(VCFStore store, VCFSamplesSummaryBuilder.Stats stats) {
    return Plugins.VCFStoreDto.newBuilder()
      .setName(store.getName()) //
      .setTotalSamplesCount(store.getSampleIds().size()) //
      .setParticipantsCount(stats.getParticipants()) //
      .setParticipantsWithGenotypeCount(stats.getParticipantsWithGenotypes()) //
      .setControlSamplesCount(stats.getControlSamples()) //
      .setSamplesCount(stats.getSamples()) //
      .addAllVcf(store.getVCFNames())
      .build();
  }

  public static Plugins.VCFSummaryDto asDto(VCFStore.VCFSummary summary, VCFSamplesSummaryBuilder.Stats stats) {
    return Plugins.VCFSummaryDto.newBuilder()
      .setName(summary.getName()) //
      .setSize(summary.size()) //
      .setTotalSamplesCount(summary.getSampleIds().size()) //
      .setSamplesCount(stats.getSamples()) //
      .setGenotypesCount(summary.getGenotypesCount()) //
      .setVariantsCount(summary.getVariantsCount()) //
      .setParticipantsCount(stats.getParticipants()) //
      .setOrphanSamplesCount(stats.getOrphanSamples())
      .setControlSamplesCount(stats.getControlSamples())
      .build();
  }

  public static VCFSamplesMapping fromDto(Plugins.VCFSamplesMappingDto dto) {
    return VCFSamplesMapping.newBuilder()
      .projectName(dto.getProjectName())
      .tableName(dto.getTableReference())
      .participantIdVariable(dto.getParticipantIdVariable())
      .sampleIdVariable(dto.getSampleIdVariable())
      .sampleRoleVariable(dto.getSampleRoleVariable())
      .build();
  }

  public static Plugins.VCFSamplesMappingDto asDto(VCFSamplesMapping sampleMappings) {
    return Plugins.VCFSamplesMappingDto.newBuilder()
      .setProjectName(sampleMappings.getProjectName())
      .setTableReference(sampleMappings.getTableReference())
      .setParticipantIdVariable(sampleMappings.getParticipantIdVariable())
      .setSampleIdVariable(sampleMappings.getSampleIdVariable())
      .setSampleRoleVariable(sampleMappings.getSampleRoleVariable())
      .build();
  }
}
