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
import javax.ws.rs.Path;

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;
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
      .setCommand("theCommand") // TODO: Set the command name (or command + args?)
      .setOwner(commandJob.getOwner()) //
      .setStatus(commandJob.getStatus()) //
      .setStartTime(formatTime(commandJob.getStartTime())) //
      .setEndTime(formatTime(commandJob.getEndTime())) //
      .addAllMessages(commandJob.getMessages()).build();

      commandDtoList.add(dto);
    }

    return commandDtoList;
  }

  //
  // Methods
  //

  public void setCommandJobService(CommandJobService commandJobService) {
    this.commandJobService = commandJobService;
  }

  protected String formatTime(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat(CommandJob.DATE_FORMAT_PATTERN);
    return dateFormat.format(date);
  }
}
