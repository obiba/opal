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

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.shell.web.ReportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Opal Web Shell services.
 */
@Component
@Transactional
@Path("/shell")
public class WebShellResource extends AbstractCommandsResource {

  private static final Logger log = LoggerFactory.getLogger(WebShellResource.class);

  private CommandRegistry commandRegistry;

  @Autowired
  @Qualifier("web")
  public void setCommandRegistry(CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
  }

  @GET
  @Path("/commands")
  public List<CommandStateDto> getCommands() {
    List<CommandStateDto> commandDtoList = new ArrayList<>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      commandDtoList.add(Dtos.asDto(commandJob));
    }

    return commandDtoList;
  }

  @GET
  @Path("/command/{id}")
  public Response getCommand(@PathParam("id") Integer id) {
    CommandJob commandJob = commandJobService.getCommand(id);

    return commandJob == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(Dtos.asDto(commandJob)).build();
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

    return commandJob == null
        ? Response.status(Status.NOT_FOUND).build()
        : Response.ok(Dtos.asDto(commandJob).getStatus()).build();
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
        log.info("setCommandStatus called with newStatus '{}' (only '{}' is allowed)", newStatus,
            CommandStateDto.Status.CANCELED);
        return Response.status(Status.BAD_REQUEST).entity("SetCommandStatus_BadRequest_IllegalStatus").build();
      }
    } catch(NoSuchCommandJobException ex) {
      return Response.status(Status.NOT_FOUND).entity("SetCommandStatus_NotFound").build();
    } catch(IllegalStateException ex) {
      return Response.status(Status.BAD_REQUEST).entity("SetCommandStatus_BadRequest_NotCancellable").build();
    }
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

  @Override
  protected Response buildLaunchCommandResponse(Integer jobId) {
    return Response.created(
        UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(jobId))
        .build();
  }

}
