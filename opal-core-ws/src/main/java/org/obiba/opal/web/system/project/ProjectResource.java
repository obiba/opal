/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.system.project;

import java.io.IOException;
import java.net.URI;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.security.KeyStoreResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
@Scope("request")
@Path("/project/{name}")
public class ProjectResource {

  @Autowired
  private ProjectService projectService;

  @Autowired
  private Set<DatasourceUpdateListener> datasourceUpdateListeners;

  @Autowired
  private ProjectsKeyStoreService projectsKeyStoreService;

  @PathParam("name")
  private String name;

  ApplicationContext applicationContext;

  @Autowired
  void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @GET
  @Transactional(readOnly = true)
  public Response get(@Context Request request) {
    Project project = getProject();
    Timestamped projectTimestamps = new ProjectTimestamps(project);
    TimestampedResponses.evaluate(request, projectTimestamps);
    return TimestampedResponses
        .ok(projectTimestamps, Dtos.asDto(project, projectService.getProjectDirectoryPath(project))).build();
  }

  @GET
  @Path("/summary")
  @Transactional(readOnly = true)
  public Response getSummary(@Context Request request) {
    Project project = getProject();
    Datasource ds = project.getDatasource();
    TimestampedResponses.evaluate(request, ds);
    return TimestampedResponses.ok(ds, Dtos.asSummaryDto(project)).build();
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
  public Response delete() throws FileSystemException {
    try {
      Datasource ds = getProject().getDatasource();
      projectService.delete(name);
      for(DatasourceUpdateListener listener : datasourceUpdateListeners) {
        listener.onDelete(ds);
      }
    } catch(NoSuchProjectException e) {
      // silently ignore project not found
    }
    return Response.ok().build();
  }

  @GET
  @Path("/keystore")
  public Response getKeyEntries() throws IOException, KeyStoreException {
    KeyStoreResource resource = applicationContext.getBean("keyStoreResource", KeyStoreResource.class);
    Gson gson = new GsonBuilder().create();
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());
    List<Opal.KeyDto> keyEntries = resource.getKeyEntries(keyStore);

    return Response.status(Response.Status.OK).entity(gson.toJson(keyEntries)).build();
  }

  @POST
  @Path("/keystore")
  public Response createKeyEntries(Opal.KeyForm keyForm) {
    KeyStoreResource resource = applicationContext.getBean("keyStoreResource", KeyStoreResource.class);
    URI keyEntryUri = UriBuilder.fromPath("/").path(ProjectResource.class).path("/keystore/" + keyForm.getAlias())
        .build();
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());

    return resource.createKeyEntry(keyStore, keyForm, keyEntryUri);
  }

  @GET
  @Path("/keystore/{alias}")
  public Response getKeyEntry(@PathParam("alias") String alias) throws IOException, KeyStoreException {
    KeyStoreResource resource = applicationContext.getBean("keyStoreResource", KeyStoreResource.class);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());

    return resource.getKeyEntry(keyStore, alias);
  }

  @DELETE
  @Path("/keystore/{alias}")
  public Response deleteKeyEntry(@PathParam("alias") String alias) {
    KeyStoreResource resource = applicationContext.getBean("keyStoreResource", KeyStoreResource.class);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());

    return resource.deleteKeyEntry(keyStore, alias);
  }

  @GET
  @Path("/keystore/{alias}/certificate")
  // TODO: Authenticated by cookies ?
  public Response getCertificate(@PathParam("alias") String alias) throws IOException, KeyStoreException {
    KeyStoreResource resource = applicationContext.getBean("keyStoreResource", KeyStoreResource.class);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(getProject());

    return resource.getCertificate(keyStore, alias);
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
