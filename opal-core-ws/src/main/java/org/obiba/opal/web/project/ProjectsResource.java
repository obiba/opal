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

import com.google.common.collect.Lists;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.security.Authorizer;
import org.obiba.magma.security.shiro.ShiroAuthorizer;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalPermissions;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.web.BaseResource;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.AuthorizationInterceptor;
import org.obiba.opal.web.ws.security.NoAuthorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Path("/projects")
public class ProjectsResource implements BaseResource {

  private final static Authorizer authorizer = new ShiroAuthorizer();

  @Autowired
  private ProjectService projectService;

  @GET
  @NoAuthorization
  public List<Projects.ProjectDto> getProjects(@QueryParam("digest") @DefaultValue("false") boolean digest) {
    List<Projects.ProjectDto> projects = Lists.newArrayList();
    for (Project project : projectService.getProjects()) {
      try {
        if (isReadable(project.getName())) {
          projects.add(
              digest ? Dtos.asDtoDigest(project, projectService) : Dtos.asDto(project, projectService));
        }
      } catch (NoSuchDatasourceException e) {
        // ignore
      }
    }
    return projects;
  }

  @POST
  public Response createProject(@Context UriInfo uriInfo, Projects.ProjectFactoryDto projectFactoryDto) {
    Project project = Dtos.fromDto(projectFactoryDto);
    // verify project does not exists
    if (projectService.hasProject(project.getName())) throw new IllegalArgumentException("Project already exists");

    if (!Pattern.compile("^[\\w _-]+$").matcher(project.getName()).matches())
      throw new IllegalArgumentException("Project nome invalid: only words, blank space, underscore and hyphen characters are valid");

    projectService.save(project);
    URI projectUri = uriInfo.getBaseUriBuilder().path("project").path(project.getName()).build();
    return Response.created(projectUri)
        .header(AuthorizationInterceptor.ALT_PERMISSIONS, new OpalPermissions(projectUri, Opal.AclAction.PROJECT_ALL))
        .build();
  }

  private boolean isReadable(String project) {
    return authorizer.isPermitted("rest:/project/" + project + ":GET");
  }

}
