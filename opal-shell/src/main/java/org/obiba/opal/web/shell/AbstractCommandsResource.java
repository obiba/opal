/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.shell;

import javax.ws.rs.core.Response;

import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandJobService;

import com.google.common.base.Strings;

public abstract class AbstractCommandsResource {

  protected final CommandJobService commandJobService;

  protected final OpalRuntime opalRuntime;

  protected AbstractCommandsResource(OpalRuntime opalRuntime, CommandJobService commandJobService) {
    this.commandJobService = commandJobService;
    this.opalRuntime = opalRuntime;
  }

  protected Response launchCommand(Command<?> command) {
    return launchCommand(command.getName(), command);
  }

  protected Response launchCommand(String name, Command<?> command) {
    CommandJob commandJob = newCommandJob(name, command);
    return buildLaunchCommandResponse(commandJobService.launchCommand(commandJob));
  }

  protected CommandJob newCommandJob(String name, Command<?> command) {
    return new CommandJob(name, command);
  }

  protected abstract Response buildLaunchCommandResponse(Integer jobId);

}
