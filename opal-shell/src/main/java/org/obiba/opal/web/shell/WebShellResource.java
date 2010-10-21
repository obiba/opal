/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.shell;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.shell.web.CopyCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ImportCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ReportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Opal Web Shell services.
 */
@Component
@Path("/shell")
public class WebShellResource {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(WebShellResource.class);

  //
  // Instance Variables
  //

  private OpalRuntime opalRuntime;

  private CommandJobService commandJobService;

  private CommandRegistry commandRegistry;

  //
  // Constructors
  //

  @Autowired
  public WebShellResource(OpalRuntime opalRuntime, CommandJobService commandJobService, @Qualifier("web") CommandRegistry commandRegistry) {
    this.opalRuntime = opalRuntime;
    this.commandJobService = commandJobService;
    this.commandRegistry = commandRegistry;
  }

  //
  // Web Service Methods
  //

  @GET
  @Path("/commands")
  public List<CommandStateDto> getCommands() {
    List<CommandStateDto> commandDtoList = new ArrayList<CommandStateDto>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      commandDtoList.add(toCommandStateDto(commandJob));
    }

    return commandDtoList;
  }

  @GET
  @Path("/command/{id}")
  public Response getCommand(@PathParam("id") Integer id) {
    CommandJob commandJob = commandJobService.getCommand(id);

    if(commandJob != null) {
      return Response.ok(toCommandStateDto(commandJob)).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("/command/{id}")
  public Response deleteCommand(@PathParam("id") Integer id) {
    try {
      commandJobService.deleteCommand(id);
      return Response.ok().build();
    } catch(NoSuchCommandJobException ex) {
      return Response.status(Status.NOT_FOUND).entity("DeleteCommand_NotFound").build();
    } catch(IllegalStateException ex) {
      return Response.status(Status.BAD_REQUEST).entity("DeleteCommand_BadRequest_NotDeletable").build();
    }
  }

  @DELETE
  @Path("/commands/completed")
  public Response deleteCompletedCommands() {
    commandJobService.deleteCompletedCommands();
    return Response.ok().build();
  }

  @GET
  @Path("/command/{id}/status")
  public Response getCommandStatus(@PathParam("id") Integer id) {
    CommandJob commandJob = commandJobService.getCommand(id);

    if(commandJob != null) {
      return Response.ok(toCommandStateDto(commandJob).getStatus().toString()).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @PUT
  @Path("/command/{id}/status")
  public Response setCommandStatus(@PathParam("id") Integer id, String newStatus) {
    try {
      if(CommandStateDto.Status.CANCELED.toString().equals(newStatus)) {
        commandJobService.cancelCommand(id);
        return Response.ok().build();
      } else {
        // Status may only be updated to CANCELED.
        log.info("setCommandStatus called with newStatus '{}' (only '{}' is allowed)", newStatus, CommandStateDto.Status.CANCELED);
        return Response.status(Status.BAD_REQUEST).entity("SetCommandStatus_BadRequest_IllegalStatus").build();
      }
    } catch(NoSuchCommandJobException ex) {
      return Response.status(Status.NOT_FOUND).entity("SetCommandStatus_NotFound").build();
    } catch(IllegalStateException ex) {
      return Response.status(Status.BAD_REQUEST).entity("SetCommandStatus_BadRequest_NotCancellable").build();
    }
  }

  @POST
  @Path("/import")
  public Response importData(Commands.ImportCommandOptionsDto options) {
    ImportCommandOptions importOptions = new ImportCommandOptionsDtoImpl(options);
    Command<ImportCommandOptions> importCommand = commandRegistry.newCommand("import");
    importCommand.setOptions(importOptions);

    return launchCommand(importCommand);
  }

  @POST
  @Path("/copy")
  public Response copyData(final Commands.CopyCommandOptionsDto options) {
    CopyCommandOptions copyOptions = new CopyCommandOptionsDtoImpl(opalRuntime, options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand("copy");
    copyCommand.setOptions(copyOptions);

    return launchCommand(copyCommand);
  }

  @POST
  @Path("/report")
  public Response createReport(Commands.ReportCommandOptionsDto options) {
    ReportCommandOptions reportOptions = new ReportCommandOptionsDtoImpl(options);
    Command<ReportCommandOptions> reportCommand = commandRegistry.newCommand("report");
    reportCommand.setOptions(reportOptions);

    return launchCommand(reportCommand);
  }

  //
  // Methods
  //

  public void setCommandJobService(CommandJobService commandJobService) {
    this.commandJobService = commandJobService;
  }

  public void setCommandRegistry(CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
  }

  private Response launchCommand(Command<?> command) {
    CommandJob commandJob = new CommandJob(command);
    return buildLaunchCommandResponse(commandJobService.launchCommand(commandJob));
  }

  private Response buildLaunchCommandResponse(Integer jobId) {
    return Response.created(UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(jobId)).build();
  }

  private CommandStateDto toCommandStateDto(CommandJob commandJob) {
    CommandStateDto.Builder dtoBuilder = CommandStateDto.newBuilder() //
    .setId(commandJob.getId()) //
    .setCommand(commandJob.getCommand().getName()) //
    .setCommandArgs(commandJob.getCommand().toString()) //
    .setOwner(commandJob.getOwner()) //
    .setStatus(commandJob.getStatus().toString()) //
    .addAllMessages(commandJob.getMessages());

    if(commandJob.getStartTime() != null) {
      dtoBuilder.setStartTime(commandJob.getStartTime().getTime());
    }
    if(commandJob.getEndTime() != null) {
      dtoBuilder.setEndTime(commandJob.getEndTime().getTime());
    }

    return dtoBuilder.build();
  }
}
