/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.Timestamps;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.type.DateTimeType;
import org.obiba.opal.project.domain.Project;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Magma;
import org.obiba.opal.web.model.Projects;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class Dtos {

  public static Projects.ProjectDto.Builder asDto(Project project, Datasource datasource, String directory,
      boolean withCounts) {
    Projects.ProjectDto.Builder builder = Projects.ProjectDto.newBuilder() //
        .setName(project.getName());

    builder.setTitle(project.hasTitle() ? project.getTitle() : project.getName());

    if(project.hasDescription()) {
      builder.setDescription(project.getDescription());
    }

    if(project.hasTags()) {
      builder.addAllTags(project.getTags());
    }

    builder.setArchived(project.isArchived());

    if(directory != null) {
      builder.setDirectory(directory);
    }

    builder.setLink(UriBuilder.fromPath("/").path(ProjectResource.class).build(project.getName()).toString());
    builder.setDatasource(org.obiba.opal.web.magma.Dtos.asDto(datasource)
        .setLink(UriBuilder.fromPath("/").path(DatasourceResource.class).build(project.getName()).toString()));

    if(withCounts) {
      addCounts(builder, datasource);
    }

    addTimestamps(builder, datasource);

    return builder;
  }

  public static Project fromDto(Projects.ProjectDto projectDto) {
    Project.Builder builder = Project.Builder.create(projectDto.getName()) //
        .title(projectDto.getTitle()) //
        .archived(projectDto.getArchived()) //
        .tags(projectDto.getTagsList());

    if(projectDto.hasDescription()) {
      builder.description(projectDto.getDescription());
    }

    return builder.build();
  }
  private static void addCounts(Projects.ProjectDto.Builder builder, Datasource datasource) {
    // TODO get counts from elasticsearch
    int tableCount = 0;
    int variablesCount = 0;
    Set<String> ids = Sets.newHashSet();
    for(ValueTable table : datasource.getValueTables()) {
      tableCount++;
      variablesCount = variablesCount + (Iterables.size(table.getVariables()));
      for (VariableEntity entity : table.getVariableEntities()) {
        ids.add(entity.getType() + ":" + entity.getIdentifier());
      }
    }
    builder.setTableCount(tableCount);
    builder.setVariableCount(variablesCount);
    builder.setEntityCount(ids.size());
  }

  private static void addTimestamps(Projects.ProjectDto.Builder builder, Datasource datasource) {
    Value created = DateTimeType.get().now();
    Value lastUpdate = null;
    for(ValueTable table : datasource.getValueTables()) {
      Timestamps ts = table.getTimestamps();
      if(created.compareTo(ts.getCreated()) > 0) {
        created = ts.getCreated();
      }
      if(lastUpdate == null || lastUpdate.compareTo(ts.getLastUpdate()) < 0) {
        lastUpdate = ts.getLastUpdate();
      }
    }
    if(lastUpdate == null) {
      lastUpdate = created;
    }

    Magma.TimestampsDto.Builder tsBuilder = Magma.TimestampsDto.newBuilder();
    tsBuilder.setCreated(created.toString()).setLastUpdate(lastUpdate.toString());
    builder.setTimestamps(tsBuilder);
  }
}
