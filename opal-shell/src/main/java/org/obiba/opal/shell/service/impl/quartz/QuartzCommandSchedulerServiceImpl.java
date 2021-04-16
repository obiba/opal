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

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.shell.service.CommandSchedulerServiceException;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz-based implementation of {@link CommandSchedulerService}.
 */
@Component
public class QuartzCommandSchedulerServiceImpl implements CommandSchedulerService {

  private static final Logger log = LoggerFactory.getLogger(QuartzCommandSchedulerServiceImpl.class);

  private final Scheduler scheduler;

  @Autowired
  public QuartzCommandSchedulerServiceImpl(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public void addCommand(String name, String group, Command<?> command) {
    try {
      JobDetail jobDetail = JobBuilder.newJob(QuartzCommandJob.class) //
          .withIdentity(name, group) //
          .storeDurably(true) // OPAL-917
          .usingJobData("command", command.toString()) //
          .build();
      jobDetail.getJobDataMap().put("subject", SecurityUtils.getSubject().getPrincipals());
      log.debug("Add job {}", jobDetail);
      scheduler.addJob(jobDetail, true);
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void deleteCommand(String name, String group) {
    try {
      JobKey jobKey = new JobKey(name, group);
      log.debug("Delete job {}", jobKey);
      scheduler.deleteJob(jobKey);
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void scheduleCommand(String name, String group, String cronExpression) {
    try {
      Trigger trigger = TriggerBuilder.newTrigger() //
          .withIdentity(name + "-trigger", group) //
          .forJob(name, group) //
          .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)) //
          .build();
      log.debug("Schedule job {} ({}): {}", trigger.getKey(), cronExpression, trigger);
      scheduler.scheduleJob(trigger);
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public void unscheduleCommand(String name, String group) {
    try {
      for(Trigger trigger : scheduler.getTriggersOfJob(new JobKey(name, group))) {
        log.debug("Unschedule job {}", trigger);
        scheduler.unscheduleJob(trigger.getKey());
      }
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  @Override
  public String getCommandSchedule(String name, String group) {
    try {
      List<? extends Trigger> triggers = scheduler.getTriggersOfJob(new JobKey(name, group));
      return triggers != null && !triggers.isEmpty() && triggers.get(0) instanceof CronTrigger //
          ? ((CronTrigger) triggers.get(0)).getCronExpression() //
          : null;
    } catch(SchedulerException ex) {
      throw new CommandSchedulerServiceException(ex);
    }
  }

  public boolean hasCommand(String name, String group) {
    try {
      JobDetail detail = scheduler.getJobDetail(new JobKey(name, group));
      return detail != null;
    } catch (SchedulerException e) {
      return false;
    }
  }
}
