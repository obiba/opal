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

import java.util.Collections;
import java.util.List;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.fest.util.Lists;
import org.junit.Test;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link QuartzCommandSchedulerServiceImpl}.
 */
public class QuartzCommandSchedulerServiceImplTest {

  @Test
  public void testAddCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    JobDetail expectedJobDetail = JobBuilder.newJob(QuartzCommandJob.class).withIdentity("commandName", "reporting")
        .usingJobData("command", "commandLine").build();
    schedulerMock.addJob(eqJobDetail(expectedJobDetail), eq(true));
    expectLastCall().once();

    Subject mockSubject = createMock(Subject.class);
    ThreadContext.bind(mockSubject);
    expect(mockSubject.getPrincipals()).andReturn(createMock(PrincipalCollection.class)).anyTimes();

    replay(schedulerMock, mockSubject);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.addCommand("commandName", "reporting", new CommandStub("commandName", "commandLine"));
    ThreadContext.unbindSubject();

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  public void testDeleteCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    expect(schedulerMock.deleteJob(new JobKey("commandName", "reporting"))).andReturn(true).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.deleteCommand("commandName", "reporting");

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  public void testScheduleCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger expectedCronTrigger = TriggerBuilder.newTrigger() //
        .withIdentity("commandName-trigger", "reporting") //
        .forJob("commandName", "reporting") //
        .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")) //
        .build();
    expect(schedulerMock.scheduleJob(eqCronTrigger(expectedCronTrigger))).andReturn(null).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.scheduleCommand("commandName", "reporting", "0 * * * * ?");

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testUnscheduleCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    Trigger cronTrigger = TriggerBuilder.newTrigger() //
        .withIdentity("commandName-trigger", "reporting") //
        .forJob("commandName", "reporting") //
        .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")) //
        .build();

    expect(schedulerMock.getTriggersOfJob(new JobKey("commandName", "reporting")))
        .andReturn((List) Lists.newArrayList(cronTrigger)).once();
    expect(schedulerMock.unscheduleJob(new TriggerKey("commandName-trigger", "reporting"))).andReturn(true).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.unscheduleCommand("commandName", "reporting");

    // Verify behaviour
    verify(schedulerMock);

  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testGetCommandSchedule() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger cronTrigger = TriggerBuilder.newTrigger() //
        .withIdentity("commandName-trigger", "reporting") //
        .forJob("commandName", "reporting") //
        .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")) //
        .build();
    expect(schedulerMock.getTriggersOfJob(new JobKey("commandName", "reporting")))
        .andReturn((List) Lists.newArrayList(cronTrigger)).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertThat(commandSchedule).isNotNull();
    assertThat(commandSchedule).isEqualTo("0 * * * * ?");
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testGetCommandSchedule_ReturnsFirstScheduleIfCommandHasMultipleSchedules() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger cronTrigger1 = TriggerBuilder.newTrigger() //
        .withIdentity("commandName-trigger1", "reporting") //
        .forJob("commandName", "reporting") //
        .withSchedule(CronScheduleBuilder.cronSchedule("0 * * * * ?")) //
        .build();
    CronTrigger cronTrigger2 = TriggerBuilder.newTrigger() //
        .withIdentity("commandName-trigger2", "reporting") //
        .forJob("commandName", "reporting") //
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 * * * ?")) //
        .build();

    expect(schedulerMock.getTriggersOfJob(new JobKey("commandName", "reporting")))
        .andReturn((List) Lists.newArrayList(cronTrigger1, cronTrigger2)).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertThat(commandSchedule).isNotNull();
    assertThat(commandSchedule).isEqualTo("0 * * * * ?");
  }

  @Test
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void testGetCommandSchedule_ReturnsNullIfCommandNotScheduled() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    expect(schedulerMock.getTriggersOfJob(new JobKey("commandName", "reporting")))
        .andReturn((List) Collections.emptyList()).once();

    replay(schedulerMock);

    // Exercise
    CommandSchedulerService sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertThat(commandSchedule).isNull();
  }

  //
  // Inner Classes
  //

  static class CommandStub implements Command<Object> {

    private final String name;

    private final String commandLine;

    CommandStub(String name, String commandLine) {
      this.name = name;
      this.commandLine = commandLine;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public void setOwner(String owner) {

    }

    @Override
    public String getOwner() {
      return null;
    }

    @Override
    public String toString() {
      return commandLine;
    }

    @Override
    public int execute() {
      return 0;
    }

    @Override
    public Object getOptions() {
      return null;
    }

    @Override
    public void setOptions(Object options) {
    }

    @Override
    public void setShell(OpalShell shell) {
    }
  }

  static class JobDetailMatcher implements IArgumentMatcher {

    private final JobDetail expected;

    JobDetailMatcher(JobDetail expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof JobDetail) {
        JobDetail actualJobDetail = (JobDetail) actual;
        return actualJobDetail.getKey().equals(expected.getKey()) &&
            actualJobDetail.getJobClass().getName().equals(expected.getJobClass().getName()) &&
            actualJobDetail.getJobDataMap().getString("command").equals(expected.getJobDataMap().getString("command"));
      }
      return false;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqJobDetail(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with name \"");
      buffer.append(expected.getKey().getName());
      buffer.append("\", group \"");
      buffer.append(expected.getKey().getGroup());
      buffer.append("\", jobClass \"");
      buffer.append(expected.getJobClass().getName());
      buffer.append("\", jobDataMap.getString(\"command\") \"");
      buffer.append(expected.getJobDataMap().getString("command"));
      buffer.append("\")");
    }

  }

  static JobDetail eqJobDetail(JobDetail in) {
    EasyMock.reportMatcher(new JobDetailMatcher(in));
    return null;
  }

  static class CronTriggerMatcher implements IArgumentMatcher {

    private final CronTrigger expected;

    CronTriggerMatcher(CronTrigger expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof CronTrigger) {
        CronTrigger actualCronTrigger = (CronTrigger) actual;
        return actualCronTrigger.getKey().equals(expected.getKey()) &&
            actualCronTrigger.getJobKey().equals(expected.getJobKey()) &&
            actualCronTrigger.getCronExpression().equals(expected.getCronExpression());
      }
      return false;
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqCronTrigger(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with name \"");
      buffer.append(expected.getKey().getName());
      buffer.append("\", group \"");
      buffer.append(expected.getKey().getGroup());
      buffer.append("\", jobName \"");
      buffer.append(expected.getJobKey().getName());
      buffer.append("\", jobGroup \"");
      buffer.append(expected.getJobKey().getGroup());
      buffer.append("\", cronExpression \"");
      buffer.append(expected.getCronExpression());
      buffer.append("\")");
    }

  }

  static CronTrigger eqCronTrigger(CronTrigger in) {
    EasyMock.reportMatcher(new CronTriggerMatcher(in));
    return null;
  }
}
