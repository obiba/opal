/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.project;

import com.google.common.eventbus.EventBus;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.magma.Timestamped;
import org.obiba.magma.Timestamps;
import org.obiba.magma.support.UnionTimestamps;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.event.DatasourceDeletedEvent;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.KeyStoreResource;
import org.obiba.opal.web.vcf.VCFStoreResource;
import org.obiba.plugins.spi.ServicePlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Arrays;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  private final OpalRuntime opalRuntime;

  private final ProjectService projectService;

  private final EventBus eventBus;

  private final ProjectsKeyStoreService projectsKeyStoreService;

  private final ApplicationContext applicationContext;

  private final VCFSamplesMappingService vcfSamplesMappingService;

  private final SubjectProfileService subjectProfileService;

  @Autowired
  public ProjectResource(
      OpalRuntime opalRuntime,
      ProjectService projectService,
      EventBus eventBus,
      ProjectsKeyStoreService projectsKeyStoreService,
      ApplicationContext applicationContext,
      VCFSamplesMappingService vcfSamplesMappingService,
      SubjectProfileService subjectProfileService) {
    this.opalRuntime = opalRuntime;
    this.projectService = projectService;
    this.eventBus = eventBus;
    this.projectsKeyStoreService = projectsKeyStoreService;
    this.applicationContext = applicationContext;
    this.vcfSamplesMappingService = vcfSamplesMappingService;
    this.subjectProfileService = subjectProfileService;
  }

  @GET
  @Transactional(readOnly = true)
  public Projects.ProjectDto get(@Context Request request, @PathParam("name") String name) {
    Project project = projectService.getProject(name);
    return Dtos.asDto(project, projectService.getProjectDirectoryPath(project));
  }

  @GET
  @Path("/summary")
  @Transactional(readOnly = true)
  public Projects.ProjectSummaryDto getSummary(@Context Request request, @PathParam("name") String name) {
    Project project = projectService.getProject(name);
    return Dtos.asSummaryDto(project);
  }

  @PUT
  public Response update(Projects.ProjectDto projectDto, @PathParam("name") String name) {
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
  public Response delete(@PathParam("name") String name, @QueryParam("archive") @DefaultValue("false") boolean archive) throws FileSystemException {
    try {
      Project project = projectService.getProject(name);
      Datasource ds = project.hasDatasource() ? project.getDatasource() : null;
      projectService.delete(name, archive);
      if (ds != null) {
        eventBus.post(new DatasourceDeletedEvent(ds));
      }
      if (project.hasVCFStoreService() && opalRuntime.hasServicePlugin(project.getVCFStoreService())) {
        ServicePlugin servicePlugin = opalRuntime.getServicePlugin(project.getVCFStoreService());
        ((VCFStoreService) servicePlugin).deleteStore(project.getName());
      }
      vcfSamplesMappingService.deleteProjectSampleMappings(name + ".%");
      subjectProfileService.deleteBookmarks("/project/" + project.getName());
      subjectProfileService.deleteBookmarks("/datasource/" + project.getName());
    } catch(Exception e) {
      // silently ignore project not found and other errors
    }
    return Response.ok().build();
  }

  @Path("/keystore")
  public KeyStoreResource getKeyStoreResource(@PathParam("name") String name) {
    KeyStoreResource resource = applicationContext.getBean(KeyStoreResource.class);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(projectService.getProject(name));
    resource.setKeyStore(keyStore);
    return resource;
  }

  @Path("/vcf-store")
  public VCFStoreResource getVCFStoreResource(@PathParam("name") String name) {
    Project project = projectService.getProject(name);
    if (!opalRuntime.hasServicePlugins(VCFStoreService.class)) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    if (!project.hasVCFStoreService()) throw new NotFoundException("Project has no VCF store: " + project.getName());
    VCFStoreResource resource = applicationContext.getBean(VCFStoreResource.class);
    resource.setVCFStore(project.getVCFStoreService(), name);
    return resource;
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

  @GET
  @Path("/state")
  public Response getState(@PathParam("name") String name) {
    Project project = projectService.getProject(name);

    boolean isRefreshing = !project.hasDatasource();
    boolean isBusy = isRefreshing;

    ResponseBuilder responseBuilder = Response.ok();

    if (isBusy) {
      responseBuilder.entity(State.BUSY.name());
    } else if (isRefreshing) {
      responseBuilder.entity(State.REFRESHING.name());
    } else {
      responseBuilder.entity(State.READY.name());
    }

    return responseBuilder.build();
  }


  public enum State {
    BUSY, // project has read, write and refresh commands that are pending or being processed
    READY,
    REFRESHING // project's datasource is not ready
  }
}
