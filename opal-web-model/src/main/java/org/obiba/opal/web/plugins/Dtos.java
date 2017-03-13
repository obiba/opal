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

  public static Plugins.VCFStoreDto asDto(VCFStore store) {
    Plugins.VCFStoreDto.Builder builder = Plugins.VCFStoreDto.newBuilder();
    builder.setName(store.getName()) //
        .setSamplesCount(store.getSampleIds().size()) //
        .addAllVcf(store.getVCFNames());
    return builder.build();
  }

  public static Plugins.VCFSummaryDto asDto(VCFStore.VCFSummary summary, VCFSamplesSummaryBuilder.Stats stats) {
    Plugins.VCFSummaryDto.Builder builder = Plugins.VCFSummaryDto.newBuilder();
    builder.setName(summary.getName()) //
        .setSize(summary.size()) //
        .setSamplesCount(summary.getSampleIds().size()) //
        .setGenotypesCount(summary.getGenotypesCount()) //
        .setVariantsCount(summary.getVariantsCount())
        .setSamplesSummary(Plugins.VCFSamplesSummaryDto.newBuilder()
          .setParticipants(stats.getParticipants())
          .setOrphanSamples(stats.getOrphanSamples())
          .setControlSamples(stats.getControlSamples())
        );
    return builder.build();
  }

  public static VCFSamplesMapping fromDto(Plugins.VCFSamplesMappingDto dto) {
    return VCFSamplesMapping.newBuilder()
      .projectName(dto.getProjectName())
      .tableName(dto.getTableName())
      .participantIdVariable(dto.getParticipantIdVariable())
      .sampleIdVariable(dto.getSampleIdVariable())
      .sampleRoleVariable(dto.getSampleRoleVariable())
      .build();
  }

  public static Plugins.VCFSamplesMappingDto fromDto(VCFSamplesMapping sampleMappings) {
    return Plugins.VCFSamplesMappingDto.newBuilder()
      .setProjectName(sampleMappings.getProjectName())
      .setTableName(sampleMappings.getTableName())
      .setParticipantIdVariable(sampleMappings.getParticipantIdVariable())
      .setSampleIdVariable(sampleMappings.getSampleIdVariable())
      .setSampleRoleVariable(sampleMappings.getSampleRoleVariable())
      .build();
  }

  public static Plugins.VCFSamplesSummaryDto fromDto(VCFSamplesSummaryBuilder.Stats stats) {
    Plugins.VCFSamplesSummaryDto.Builder builder = Plugins.VCFSamplesSummaryDto.newBuilder();
    builder.setParticipants(stats.getParticipants());
    builder.setParticipantsWithGenotype(stats.getParticipantsWithGenotypes());
    builder.setSamples(stats.getSamples());
    builder.setControlSamples(stats.getControlSamples());

    return builder.build();
  }
}
