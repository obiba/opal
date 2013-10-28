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

import org.apache.commons.vfs2.FileSystemException;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.project.domain.Project;
import org.obiba.opal.web.model.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  @Autowired
  private ProjectService projectService;

  @PathParam("name")
  private String name;

  @GET
  public Projects.ProjectDto get() {
    Project project = getProject();
    return Dtos.asDto(project, projectService.getProjectDirectoryPath(project));
  }

  @GET
  @Path("/summary")
  public Projects.ProjectSummaryDto getSummary() {
    return Dtos.asSummaryDto(getProject());
  }

  @PUT
  public Response update(Projects.ProjectDto projectDto) {
    // will throw a no such project exception
    Project project = projectService.getProject(name);
    if(!name.equals(projectDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    projectService.save(Dtos.fromDto(projectDto));
    return Response.ok().build();
  }

  @DELETE
  public Response delete() throws FileSystemException {
    try {
      projectService.delete(name);
    } catch(NoSuchProjectException e) {
      // silently ignore project not found
    }
    return Response.ok().build();
  }

  private Project getProject() {
    return projectService.getProject(name);
  }
}
