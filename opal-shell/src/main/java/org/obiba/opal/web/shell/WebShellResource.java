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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.CopyCommandOptions;
import org.obiba.opal.shell.commands.options.ImportCommandOptions;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.web.CopyCommandOptionsDtoImpl;
import org.obiba.opal.shell.web.ImportCommandOptionsDtoImpl;
import org.obiba.opal.web.model.Commands;
import org.obiba.opal.web.model.Commands.CommandStateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Opal Web Shell services.
 */
@Component
@Path("/shell")
public class WebShellResource {
  //
  // Instance Variables
  //

  @Autowired
  private CommandJobService commandJobService;

  @Autowired
  private CommandRegistry commandRegistry;

  //
  // Web Service Methods
  //

  @GET
  @Path("/commands")
  public List<CommandStateDto> getCommands() {
    List<CommandStateDto> commandDtoList = new ArrayList<CommandStateDto>();

    List<CommandJob> history = commandJobService.getHistory();
    for(CommandJob commandJob : history) {
      CommandStateDto dto = CommandStateDto.newBuilder() //
      .setCommand(commandJob.getCommand().getName()) //
      .setCommandArgs(commandJob.getCommand().toString()) //
      .setOwner(commandJob.getOwner()) //
      .setStatus(commandJob.getStatus()) //
      .setStartTime(commandJob.getStartTime() != null ? formatTime(commandJob.getStartTime()) : null) //
      .setEndTime(commandJob.getEndTime() != null ? formatTime(commandJob.getEndTime()) : null) //
      .addAllMessages(commandJob.getMessages()).build();

      commandDtoList.add(dto);
    }

    return commandDtoList;
  }

  @POST
  @Path("/import")
  public void importData(Commands.ImportCommandOptionsDto options) {
    ImportCommandOptions importOptions = new ImportCommandOptionsDtoImpl(options);
    Command<ImportCommandOptions> importCommand = commandRegistry.newCommand("import");
    importCommand.setOptions(importOptions);

    launchCommand(importCommand);
  }

  @POST
  @Path("/copy")
  public void copyData(Commands.CopyCommandOptionsDto options) {
    CopyCommandOptions copyOptions = new CopyCommandOptionsDtoImpl(options);
    Command<CopyCommandOptions> copyCommand = commandRegistry.newCommand("copy");
    copyCommand.setOptions(copyOptions);

    launchCommand(copyCommand);
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

  protected String formatTime(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(CommandJob.DATE_FORMAT_PATTERN);
    return dateFormat.format(date);
  }

  private void launchCommand(Command<?> command) {
    CommandJob commandJob = new CommandJob();
    command.setShell(commandJob);
    commandJob.setCommand(command);
    commandJobService.launchCommand(commandJob);
  }
}
