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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.project.NoSuchProjectException;
import org.obiba.opal.project.cfg.ProjectsConfigurationService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource extends AbstractProjectResource {

  private final ProjectsConfigurationService projectsConfigurationService;

  @Autowired
  public ProjectResource(ProjectsConfigurationService projectsConfigurationService) {
    this.projectsConfigurationService = projectsConfigurationService;
  }

  @PathParam("name")
  private String name;

  @GET
  public Projects.ProjectDto get() {
    if (MagmaEngine.get().hasDatasource(name)) {
      Datasource ds = MagmaEngine.get().getDatasource(name);
      return Dtos.asDto(getProject(ds), ds).build();
    } else {
      throw new NoSuchProjectException(name);
    }
  }

  @Override
  protected ProjectsConfigurationService getProjectsConfigurationService() {
    return projectsConfigurationService;
  }

}
