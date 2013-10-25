/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.web.CopyCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ImportCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ReportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/project/{name}/commands")
public class ProjectCommandsResource extends AbstractCommandsResource {

  @SuppressWarnings("UnusedDeclaration")
  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsResource.class);

  @PathParam("name")
  protected String name;

  private final CommandRegistry commandRegistry;

  @Autowired
  public ProjectCommandsResource(OpalRuntime opalRuntime, CommandJobService commandJobService,
      @Qualifier("web") CommandRegistry commandRegistry) {
    super(opalRuntime, commandJobService);
    this.commandRegistry = commandRegistry;
  }

  @GET
  public List<Commands.CommandStateDto> getCommands() {
    List<Commands.CommandStateDto> commandDtoList = new ArrayList<Commands.CommandStateDto>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      if(commandJob.hasProject() && commandJob.getProject().equals(name)) {
        commandDtoList.add(Dtos.asDto(commandJob));
      }
    }

    return commandDtoList;
  }

  @POST
  @Path("/_import")
  public Response importData(Commands.ImportCommandOptionsDto options) {
    if(!name.equals(options.getDestination())) {
      throw new InvalidRequestException("DataCanOnlyBeImportedInCurrentDatasource", name);
    }

    // TODO check file access

    ImportCommandOptions importOptions = new ImportCommandOptionsDtoImpl(options);
    Command<ImportCommandOptions> importCommand = commandRegistry.newCommand("import");
    importCommand.setOptions(importOptions);

    return launchCommand(importCommand);
  }

  @POST
  @Path("/_copy")
  public Response copyData(Commands.CopyCommandOptionsDto options) {
    return launchCopyCommand("copy",options);
  }

  @POST
  @Path("/_export")
  public Response exportData(Commands.CopyCommandOptionsDto options) {
    return launchCopyCommand("export",options);
  }

  @POST
  @Path("/_report")
  public Response createReport(Commands.ReportCommandOptionsDto options) {
    // TODO ensure file access (report template file and report repo)

    ReportCommandOptions reportOptions = new ReportCommandOptionsDtoImpl(options);
    Command<ReportCommandOptions> reportCommand = commandRegistry.newCommand("report");
    reportCommand.setOptions(reportOptions);

    return launchCommand(reportCommand);
  }

  @Override
  protected CommandJob newCommandJob(String name, Command<?> command) {
    CommandJob job = super.newCommandJob(name, command);
    job.setProject(this.name);
    return job;
  }

  private Response launchCopyCommand(String commandName, Commands.CopyCommandOptionsDto options) {
    for(String table : options.getTablesList()) {
      ensureTableValuesAccess(table);
    }

    if(options.hasDestination()) {
      ensureDatasourceWriteAccess(options.getDestination());
    } else if(options.hasOut()) {
      ensureFileWriteAccess(options.getOut());
    }

    CopyCommandOptions copyOptions = new CopyCommandOptionsDtoImpl(opalRuntime, options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand(commandName);
    copyCommand.setOptions(copyOptions);

    return launchCommand(commandName, copyCommand);
  }

  private void ensureTableValuesAccess(String table) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(table);
    if(!SecurityUtils.getSubject().isPermitted("magma:/datasource/" + resolver.getDatasourceName() + "/table/" +
        resolver.getTableName() + "/valueSet:GET:GET/GET")) {
      throw new InvalidRequestException("AccessDeniedToTableValues", table);
    }
  }

  private void ensureDatasourceWriteAccess(String datasource) {
    if(!SecurityUtils.getSubject().isPermitted("magma:/datasource/" + datasource + "/commands/_import:POST")) {
      throw new InvalidRequestException("DataWriteNotAuthorized", datasource);
    }
  }

  private void ensureFileWriteAccess(String path) {
    if(!SecurityUtils.getSubject().isPermitted("magma:/file" + path + ":POST")) {
      throw new InvalidRequestException("FileWriteNotAuthorized", path);
    }
  }

  @Override
  protected Response buildLaunchCommandResponse(Integer jobId) {
    return Response.created(
        UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(jobId))
        .build();
  }
}
