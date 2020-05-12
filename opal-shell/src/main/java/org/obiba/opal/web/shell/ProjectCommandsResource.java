/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.NoSuchDatasourceException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.security.SecuredDatasource;
import org.obiba.magma.support.MagmaEngineReferenceResolver;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.opal.core.domain.ProjectsState;
import org.obiba.opal.core.domain.ProjectsState.State;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.*;
import org.obiba.opal.shell.web.*;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.ReloadDatasourceCommandOptionsDto;
import org.obiba.opal.web.support.ConflictingRequestException;
import org.obiba.opal.web.support.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
@Scope("request")
@Path("/project/{name}/commands")
@SuppressWarnings("OverlyCoupledClass")
public class ProjectCommandsResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(ProjectCommandsResource.class);

  @PathParam("name")
  protected String name;

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @Autowired
  private ProjectsState projectsState;

  @GET
  public List<Commands.CommandStateDto> getCommands() {
    List<Commands.CommandStateDto> commandDtoList = new ArrayList<>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      if(commandJob.hasProject() && commandJob.getProject().equals(name)) {
        commandDtoList.add(Dtos.asDto(commandJob));
      }
    }

    return commandDtoList;
  }

  //
  // Phenotype data
  //

  @POST
  @Path("/_import")
  public Response importData(Commands.ImportCommandOptionsDto options) {
    if (checkCommandIsBlocked(name, false)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    if(!name.equals(options.getDestination())) {
      throw new InvalidRequestException("DataCanOnlyBeImportedInCurrentDatasource", name);
    }

    // check import does not override an existing but not visible table
    if (options.getTablesCount()>0) {
      ensureTableWriteAccess(options.getDestination(),
          options.getTablesList().stream().map(tablePath -> MagmaEngineTableResolver.valueOf(tablePath).getTableName()).collect(Collectors.toList()));
    }
    if (options.hasSource()) {
      Datasource ds = getDatasourceOrTransientDatasource(options.getSource());
      ensureTableWriteAccess(options.getDestination(),
          ds.getValueTables().stream().map(ValueTable::getName).collect(Collectors.toList()));
    }


    ImportCommandOptions importOptions = new ImportCommandOptionsDtoImpl(options);
    Command<ImportCommandOptions> importCommand = commandRegistry.newCommand("import");
    importCommand.setOptions(importOptions);

    return launchCommand(importCommand);
  }

  private Datasource getDatasourceOrTransientDatasource(String datasourceName) throws NoSuchDatasourceException {
    return MagmaEngine.get().hasDatasource(datasourceName)
        ? MagmaEngine.get().getDatasource(datasourceName)
        : MagmaEngine.get().getTransientDatasourceInstance(datasourceName);
  }

  @POST
  @Path("/_copy")
  public Response copyData(Commands.CopyCommandOptionsDto options) {
    if (checkCommandIsBlocked(name, false)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    String commandName = "copy";

    for(String table : options.getTablesList()) {
      ensureTableValuesAccess(table);
    }

    if (options.hasDestinationTableName())
      ensureTableWriteAccess(options.getDestination(), options.getDestinationTableName());
    if (options.getTablesCount()>0)
      ensureTableWriteAccess(options.getDestination(), options.getTablesList());

    CopyCommandOptions copyOptions = new CopyCommandOptionsDtoImpl(options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand(commandName);
    copyCommand.setOptions(copyOptions);

    return launchCommand(commandName, copyCommand);
  }

  @POST
  @Path("/_export")
  public Response exportData(Commands.ExportCommandOptionsDto options) {
    if (checkCommandIsBlocked(name, false)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    String commandName = "export";

    for(String table : options.getTablesList()) {
      ensureTableValuesAccess(table);
    }

    if(options.hasDestination()) {
      ensureDatasourceWriteAccess(options.getDestination());
    } else if(options.hasOut() && !options.getOut().trim().startsWith("{")) {
      ensureFileWriteAccess(options.getOut());
    }

    CopyCommandOptions copyOptions = new ExportCommandOptionsDtoImpl(opalRuntime, options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand(commandName);
    copyCommand.setOptions(copyOptions);

    return launchCommand(commandName, copyCommand);
  }

  @POST
  @Path("/_analyse")
  public Response analyse(Commands.AnalyseCommandOptionsDto options) {
    if(!name.equals(options.getProject())) {
      throw new InvalidRequestException("InvalidProjectName", name);
    }

    options.getAnalysesList().forEach(dto -> ensureTableValuesAccess(String.format("%s.%s", name, dto.getTable())));

    String commandName = "analyse";
    AnalyseCommandOptions analyseCommandOptions = new AnalyseCommandOptionsDtoImpl(options);
    Command<AnalyseCommandOptions> analyseCommand = commandRegistry.newCommand(commandName);
    analyseCommand.setOptions(analyseCommandOptions);

    return launchCommand(commandName, analyseCommand);
  }

  //
  // Genotype data
  //

  @POST
  @Path("/_import_vcf")
  public Response importVCF(Commands.ImportVCFCommandOptionsDto optionsDto) {
    ImportVCFCommandOptions options = new ImportVCFCommandOptionsDtoImpl(optionsDto);
    Command<ImportVCFCommandOptions> command = commandRegistry.newCommand("import-vcf");
    command.setOptions(options);
    return launchCommand(command);
  }

  @POST
  @Path("/_export_vcf")
  public Response exportVCF(Commands.ExportVCFCommandOptionsDto optionsDto) {
    ExportVCFCommandOptions options = new ExportVCFCommandOptionsDtoImpl(optionsDto);
    Command<ExportVCFCommandOptions> command = commandRegistry.newCommand("export-vcf");
    command.setOptions(options);
    return launchCommand(command);
  }

  //
  // Report
  //

  @POST
  @Path("/_report")
  public Response createReport(Commands.ReportCommandOptionsDto options) {
    // TODO ensure file access (report template file and report repo)

    ReportCommandOptions reportOptions = new ReportCommandOptionsDtoImpl(options);
    Command<ReportCommandOptions> reportCommand = commandRegistry.newCommand("report");
    reportCommand.setOptions(reportOptions);

    return launchCommand(reportCommand);
  }

  @POST
  @Path("/_reload")
  public Response reloadProject() {
    if (checkCommandIsBlocked(name, true)) throw new ConflictingRequestException("ProjectMomentarilyNotReloadable", name);

    Command<Object> reloadCommand = commandRegistry.newCommand("reload");
    reloadCommand.setOptions(new ReloadDatasourceCommandOptionsDtoImpl(ReloadDatasourceCommandOptionsDto.newBuilder().setProject(name).build()));
    return launchCommand(reloadCommand);
  }

  @Override
  protected CommandJob newCommandJob(String jobName, Command<?> command) {
    CommandJob job = super.newCommandJob(jobName, command);
    job.setProject(name);
    return job;
  }

  private boolean checkCommandIsBlocked(String projectName, boolean forProjectRefresh) {
    String projectState = projectsState.getProjectState(projectName);

    if (forProjectRefresh) {
      return !State.READY.name().equals(projectState);
    } else {
      return State.LOADING.name().equals(projectState);
    }
  }

  private void ensureTableValuesAccess(String table) {
    MagmaEngineReferenceResolver resolver = MagmaEngineTableResolver.valueOf(table);
    if(!SecurityUtils.getSubject().isPermitted("rest:/datasource/" + resolver.getDatasourceName() + "/table/" +
        resolver.getTableName() + "/valueSet:GET:GET/GET")) {
      throw new InvalidRequestException("AccessDeniedToTableValues", table);
    }
  }

  private void ensureTableWriteAccess(String datasource, String table) {
    ensureTableWriteAccess(datasource, Lists.newArrayList(table));
  }

  private void ensureTableWriteAccess(String datasource, List<String> tables) {
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

  private void ensureDatasourceWriteAccess(String datasource) {
    if(!SecurityUtils.getSubject().isPermitted("rest:/project/" + datasource + "/commands/_import:POST")) {
      throw new InvalidRequestException("DataWriteNotAuthorized", datasource);
    }
  }

  private void ensureFileWriteAccess(String path) {
    if(!SecurityUtils.getSubject().isPermitted("rest:/file" + path + ":POST")) {
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
