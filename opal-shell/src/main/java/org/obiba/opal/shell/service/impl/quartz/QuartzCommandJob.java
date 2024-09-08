/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.service.impl.quartz;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.security.BackgroundJobServiceAuthToken;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.CommandLines;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandJobService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

import java.util.Arrays;

/**
 *
 */
public class QuartzCommandJob implements Job {
  //
  // Instance Variables
  //

  @Autowired
  @Qualifier("web")
  private CommandRegistry commandRegistry;

  @Autowired
  private CommandJobService commandJobService;

  //
  // Job Methods
  //

  @Override
  @SuppressWarnings("OverlyStrongTypeCast")
  public void execute(JobExecutionContext context) throws JobExecutionException {
    autowireSelf(context);

    Command<?> command = getCommand(context);
    CommandJob commandJob = new CommandJob(command);
    commandJobService.launchCommand(commandJob, getSubject(context));
  }

  //
  // Methods
  //

  private void autowireSelf(JobExecutionContext context) throws JobExecutionException {
    ApplicationContext applicationContext;
    try {
      applicationContext = (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
    } catch(SchedulerException ex) {
      throw new JobExecutionException("applicationContext lookup failed", ex);
    }
    applicationContext.getAutowireCapableBeanFactory()
        .autowireBeanProperties(this, AutowireCapableBeanFactory.AUTOWIRE_NO, false);
  }

  private Subject getSubject(JobExecutionContext context) {
    PrincipalCollection principals = (PrincipalCollection) context.getJobDetail().getJobDataMap().get("subject");
    if(principals == null) {
      // Login as background task user
      principals = SecurityUtils.getSecurityManager().authenticate(BackgroundJobServiceAuthToken.INSTANCE)
          .getPrincipals();
    }
    return new Subject.Builder().principals(principals).authenticated(true).buildSubject();
  }

  private Command<?> getCommand(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    String commandLine = dataMap.getString("command");
    try {
      return toCommand(commandLine);
    } catch(ArgumentValidationException ex) {
      throw new JobExecutionException("Invalid task parameter 'command': " + commandLine, ex);
    }
  }

  private Command<?> toCommand(String commandLine) throws ArgumentValidationException {
    String[] commandLineArray = CommandLines.parseArguments(commandLine);
    String commandName = commandLineArray[0];
    String[] commandArgs = Arrays.copyOfRange(commandLineArray, 1, commandLineArray.length);

    Class<?> optionsClass = commandRegistry.getOptionsClass(commandName);
    Command<Object> command = commandRegistry.newCommand(commandName);
    command.setOptions(CliFactory.parseArguments(optionsClass, commandArgs));
    return command;
  }
}
