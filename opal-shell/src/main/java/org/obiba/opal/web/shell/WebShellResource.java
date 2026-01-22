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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.Dtos;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Opal Web Shell services.
 */
@Component
@Path("/shell")
@Tag(name = "Tasks", description = "Operations on tasks")
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
  @Operation(
    summary = "Get all commands",
    description = "Retrieves the list of all command jobs in the system with their current status."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved command list"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
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
  @Operation(
    summary = "Get command by ID",
    description = "Retrieves detailed information about a specific command job by its ID."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved command details"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getCommand(@PathParam("id") Integer id) {
    CommandJob commandJob = commandJobService.getCommand(id);

    return commandJob == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(Dtos.asDto(commandJob)).build();
  }

  @DELETE
  @Path("/command/{id}")
  @Operation(
    summary = "Delete command",
    description = "Deletes a specific command job by its ID. Only completed or failed commands can be deleted."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Command successfully deleted"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "400", description = "Command cannot be deleted (still running)"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
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
  @Operation(
    summary = "Delete completed commands",
    description = "Deletes all completed command jobs from the system to clean up the command history."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "All completed commands successfully deleted"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteCompletedCommands() {
    commandJobService.deleteCompletedCommands();
    return Response.ok().build();
  }

  @GET
  @Path("/command/{id}/status")
  @Operation(
    summary = "Get command status",
    description = "Retrieves the current status of a specific command job."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved command status"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getCommandStatus(@PathParam("id") Integer id) {
    CommandJob commandJob = commandJobService.getCommand(id);

    return commandJob == null
        ? Response.status(Status.NOT_FOUND).build()
        : Response.ok(Dtos.asDto(commandJob).getStatus()).build();
  }

  @PUT
  @Path("/command/{id}/status")
  @Operation(
    summary = "Update command status",
    description = "Updates the status of a specific command job. Currently only allows canceling running commands."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Command status successfully updated"),
    @ApiResponse(responseCode = "404", description = "Command not found"),
    @ApiResponse(responseCode = "400", description = "Invalid status or command cannot be canceled"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
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

  //
  // Methods
  //

  @Override
  protected Response buildLaunchCommandResponse(CommandJob commandJob) {
    return Response.created(
        UriBuilder.fromPath("/").path(WebShellResource.class).path(WebShellResource.class, "getCommand").build(commandJob.getId()))
        .entity(Dtos.asDto(commandJob))
        .build();
  }

}
