/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service.impl.quartz;

import java.util.Arrays;

import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandLines;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandJobService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 *
 */
public class QuartzCommandJob implements Job {
  //
  // Instance Variables
  //

  @Autowired
  private CommandJobService commandJobService;

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  //
  // Job Methods
  //

  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    String commandLine = dataMap.getString("command");

    Command<?> command = null;
    try {
      command = toCommand(commandLine);
    } catch(ArgumentValidationException ex) {
      throw new JobExecutionException("Invalid job parameter 'command': " + commandLine, ex);
    }

    CommandJob commandJob = new CommandJob(command);
    commandJobService.launchCommand(commandJob);
  }

  //
  // Methods
  //

  private Command<?> toCommand(String commandLine) throws ArgumentValidationException {
    String[] commandLineArray = CommandLines.parseArguments(commandLine);
    String commandName = commandLineArray[0];
    String[] commandArgs = Arrays.copyOfRange(commandLineArray, 1, commandLineArray.length);

    Class<?> optionsClass = commandRegistry.getOptionsClass(commandName);
    Object options = CliFactory.parseArguments(optionsClass, commandArgs);
    Command<Object> command = commandRegistry.newCommand(commandName);
    command.setOptions(options);

    return command;
  }
}
