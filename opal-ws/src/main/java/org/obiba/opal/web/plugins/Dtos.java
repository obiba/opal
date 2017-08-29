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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.domain.plugins.PluginPackage;
import org.obiba.opal.core.runtime.Plugin;
import org.obiba.opal.core.support.vcf.VCFSamplesSummaryBuilder;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.web.model.Plugins;

import java.util.*;
import java.util.stream.Collectors;

public class Dtos {

  private static List<String> reservedProperties = Lists.newArrayList("OPAL_HOME", "install.dir", "data.dir", "work.dir");

  public static Plugins.PluginPackagesDto asDto(String site, Date updated, boolean restart, List<PluginPackage> packages) {
    return asDto(site, updated, restart, packages, null);
  }

  public static Plugins.PluginPackagesDto asDto(String site, Date updated, boolean restart, List<PluginPackage> packages, Collection<String> uninstalledNames) {
    return Plugins.PluginPackagesDto.newBuilder()
        .setSite(site)
        .setUpdated(DateTimeType.get().valueOf(updated).toString())
        .setRestart(restart)
        .addAllPackages(packages.stream().map(p -> asDto(p, uninstalledNames == null ? null : uninstalledNames.contains(p.getName())))
            .collect(Collectors.toList()))
        .build();
  }

  public static Plugins.PluginPackageDto asDto(PluginPackage pluginPackage, Boolean uninstalled) {
    Plugins.PluginPackageDto.Builder buider = Plugins.PluginPackageDto.newBuilder()
        .setName(pluginPackage.getName())
        .setType(pluginPackage.getType())
        .setTitle(pluginPackage.getTitle())
        .setDescription(pluginPackage.getDescription())
        .setVersion(pluginPackage.getVersion().toString())
        .setOpalVersion(pluginPackage.getOpalVersion().toString());
    if (!Strings.isNullOrEmpty(pluginPackage.getFileName()))
      buider.setFile(pluginPackage.getFileName());
    if (uninstalled != null) buider.setUninstalled(uninstalled);
    return buider.build();
  }

  public static Plugins.PluginDto asDto(Plugin plugin) {
    Plugins.PluginDto.Builder builder = Plugins.PluginDto.newBuilder();
    setProperties(plugin.getProperties(), builder, false);
    return builder.build();
  }

  public static Plugins.PluginDto asDto(ServicePlugin plugin) {
    Plugins.PluginDto.Builder builder = Plugins.PluginDto.newBuilder();
    setProperties(plugin.getProperties(), builder, false);
    return builder.build();
  }

  public static Properties fromDto(Plugins.PluginCfgDto configDto) {
    Properties configMap = new Properties();
    for (Plugins.PropertyDto property : configDto.getPropertiesList()) {
      configMap.setProperty(property.getKey(), property.getValue());
    }
    return configMap;
  }

  public static Plugins.VCFStoreDto asDto(VCFStore store, VCFSamplesSummaryBuilder.Stats stats) {
    Plugins.VCFStoreDto.Builder builder = Plugins.VCFStoreDto.newBuilder()
        .setName(store.getName()) //
        .setTotalSamplesCount(stats.getSamplesIdCount()) //
        .addAllVcf(store.getVCFNames());

    if (stats.hasIdentifiedSamples()) builder.setIdentifiedSamplesCount(stats.getIdentifiedSamples());
    if (stats.hasControlSamples()) builder.setControlSamplesCount(stats.getControlSamples());
    if (stats.hasParticipants()) builder.setParticipantsCount(stats.getParticipants());

    return builder.build();
  }

  public static Plugins.VCFSummaryDto asDto(VCFStore.VCFSummary summary, VCFSamplesSummaryBuilder.Stats stats) {
    Plugins.VCFSummaryDto.Builder builder = Plugins.VCFSummaryDto.newBuilder()
        .setName(summary.getName()) //
        .setFormat(summary.getFormat().name()) //
        .setSize(summary.size()) //
        .setTotalSamplesCount(summary.getSampleIds().size()) //
        .setGenotypesCount((long) summary.getVariantsCount() * summary.getSampleIds().size()) //
        .setVariantsCount(summary.getVariantsCount());

    if (stats.hasIdentifiedSamples()) builder.setIdentifiedSamplesCount(stats.getIdentifiedSamples());
    if (stats.hasControlSamples()) builder.setControlSamplesCount(stats.getControlSamples());
    if (stats.hasParticipants()) builder.setParticipantsCount(stats.getParticipants());

    return builder.build();
  }

  public static VCFSamplesMapping fromDto(Plugins.VCFSamplesMappingDto dto) {
    return VCFSamplesMapping.newBuilder()
        .projectName(dto.getProjectName())
        .tableName(dto.getTableReference())
        .participantIdVariable(dto.getParticipantIdVariable())
        .sampleRoleVariable(dto.getSampleRoleVariable())
        .build();
  }

  public static Plugins.VCFSamplesMappingDto asDto(VCFSamplesMapping sampleMappings) {
    Plugins.VCFSamplesMappingDto.Builder builder = Plugins.VCFSamplesMappingDto.newBuilder()
        .setProjectName(sampleMappings.getProjectName());

    if (sampleMappings.hasTableReference()) {
      MagmaEngineTableResolver.valueOf(sampleMappings.getTableReference()).resolveTable();
      builder.setTableReference(sampleMappings.getTableReference())
          .setParticipantIdVariable(sampleMappings.getParticipantIdVariable())
          .setSampleRoleVariable(sampleMappings.getSampleRoleVariable());
    }

    return builder.build();
  }

  private static void setProperties(Properties properties, Plugins.PluginDto.Builder builder, boolean isPublic) {
    Plugins.PluginCfgDto.Builder cfgBuilder = Plugins.PluginCfgDto.newBuilder();
    properties.entrySet().stream().filter(entry -> !reservedProperties.contains(entry.getKey().toString())).forEach(entry -> {
      if ("name".equals(entry.getKey())) builder.setName(entry.getValue().toString());
      else if ("title".equals(entry.getKey())) builder.setTitle(entry.getValue().toString());
      else if ("description".equals(entry.getKey())) builder.setDescription(entry.getValue().toString());
      else if ("version".equals(entry.getKey())) builder.setVersion(entry.getValue().toString());
      else if ("opal.version".equals(entry.getKey())) builder.setOpalVersion(entry.getValue().toString());
      else if ("type".equals(entry.getKey())) builder.setType(entry.getValue().toString());
      else if (!isPublic)
        cfgBuilder.addProperties(Plugins.PropertyDto.newBuilder().setKey(entry.getKey().toString()).setValue(entry.getValue().toString()));
    });
    builder.setConfig(cfgBuilder);
  }
}
