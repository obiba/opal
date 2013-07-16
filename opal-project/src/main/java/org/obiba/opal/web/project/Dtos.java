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

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.opal.project.Project;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Projects;

public class Dtos {

  public static Projects.ProjectDto.Builder asDto(Project project, Datasource datasource) {
    Projects.ProjectDto.Builder builder = Projects.ProjectDto.newBuilder() //
        .setName(project.getName());

    if(project.hasDescription()) {
      builder.setDescription(project.getDescription());
    }

    if (project.hasTags()) {
      builder.addAllTags(project.getTags());
    }

    builder.setLink(UriBuilder.fromPath("/").path(ProjectResource.class).build(project.getName()).toString());
    builder.setDatasource(org.obiba.opal.web.magma.Dtos.asDto(datasource)
        .setLink(UriBuilder.fromPath("/").path(DatasourceResource.class).build(project.getName()).toString()));

    return builder;
  }

}
