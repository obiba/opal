/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import com.google.common.collect.Lists;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.security.SecuredDatasource;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.service.ProjectsState;
import org.obiba.opal.core.service.ProjectsState.State;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

public abstract class AbstractProjectCommandsResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(AbstractProjectCommandsResource.class);

  protected CommandRegistry commandRegistry;

  protected ProjectsState projectsState;

  @Autowired
  @Qualifier("web")
  public void setCommandRegistry(CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
  }

  @Autowired
  public void setProjectsState(ProjectsState projectsState) {
    this.projectsState = projectsState;
  }

  protected abstract String getName();

  @Override
  protected CommandJob newCommandJob(String jobName, Command<?> command) {
    CommandJob job = super.newCommandJob(jobName, command);
    job.setProject(getName());
    return job;
  }

  protected Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    return MagmaEngine.get().hasDatasource(datasourceName)
        ? MagmaEngine.get().getDatasource(datasourceName)
        : MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
  }

  protected boolean checkCommandIsBlocked(String projectName, boolean forProjectRefresh) {
    String projectState = projectsState.getProjectState(projectName);

    if (forProjectRefresh) {
      return !State.READY.name().equals(projectState);
    } else {
      return State.LOADING.name().equals(projectState);
    }
  }

  protected void ensureTableValuesAccess(String table) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(table);
    if(!SecurityUtils.getSubject().isPermitted("rest:/datasource/" + resolver.getDatasourceName() + "/table/" +
        resolver.getTableName() + "/valueSet:GET:GET/GET")) {
      throw new InvalidRequestException("AccessDeniedToTableValues", table);
    }
  }

  protected void ensureTableWriteAccess(String datasource, String table) {
    ensureTableWriteAccess(datasource, Lists.newArrayList(table));
  }

  protected void ensureTableWriteAccess(String datasource, List<String> tables) {
    Datasource ds = MagmaEngine.get().getDatasource(datasource);
    if(ds instanceof SecuredDatasource) {
      // by-pass security otherwise existing but not visible table could confuse
      ds = ((SecuredDatasource) ds).getWrappedDatasource();
    }
    for (String table : tables) {
      if (ds.hasValueTable(table)) {
        // if table exists, check for higher level of permission on table
        if (!SecurityUtils.getSubject().isPermitted("rest:/datasource/" + datasource + "/table/" + table + ":DELETE")) {
          throw new InvalidRequestException("TableWriteNotAuthorized", datasource, table);
        }
      } else {
        // make sure it can be created
        ensureDatasourceWriteAccess(datasource);
      }
    }
  }

  protected void ensureDatasourceWriteAccess(String datasource) {
    if(!SecurityUtils.getSubject().isPermitted("rest:/project/" + datasource + "/commands/_import:POST")) {
      throw new InvalidRequestException("DataWriteNotAuthorized", datasource);
    }
  }

  protected void ensureFileWriteAccess(String path) {
    if(!SecurityUtils.getSubject().isPermitted("rest:/files" + path + ":POST")) {
      throw new InvalidRequestException("FileWriteNotAuthorized", path);
    }
  }

  protected void ensureFileReadAccess(String path) {
    if(!SecurityUtils.getSubject().isPermitted("rest:/files" + path + ":GET")) {
      throw new InvalidRequestException("FileReadNotAuthorized", path);
    }
  }

  @Override
  protected Response buildLaunchCommandResponse(CommandJob commandJob) {
    return Response.created(
        UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(commandJob.getId()))
        .entity(Dtos.asDto(commandJob))
        .build();
  }
}
