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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.project.cfg.ProjectsConfigurationService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
@Path("/projects")
public class ProjectsResource extends AbstractProjectResource {

  private final ProjectsConfigurationService projectsConfigurationService;

  @Autowired
  public ProjectsResource(ProjectsConfigurationService projectsConfigurationService) {
    this.projectsConfigurationService = projectsConfigurationService;
  }

  @GET
  public List<Opal.ProjectDto> getProjects() {
    List<Opal.ProjectDto> projects = Lists.newArrayList();

    // one project per datasource
    for(Datasource ds : MagmaEngine.get().getDatasources()) {
      projects.add(Dtos.asDto(getProject(ds)).build());
    }

    return projects;
  }

  @Override
  protected ProjectsConfigurationService getProjectsConfigurationService() {
    return projectsConfigurationService;
  }
}
