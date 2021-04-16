/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.plugins;

import com.google.common.base.Strings;
import org.obiba.magma.ValueType;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.obiba.opal.core.support.vcf.VCFSamplesSummaryBuilder;
import org.obiba.opal.spi.analysis.Analysis;
import org.obiba.opal.spi.analysis.AnalysisResult;
import org.obiba.opal.spi.analysis.AnalysisService;
import org.obiba.opal.spi.analysis.AnalysisTemplate;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.web.model.Plugins;
import org.obiba.opal.web.model.Plugins.AnalysisPluginPackageDto;
import org.obiba.opal.web.model.Plugins.AnalysisPluginTemplateDto;
import org.obiba.opal.web.model.Plugins.AnalysisPluginTemplateDto.Builder;
import org.obiba.plugins.PluginPackage;
import org.obiba.plugins.PluginResources;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Dtos {

  public static Plugins.PluginPackagesDto asDto(String site, Date updated, boolean restart, List<PluginPackage> packages) {
    return asDto(site, updated, restart, packages, null);
  }

  public static Plugins.PluginPackagesDto asDto(String site, Date updated, boolean restart, List<PluginPackage> packages, Collection<String> uninstalledNames) {
    Plugins.PluginPackagesDto.Builder builder = asDto(site, updated, restart);
    builder.addAllPackages(packages.stream().map(p -> asDto(p, uninstalledNames == null ? null : uninstalledNames.contains(p.getName())))
        .collect(Collectors.toList()));
    return builder.build();
  }

  public static Plugins.PluginPackagesDto.Builder asDto(String site, Date updated, boolean restart) {
    Plugins.PluginPackagesDto.Builder builder = Plugins.PluginPackagesDto.newBuilder()
        .setSite(site)
        .setRestart(restart);
    if (updated != null) builder.setUpdated(DateTimeType.get().valueOf(updated).toString());
    return builder;
  }

  public static Plugins.PluginPackageDto asDto(PluginPackage pluginPackage, Boolean uninstalled) {
    Plugins.PluginPackageDto.Builder buider = Plugins.PluginPackageDto.newBuilder()
        .setName(pluginPackage.getName())
        .setType(pluginPackage.getType())
        .setTitle(pluginPackage.getTitle())
        .setDescription(pluginPackage.getDescription())
        .setAuthor(Strings.isNullOrEmpty(pluginPackage.getAuthor()) ? "-" : pluginPackage.getAuthor())
        .setMaintainer(Strings.isNullOrEmpty(pluginPackage.getMaintainer()) ? "-" : pluginPackage.getMaintainer())
        .setLicense(Strings.isNullOrEmpty(pluginPackage.getLicense()) ? "-" : pluginPackage.getLicense())
        .setVersion(pluginPackage.getVersion().toString())
        .setOpalVersion(pluginPackage.getOpalVersion().toString());
    if (!Strings.isNullOrEmpty(pluginPackage.getWebsite()))
      buider.setWebsite(pluginPackage.getWebsite());
    if (!Strings.isNullOrEmpty(pluginPackage.getFileName()))
      buider.setFile(pluginPackage.getFileName());
    if (uninstalled != null) buider.setUninstalled(uninstalled);
    return buider.build();
  }

  public static Plugins.PluginDto asDto(PluginResources plugin) {
    Plugins.PluginDto.Builder builder = Plugins.PluginDto.newBuilder()
        .setName(plugin.getName())
        .setTitle(plugin.getTitle())
        .setDescription(plugin.getDescription())
        .setAuthor(plugin.getAuthor())
        .setMaintainer(plugin.getMaintainer())
        .setLicense(plugin.getLicense())
        .setVersion(plugin.getVersion().toString())
        .setOpalVersion(plugin.getHostVersion().toString())
        .setType(plugin.getType())
        .setSiteProperties(plugin.getSitePropertiesString());
    return builder.build();
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

  public static <T extends Analysis, U extends AnalysisResult> Plugins.AnalysisPluginPackageDto.Builder asDto(AnalysisService<T, U> analysisService) {
    Plugins.AnalysisPluginPackageDto.Builder builder = AnalysisPluginPackageDto.newBuilder();

    builder.addAllAnalysisTemplates(analysisService.getAnalysisTemplates().stream().map(Dtos::asDto).collect(Collectors.toList()));

    return builder;
  }

  public static AnalysisPluginTemplateDto asDto(AnalysisTemplate template) {
    Builder builder = AnalysisPluginTemplateDto.newBuilder()
        .setName(template.getName())
        .setTitle(template.getTitle())
        .setDescription(template.getDescription())
        .setSchemaForm(template.getJSONSchemaForm().toString());

    if (template.getValueTypes() != null) {
      builder.addAllValueTypes(template.getValueTypes().stream().map(ValueType::getName).collect(Collectors.toList()));
    }

    if (template.getReportPath() != null) {
      builder.setReportPath(template.getReportPath().toString());
    }

    if (template.getRoutinePath() != null) {
      builder.setRoutinePath(template.getRoutinePath().toString());
    }

    return builder.build();
  }

}
