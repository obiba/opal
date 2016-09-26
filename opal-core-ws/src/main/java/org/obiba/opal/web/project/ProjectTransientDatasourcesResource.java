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

import java.io.Serializable;
import java.util.Arrays;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("request")
@Transactional
@Path("/project/{name}/transient-datasources")
public class ProjectTransientDatasourcesResource {
  private static final Logger log = LoggerFactory.getLogger(ProjectTransientDatasourcesResource.class);

  private ProjectService projectService;

  private ProjectsKeyStoreService projectsKeyStoreService;

  @PathParam("name")
  private String name;

  @Autowired
  private CacheManager cacheManager;

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

      if (e instanceof DatasourceParsingException) {
        cacheDatarsourceParseErrorLog((DatasourceParsingException) e);
      }

      throw e;
    }
  }

  private void cacheDatarsourceParseErrorLog(DatasourceParsingException de) {
    StringBuilder log = new StringBuilder();

    for(DatasourceParsingException c : de.getChildrenAsList()) {
      Object[] args = c.getParameters().toArray();

      if (args.length > 2)
        log.append(String.format("[%s: %s] ", Arrays.copyOfRange(args, 0, 2)));

      log.append(c.getMessage());

      if (args.length == 5)
        log.append(String.format("(table: %s, variable: %s, category: %s)", Arrays.copyOfRange(args, 2, args.length)));
      else if (args.length == 4)
        log.append(String.format("(table: %s, variable: %s)", Arrays.copyOfRange(args, 2, args.length)));

      log.append('\n');
    }

    Serializable sessionId = getSessionId();
    if(sessionId != null)
      getCache().put(new Element(sessionId, log.toString()));
  }

  private Serializable getSessionId() {
    return SecurityUtils.getSubject().getSession().getId();
  }

  private Cache getCache() {
    return cacheManager.getCache("opal-datasource-parse-error-log");
  }

  @GET
  @Path("/opal-datasource-parse-error-log")
  public Response getDatasourceParseErrorLog() {
    Serializable sessionId = getSessionId();
    Element cached = null;

    if(sessionId != null)
       cached = getCache().get(getSessionId());

    if (cached == null)
      throw new NotFoundException();

    String log = (String)cached.getObjectValue();
    Response.ResponseBuilder builder = Response.ok(log, "text/plain");
    builder.header("Content-Disposition", "attachment; filename=\"opal-datasource-parse-error-log.txt\"");

    return builder.build();
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
