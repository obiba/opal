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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Nullable;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.MagmaRuntimeException;
import org.obiba.magma.datasource.crypt.DatasourceEncryptionStrategy;
import org.obiba.magma.datasource.crypt.EncryptedSecretKeyDatasourceEncryptionStrategy;
import org.obiba.magma.support.DatasourceParsingException;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.magma.MergingDatasourceFactory;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.web.magma.DatasourceResource;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.magma.support.DatasourceFactoryRegistry;
import org.obiba.opal.web.magma.support.PluginDatasourceFactoryDtoParser;
import org.obiba.opal.web.model.Magma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.io.Serializable;
import java.util.Arrays;


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

  private PluginDatasourceFactoryDtoParser pluginDatasourceFactoryDtoParser;

  private final Cache<String, String> logCache = CacheBuilder.newBuilder().build();

  @Autowired
  public ProjectTransientDatasourcesResource(PluginDatasourceFactoryDtoParser pluginDatasourceFactoryDtoParser) {
    this.pluginDatasourceFactoryDtoParser = pluginDatasourceFactoryDtoParser;
  }

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

  /**
   * Make a datasource factory from the provided factory DTO and optionally merge the variable properties and attributes.
   *
   * @param factoryDto
   * @param merge
   * @return
   */
  @POST
  public Response createDatasource(Magma.DatasourceFactoryDto factoryDto, @QueryParam("merge") @DefaultValue("false") boolean merge) {
    String uid = null;
    try {
      safeRemoveParseErrorLogCache();

      DatasourceFactory factory = pluginDatasourceFactoryDtoParser.canParse(factoryDto) ? pluginDatasourceFactoryDtoParser.parse(factoryDto, getDatasourceEncryptionStrategy()) : datasourceFactoryRegistry.parse(factoryDto, getDatasourceEncryptionStrategy());
      if (merge) {
        factory = new MergingDatasourceFactory(factory, MagmaEngine.get().getDatasource(name));
      }
      uid = MagmaEngine.get().addTransientDatasource(factory);
      Datasource ds = MagmaEngine.get().getTransientDatasourceInstance(uid);
      return Response.created(UriBuilder.fromPath("/").path(DatasourceResource.class).build(uid))
          .entity(Dtos.asDto(ds).build()).build();
    } catch (MagmaRuntimeException e) {
      MagmaEngine.get().removeTransientDatasource(uid);

      if (e instanceof DatasourceParsingException) {
        safeCacheParseErrorLog((DatasourceParsingException) e);
      }

      throw e;
    }
  }

  private void safeCacheParseErrorLog(DatasourceParsingException parseException) {
    try {
      cacheDatarsourceParseErrorLog(parseException);
    } catch (Exception e) {
      log.warn("Error caching error log cache.", e);
    }
  }

  private void safeRemoveParseErrorLogCache() {
    try {
      Serializable sessionId = getSessionId();
      if (sessionId != null) logCache.invalidate(sessionId.toString());
    } catch (Exception e) {
      log.warn("Error removing parse error log cache.", e);
    }
  }

  private void cacheDatarsourceParseErrorLog(DatasourceParsingException de) {
    StringBuilder log = new StringBuilder();

    for (DatasourceParsingException c : de.getChildrenAsList()) {
      Object[] args = c.getParameters().toArray();

      if (args.length > 2) log.append(String.format("[%s: %s] ", Arrays.copyOfRange(args, 0, 2)));

      log.append(c.getMessage());

      if (args.length == 5)
        log.append(String.format("(table: %s, variable: %s, category: %s)", Arrays.copyOfRange(args, 2, args.length)));
      else if (args.length == 4)
        log.append(String.format("(table: %s, variable: %s)", Arrays.copyOfRange(args, 2, args.length)));

      log.append('\n');
    }

    Serializable sessionId = getSessionId();
    if (sessionId != null) logCache.put(sessionId.toString(), log.toString());
  }

  private Serializable getSessionId() {
    try {
      return SecurityUtils.getSubject().getSession().getId();
    } catch (Exception e) {
      log.error("Error getting subject session.", e);
      return null;
    }
  }

  @GET
  @Path("/_last-errors")
  public Response getDatasourceParseErrorLog() {
    Serializable sessionId = getSessionId();
    String cached = null;

    if (sessionId != null) cached = logCache.getIfPresent(sessionId.toString());

    if (cached == null) throw new NotFoundException();

    Response.ResponseBuilder builder = Response.ok(cached, "text/plain");
    builder.header("Content-Disposition", "attachment; filename=\"opal-datasource-parse-error-log.txt\"");

    return builder.build();
  }

  @Nullable
  private DatasourceEncryptionStrategy getDatasourceEncryptionStrategy() {
    Project project = projectService.getProject(name);
    OpalKeyStore keyStore = projectsKeyStoreService.getKeyStore(project);
    DatasourceEncryptionStrategy encryptionStrategy = null;
    if (!keyStore.listKeyPairs().isEmpty()) {
      encryptionStrategy = new EncryptedSecretKeyDatasourceEncryptionStrategy();
      encryptionStrategy.setKeyProvider(keyStore);
    }
    return encryptionStrategy;
  }

}
