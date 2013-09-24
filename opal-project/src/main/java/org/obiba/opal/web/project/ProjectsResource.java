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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.DuplicateDatasourceNameException;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.nil.support.NullDatasourceFactory;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.project.domain.Project;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.NoSuchDatasourceFactoryException;
import org.obiba.opal.web.model.Projects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Path("/projects")
public class ProjectsResource {

  private final ProjectService projectService;

  private final OpalConfigurationService configService;

  private final DatasourceFactoryRegistry datasourceFactoryRegistry;

  @Autowired
  public ProjectsResource(DatasourceFactoryRegistry datasourceFactoryRegistry, ProjectService projectService,
      OpalConfigurationService configService) {
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
    this.projectService = projectService;
    this.configService = configService;
  }

  @GET
  public List<Projects.ProjectDto> getProjects() {
    List<Projects.ProjectDto> projects = Lists.newArrayList();

    // one project per datasource
    for(Datasource ds : MagmaEngine.get().getDatasources()) {
      projects.add(
          Dtos.asDto(projectService.getOrCreateProject(ds), ds, projectService.getProjectDirectoryPath(ds.getName()))
              .build());
    }

    return projects;
  }

  @POST
  public Response createProject(@Context UriInfo uriInfo, Projects.ProjectFactoryDto projectFactoryDto) {
    Response.ResponseBuilder response;
    try {
      final DatasourceFactory factory;
      factory = projectFactoryDto.hasFactory()
          ? datasourceFactoryRegistry.parse(projectFactoryDto.getFactory())
          : new NullDatasourceFactory();
      factory.setName(projectFactoryDto.getName());
      Datasource ds = MagmaEngine.get().addDatasource(factory);
      configService.modifyConfiguration(new OpalConfigurationService.ConfigModificationTask() {

        @Override
        public void doWithConfig(OpalConfiguration config) {
          config.getMagmaEngineFactory().withFactory(factory);
        }
      });
      UriBuilder ub = uriInfo.getBaseUriBuilder().path("project").path(ds.getName());
      Project project = projectService.getOrCreateProject(ds);
      if(projectFactoryDto.hasTitle()) project.setTitle(projectFactoryDto.getTitle());
      if(projectFactoryDto.hasDescription()) project.setDescription(projectFactoryDto.getDescription());
      projectService.addOrReplaceProject(project);
      response = Response.created(ub.build()).entity(org.obiba.opal.web.magma.Dtos.asDto(ds).build());
    } catch(NoSuchDatasourceFactoryException noSuchDatasourceFactoryEx) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "UnidentifiedDatasourceFactory").build());
    } catch(DuplicateDatasourceNameException duplicateDsNameEx) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DuplicateDatasourceName").build());
    } catch(DatasourceParsingException dsParsingEx) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", dsParsingEx).build());
    } catch(MagmaRuntimeException dsCreationFailedEx) {
      response = Response.status(BAD_REQUEST)
          .entity(ClientErrorDtos.getErrorMessage(BAD_REQUEST, "DatasourceCreationFailed", dsCreationFailedEx).build());
    }
    return response.build();
  }
}
