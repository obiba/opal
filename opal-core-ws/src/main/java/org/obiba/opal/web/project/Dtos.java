/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import com.google.common.base.Strings;
import org.obiba.magma.*;
import org.obiba.magma.Timestamped;
import org.obiba.magma.datasource.nil.NullDatasource;
import org.obiba.opal.core.domain.*;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.spi.analysis.AnalysisResultItem;
import org.obiba.opal.spi.resource.Resource;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.model.Projects.AnalysisResultItemDto;
import org.obiba.opal.web.model.Projects.AnalysisStatusDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto.Builder;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.stream.Collectors;

import static org.obiba.opal.web.model.Projects.ProjectDto;

public class Dtos {

  private static final Logger log = LoggerFactory.getLogger(Dtos.class);

  private Dtos() {
  }

  public static ProjectDto asDto(Project project, ProjectService projectService) {
    ProjectDto.Builder builder = ProjectDto.newBuilder() //
        .setName(project.getName()) //
        .setTitle(project.getTitle()) //
        .setDirectory(projectService.getProjectDirectoryPath(project)) //
        .setLink(UriBuilder.fromPath("/").path(ProjectResource.class).build(project.getName()).toString())
        .setArchived(project.isArchived());
    if (project.hasDescription()) builder.setDescription(project.getDescription());
    if (project.hasTags()) builder.addAllTags(project.getTags());
    if (project.hasDatabase()) {
      builder.setDatabase(project.getDatabase());
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.valueOf(projectService.getProjectState(project)));
    } else {
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.NONE);
    }
    if (project.hasVCFStoreService()) builder.setVcfStoreService(project.getVCFStoreService());
    if (project.hasExportFolder()) builder.setExportFolder(project.getExportFolder());
    if (project.hasIdentifiersMappings()) {
      builder.addAllIdMappings(
          project.getIdentifiersMappings().stream()
              .map(mapping -> asDto(mapping))
              .collect(Collectors.toList())
      );
    }

    Datasource datasource = project.getDatasource();
    Magma.DatasourceDto.Builder dsDtoBuilder;
    try {
      dsDtoBuilder = org.obiba.opal.web.magma.Dtos.asDto(datasource);
    } catch (Exception e) {
      log.error("Error when accessing project's datasource: {}", project.getName(), e);
      datasource = new NullDatasource(project.getName());
      dsDtoBuilder = org.obiba.opal.web.magma.Dtos.asDto(datasource);
    }
    builder.setDatasource(dsDtoBuilder.setLink(UriBuilder.fromPath("/").path(DatasourceResource.class).build(project.getName()).toString()));
    builder.setTimestamps(asTimestampsDto(project, datasource));

    return builder.build();
  }

  public static Projects.ProjectDto asDtoDigest(Project project, ProjectService projectService) {
    Projects.ProjectDto.Builder builder = Projects.ProjectDto.newBuilder()
        .setName(project.getName())
        .setTitle(project.getTitle());
    if (project.hasDescription()) builder.setDescription(project.getDescription());
    if (project.hasTags()) builder.addAllTags(project.getTags());
    builder.setTimestamps(asTimestampsDto(project));
    if (project.hasDatabase()) {
      builder.setDatabase(project.getDatabase());
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.valueOf(projectService.getProjectState(project)));
    } else {
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.NONE);
    }

    return builder.build();
  }

  public static Project fromDto(ProjectDto projectDto) {
    Project.Builder builder = Project.Builder.create() //
        .name(projectDto.getName()) //
        .title(projectDto.getTitle()) //
        .description(projectDto.getDescription()) //
        .database(projectDto.getDatabase()) //
        .vcfStoreService(projectDto.getVcfStoreService()) //
        .exportFolder(projectDto.getExportFolder()) //
        .archived(projectDto.getArchived()) //
        .tags(projectDto.getTagsList());

    if (projectDto.getIdMappingsCount() > 0) {
      projectDto.getIdMappingsList()
          .forEach(dto ->
              builder.idMapping(
                  ProjectIdentifiersMapping.newBuilder()
                      .entityType(dto.getEntityType())
                      .name(dto.getName())
                      .mapping(dto.getMapping())
                      .build()
              )
          );
    }

    return builder.build();
  }

  public static Project fromDto(Projects.ProjectFactoryDto projectFactoryDto) {
    return Project.Builder.create() //
        .name(projectFactoryDto.getName()) //
        .title(projectFactoryDto.getTitle()) //
        .description(projectFactoryDto.getDescription()) //
        .database(projectFactoryDto.getDatabase()) //
        .vcfStoreService(projectFactoryDto.getVcfStoreService()) //
        .exportFolder(projectFactoryDto.getExportFolder()) //
        .tags(projectFactoryDto.getTagsList()) //
        .build();
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static Projects.ProjectSummaryDto asSummaryDto(Project project, ProjectService projectService) {
    Projects.ProjectSummaryDto.Builder builder = Projects.ProjectSummaryDto.newBuilder();
    builder.setName(project.getName());

    // TODO get counts from elasticsearch
    int tableCount = 0;
    int viewCount = 0;
    int variableCount = 0;
    int derivedVariableCount = 0;
    if (!"LOADING".equals(projectService.getProjectState(project))) {
      for (ValueTable table : project.getDatasource().getValueTables()) {
        tableCount++;
        int tableVariableCount = table.getVariableCount();
        variableCount = variableCount + tableVariableCount;
        if (table.isView()) {
          viewCount++;
          derivedVariableCount = derivedVariableCount + tableVariableCount;
        }
      }
    }
    builder.setTableCount(tableCount);
    builder.setViewCount(viewCount);
    builder.setVariableCount(variableCount);
    builder.setDerivedVariableCount(derivedVariableCount);
    builder.setEntityCount(-1);
    builder.setResourceCount(projectService.getResourceReferences(project).size());
    if (project.hasDatabase()) {
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.valueOf(projectService.getProjectState(project)));
    } else {
      builder.setDatasourceStatus(Projects.ProjectDatasourceStatusDto.NONE);
    }

    builder.setTimestamps(asTimestampsDto(project, project.getDatasource()));

    return builder.build();
  }

  public static Projects.ResourceReferenceDto asDto(ResourceReference resourceReference, Resource resource, boolean isEditable) {
    Projects.ResourceReferenceDto.Builder builder = Projects.ResourceReferenceDto.newBuilder()
        .setName(resourceReference.getName())
        .setProject(resourceReference.getProject())
        .setProvider(resourceReference.getProvider())
        .setFactory(resourceReference.getFactory())
        .setParameters(resourceReference.getParametersModel())
        .setCreated(Instant.ofEpochMilli(resourceReference.getCreated().getTime()).toString())
        .setUpdated(Instant.ofEpochMilli(resourceReference.getUpdated().getTime()).toString())
        .setEditable(isEditable);

    if (!Strings.isNullOrEmpty(resourceReference.getDescription()))
      builder.setDescription(resourceReference.getDescription());

    if (isEditable)
      builder.setCredentials(resourceReference.getCredentialsModel());

    if (resource != null) {
      try {
        Projects.ResourceSummaryDto.Builder rbuilder = Projects.ResourceSummaryDto.newBuilder()
            .setName(resource.getName())
            .setUrl(resource.toURI().toString());
        if (!Strings.isNullOrEmpty(resource.getFormat()))
          rbuilder.setFormat(resource.getFormat());
        builder.setResource(rbuilder);
      } catch (URISyntaxException e) {
        log.error("Cannot get resource url", e);
      }
    }
    return builder.build();
  }

  public static ResourceReference fromDto(Projects.ResourceReferenceDto referenceDto) {
    ResourceReference reference = new ResourceReference();
    reference.setName(referenceDto.getName());
    if (referenceDto.hasDescription())
      reference.setDescription(referenceDto.getDescription());
    reference.setProject(referenceDto.getProject());
    reference.setProvider(referenceDto.getProvider());
    reference.setFactory(referenceDto.getFactory());
    reference.setParametersModel(referenceDto.getParameters());
    reference.setCredentialsModel(referenceDto.getCredentials());
    return reference;
  }

  public static OpalAnalysisDto.Builder asDto(OpalAnalysis analysis) {
    Builder builder = OpalAnalysisDto.newBuilder();

    builder.setName(analysis.getName());

    builder.setDatasource(analysis.getDatasource());
    builder.setTable(analysis.getTable());

    builder.setPluginName(analysis.getPluginName());
    builder.setTemplateName(analysis.getTemplateName());
    builder.setParameters(analysis.getParameters().toString());
    builder.setCreated(Instant.ofEpochMilli(analysis.getCreated().getTime()).toString());
    builder.setUpdated(Instant.ofEpochMilli(analysis.getUpdated().getTime()).toString());
    builder.addAllVariables(analysis.getVariables());

    return builder;
  }

  public static OpalAnalysisResultDto.Builder asDto(OpalAnalysisResult analysisResult) {
    OpalAnalysisResultDto.Builder builder = OpalAnalysisResultDto.newBuilder();

    builder.setId(analysisResult.getId());
    builder.setAnalysisName(analysisResult.getAnalysisName());

    builder.setStartDate(Instant.ofEpochMilli(analysisResult.getStartDate().getTime()).toString()); //
    builder.setEndDate(Instant.ofEpochMilli(analysisResult.getEndDate().getTime()).toString()); //

    if (analysisResult.hasResultItems()) {
      analysisResult.getResultItems().forEach(item -> builder.addResultItems(asDto((AnalysisResultItem) item)));
    }

    builder.setStatus(AnalysisStatusDto.valueOf(analysisResult.getStatus().name()));
    builder.setMessage(analysisResult.getMessage());

    builder.setCreated(analysisResult.getCreated().toString());
    builder.setUpdated(analysisResult.getUpdated().toString());

    return builder;
  }

  public static ProjectDto.IdentifiersMappingDto asDto(ProjectIdentifiersMapping mapping) {
    return ProjectDto.IdentifiersMappingDto.newBuilder()
        .setEntityType(mapping.getEntityType())
        .setName(mapping.getName())
        .setMapping(mapping.getMapping())
        .build();
  }

  private static AnalysisResultItemDto asDto(AnalysisResultItem item) {
    AnalysisResultItemDto.Builder builder = AnalysisResultItemDto.newBuilder();

    builder.setStatus(AnalysisStatusDto.valueOf(item.getStatus().name()));
    builder.setMessage(item.getMessage());

    return builder.build();
  }

  /**
   * Get the best representation of project's timestamps.
   *
   * @param project
   * @param datasource
   * @return
   */
  private static Magma.TimestampsDto asTimestampsDto(Project project, Datasource datasource) {
    if (datasource.getValueTables().size() == 0)
      return asTimestampsDto(project);
    else
      return asTimestampsDto(datasource);
  }

  private static Magma.TimestampsDto asTimestampsDto(Timestamped timestamped) {
    Timestamps ts = timestamped.getTimestamps();
    Magma.TimestampsDto.Builder builder = Magma.TimestampsDto.newBuilder();
    Value created = ts.getCreated();
    if (!created.isNull()) builder.setCreated(created.toString());
    Value lastUpdate = ts.getLastUpdate();
    if (!lastUpdate.isNull()) builder.setLastUpdate(lastUpdate.toString());
    return builder.build();
  }
}
