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

import org.obiba.opal.project.Project;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Opal;

public class Dtos {

  public static Opal.ProjectDto.Builder asDto(Project project) {
    Opal.ProjectDto.Builder builder = Opal.ProjectDto.newBuilder() //
        .setName(project.getName());

    if(project.hasSummary()) {
      builder.setSummary(project.getSummary());
    }

    if(project.hasDescription()) {
      builder.setDescription(project.getDescription());
    }

    builder.setLink(UriBuilder.fromPath("/").path(ProjectResource.class).build(project.getName()).toString());
    builder.setDatasourceLink(UriBuilder.fromPath("/").path(DatasourceResource.class).build(project.getName()).toString());

    return builder;
  }

}
