/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import com.google.common.collect.Sets;
import org.obiba.magma.*;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Projects;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import java.util.Set;

import static org.obiba.opal.web.model.Projects.ProjectDto;

public class Dtos {


  private Dtos() {}

  public static ProjectDto asDto(Project project, @NotNull String directory) {
    ProjectDto.Builder builder = ProjectDto.newBuilder() //
        .setName(project.getName()) //
        .setTitle(project.getTitle()) //
        .setDirectory(directory) //
        .setLink(UriBuilder.fromPath("/").path(ProjectResource.class).build(project.getName()).toString())
        .setArchived(project.isArchived());
    if(project.hasDescription()) builder.setDescription(project.getDescription());
    if(project.hasTags()) builder.addAllTags(project.getTags());
    if(project.hasDatabase()) builder.setDatabase(project.getDatabase());
    if(project.hasVCFStoreService()) builder.setVcfStoreService(project.getVCFStoreService());
    if(project.hasVCFSamplesMapping()) builder.setVcfSamplesMapping(org.obiba.opal.web.plugins.Dtos.fromDto(project.getVCFSamplesMapping()));

    Datasource datasource = project.getDatasource();
    builder.setDatasource(org.obiba.opal.web.magma.Dtos.asDto(datasource)
        .setLink(UriBuilder.fromPath("/").path(DatasourceResource.class).build(project.getName()).toString()));

    builder.setTimestamps(asTimestampsDto(datasource));

    return builder.build();
  }

  public static Project fromDto(ProjectDto projectDto) {
    Project.Builder builder = Project.Builder.create() //
        .name(projectDto.getName()) //
        .title(projectDto.getTitle()) //
        .description(projectDto.getDescription()) //
        .database(projectDto.getDatabase()) //
        .vcfStoreService(projectDto.getVcfStoreService()) //
        .archived(projectDto.getArchived()) //
        .tags(projectDto.getTagsList());


    if (projectDto.hasVcfSamplesMapping()) {
      builder.vcfSamplesMapping(org.obiba.opal.web.plugins.Dtos.fromDto(projectDto.getVcfSamplesMapping()));
    }

    return builder.build();
  }

  public static Project fromDto(Projects.ProjectFactoryDto projectFactoryDto) {
    Project.Builder builder = Project.Builder.create() //
        .name(projectFactoryDto.getName()) //
        .title(projectFactoryDto.getTitle()) //
        .description(projectFactoryDto.getDescription()) //
        .database(projectFactoryDto.getDatabase()) //
        .vcfStoreService(projectFactoryDto.getVcfStoreService()) //
        .tags(projectFactoryDto.getTagsList());

    if (projectFactoryDto.hasVcfSamplesMapping()) {
      builder.vcfSamplesMapping(org.obiba.opal.web.plugins.Dtos.fromDto(projectFactoryDto.getVcfSamplesMapping()));
    }

    return builder.build();
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static Projects.ProjectSummaryDto asSummaryDto(Project project) {
    Projects.ProjectSummaryDto.Builder builder = Projects.ProjectSummaryDto.newBuilder();
    builder.setName(project.getName());

    // TODO get counts from elasticsearch
    int tableCount = 0;
    int variablesCount = 0;
    Set<String> ids = Sets.newHashSet();
    for(ValueTable table : project.getDatasource().getValueTables()) {
      tableCount++;
      variablesCount = variablesCount + table.getVariableCount();
      for(VariableEntity entity : table.getVariableEntities()) {
        ids.add(entity.getType() + ":" + entity.getIdentifier());
      }
    }
    builder.setTableCount(tableCount);
    builder.setVariableCount(variablesCount);
    builder.setEntityCount(ids.size());

    builder.setTimestamps(asTimestampsDto(project.getDatasource()));

    return builder.build();
  }

  private static Magma.TimestampsDto asTimestampsDto(Timestamped timestamped) {
    Timestamps ts = timestamped.getTimestamps();
    Magma.TimestampsDto.Builder builder = Magma.TimestampsDto.newBuilder();
    Value created = ts.getCreated();
    if(!created.isNull()) builder.setCreated(created.toString());
    Value lastUpdate = ts.getLastUpdate();
    if(!lastUpdate.isNull()) builder.setLastUpdate(lastUpdate.toString());
    return builder.build();
  }
}
