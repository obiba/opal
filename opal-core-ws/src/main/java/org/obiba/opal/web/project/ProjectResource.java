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

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceUpdateListener;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.NoSuchProjectException;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.KeyStoreResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  @Autowired
  private ProjectService projectService;

  @Autowired
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private Set<DatasourceUpdateListener> datasourceUpdateListeners;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @PathParam("name")
  private String name;

  @Autowired
  private ApplicationContext applicationContext;

  @GET
  @Transactional(readOnly = true)
  public Projects.ProjectDto get(@Context Request request) {
    Project project = getProject();
    Timestamped projectTimestamps = new ProjectTimestamps(project);
    return Dtos.asDto(project, projectService.getProjectDirectoryPath(project));
  }

  @GET
  @Path("/summary")
  @Transactional(readOnly = true)
  public Projects.ProjectSummaryDto getSummary(@Context Request request) {
    Project project = getProject();
    Datasource ds = project.getDatasource();
    return Dtos.asSummaryDto(project);
  }

  @PUT
  public Response update(Projects.ProjectDto projectDto) {
    // will throw a no such project exception
    projectService.getProject(name);
    if(!name.equals(projectDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    projectService.save(Dtos.fromDto(projectDto));
    return Response.ok().build();
  }

  @DELETE
  @Transactional
  public Response delete(@QueryParam("archive") @DefaultValue("false") boolean archive) throws FileSystemException {
    try {
      Datasource ds = getProject().getDatasource();
      projectService.delete(name, archive);
      for(DatasourceUpdateListener listener : datasourceUpdateListeners) {
        listener.onDelete(ds);
      }
    } catch(NoSuchProjectException e) {
      // silently ignore project not found
    }
    return Response.ok().build();
  }

  @Path("/keystore")
  public KeyStoreResource getKeyStoreResource() {
    KeyStoreResource resource = applicationContext.getBean(KeyStoreResource.class);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());
    resource.setKeyStore(keyStore);
    return resource;
  }

  private Project getProject() {
    return projectService.getProject(name);
  }

  private static class ProjectTimestamps implements Timestamped {
    final UnionTimestamps timestamps;

    private ProjectTimestamps(Project project) {
      timestamps = new UnionTimestamps(Arrays.asList(project, project.getDatasource()));
    }

    @Nonnull
    @Override
    public Timestamps getTimestamps() {
      return timestamps;
    }
  }
}
