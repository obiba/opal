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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@Component
@Path("/projects")
public class ProjectsResource {

  @Autowired
  private ProjectService projectService;

  @GET
  @Transactional(readOnly = true)
  @NoAuthorization
  public List<Projects.ProjectDto> getProjects() {
    List<Projects.ProjectDto> projects = Lists.newArrayList();
    for(Project project : projectService.getProjects()) {
      try {
        projects.add(Dtos.asDto(project, projectService.getProjectDirectoryPath(project)));
      } catch(NoSuchDatasourceException e) {
        // ignore
      }
    }
    return projects;
  }

  @POST
  public Response createProject(@Context UriInfo uriInfo, Projects.ProjectFactoryDto projectFactoryDto) {
    Project project = Dtos.fromDto(projectFactoryDto);
    // verify project does not exists
    if(projectService.hasProject(project.getName())) throw new IllegalArgumentException("Project already exists");
    
    projectService.save(project);
    URI projectUri = uriInfo.getBaseUriBuilder().path("project").path(project.getName()).build();
    return Response.created(projectUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(projectUri, Opal.AclAction.PROJECT_ALL))
        .build();
  }

}
