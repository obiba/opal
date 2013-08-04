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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.project.cfg.ProjectsConfigurationService;
import org.obiba.opal.project.domain.Project;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  private final ProjectService projectService;

  @Autowired
  public ProjectResource(ProjectService projectService) {
    this.projectService = projectService;
  }

  @PathParam("name")
  private String name;

  @GET
  public Projects.ProjectDto get() {
    if(MagmaEngine.get().hasDatasource(name)) {
      Datasource ds = MagmaEngine.get().getDatasource(name);
      return Dtos.asDto(projectService.getOrCreateProject(ds), ds, projectService.getProjectDirectoryPath(name)).build();
    } else {
      throw new NoSuchProjectException(name);
    }
  }

  @PUT
  public Response update(Projects.ProjectDto projectDto) {
    // will throw a no such project exception
    Project project = projectService.getProject(name);

    if (!name.equals(projectDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    projectService.addOrReplaceProject(Dtos.fromDto(projectDto));

    return Response.ok().build();
  }

  @DELETE
  public Response delete() {
    // silently ignore project not found
    if(projectService.hasProject(name)) {
      projectService.removeProject(name);

    }
    // TODO remove all tables, permissions, folders, index etc.
    if (MagmaEngine.get().hasDatasource(name)) {
      Datasource ds = MagmaEngine.get().getDatasource(name);
      MagmaEngine.get().removeDatasource(ds);
      for (ValueTable table : ds.getValueTables()) {
        if (ds.canDropTable(table.getName())) {
          ds.dropTable(table.getName());
        }
      }
    }
    return Response.ok().build();
  }

}
