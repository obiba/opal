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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.shell.web.CopyCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ImportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.obiba.opal.web.model.Commands.CopyCommandOptionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;

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
    CopyCommandOptions copyOptions = new WebShellCopyCommandOptions(options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand("copy");
    copyCommand.setOptions(copyOptions);

    return launchCommand(copyCommand);
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

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  //
  // Inner Classes / Interfaces
  //

  @CommandLineInterface(application = "copy")
  class WebShellCopyCommandOptions extends CopyCommandOptionsDtoImpl {

    private String pathWithExtension;

    public WebShellCopyCommandOptions(CopyCommandOptionsDto dto) {
      super(dto);
    }

    @Override
    public String getOut() {
      if(dto.hasOut() && dto.hasFormat()) {
        if(pathWithExtension == null) {
          pathWithExtension = addFileExtensionIfMissing(dto.getOut(), dto.getFormat());
        }
        return pathWithExtension;
      } else {
        return super.getOut();
      }
    }

    private String addFileExtensionIfMissing(String outputFilePath, String outputFileFormat) {
      String pathWithExtension = outputFilePath;

      FileObject file = null;
      try {
        file = resolveFileInFileSystem(outputFilePath);

        if(file.getType() == FileType.FILE) {
          if(outputFileFormat.equals("csv") && !outputFilePath.endsWith(".csv")) {
            pathWithExtension = outputFilePath + ".csv";
          } else if(outputFileFormat.equals("excel") && !outputFilePath.endsWith(".xls") && !outputFilePath.endsWith(".xlsx")) {
            pathWithExtension = outputFilePath + ".xlsx"; // prefer .xlsx over .xls
          } else if(outputFileFormat.equals("xml") && !outputFilePath.endsWith(".zip")) {
            pathWithExtension = outputFilePath + ".zip";
          }
        }
      } catch(FileSystemException ex) {
        log.error("Unexpected file system exception in addFileExtensionIfMissing", ex);
      }

      return pathWithExtension;
    }
  }
}
