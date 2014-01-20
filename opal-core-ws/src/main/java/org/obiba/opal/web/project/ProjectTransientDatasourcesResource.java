/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.project;

import javax.annotation.Nullable;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("request")
@Transactional
@Path("/project/{name}/transient-datasources")
public class ProjectTransientDatasourcesResource {

  private ProjectService projectService;

  private ProjectsKeyStoreService projectsKeyStoreService;

  @PathParam("name")
  private String name;

  private DatasourceFactoryRegistry datasourceFactoryRegistry;

  public void setName(String name) {
    this.name = name;
  }

  @Autowired
  public void setProjectService(ProjectService projectService) {
    this.projectService = projectService;
  }

  @Autowired
  public void setProjectsKeyStoreService(ProjectsKeyStoreService projectsKeyStoreService) {
    this.projectsKeyStoreService = projectsKeyStoreService;
  }

  @Autowired
  public void setDatasourceFactoryRegistry(DatasourceFactoryRegistry datasourceFactoryRegistry) {
    this.datasourceFactoryRegistry = datasourceFactoryRegistry;
  }

  @POST
  public Response createDatasource(Magma.DatasourceFactoryDto factoryDto) {
    String uid = null;
    try {
      DatasourceFactory factory = datasourceFactoryRegistry.parse(factoryDto, getDatasourceEncryptionStrategy());
      uid = MagmaEngine.get().addTransientDatasource(factory);
      Datasource ds = MagmaEngine.get().getTransientDatasourceInstance(uid);
      return Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).build(uid))
          .entity(Dtos.asDto(ds).build()).build();
    } catch(MagmaRuntimeException e) {
      MagmaEngine.get().removeTransientDatasource(uid);
      throw e;
    }
  }

  @Nullable
  private DatasourceEncryptionStrategy getDatasourceEncryptionStrategy() {
    Project project = projectService.getProject(name);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(project);
    DatasourceEncryptionStrategy encryptionStrategy = null;
    if(!keyStore.listKeyPairs().isEmpty()) {
      encryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
      encryptionStrategy.setKeyProvider(keyStore);
    }
    return encryptionStrategy;
  }

}
