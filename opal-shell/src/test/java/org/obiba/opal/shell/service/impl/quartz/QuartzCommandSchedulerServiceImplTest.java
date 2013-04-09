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

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Test;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.Command;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for {@link QuartzCommandSchedulerServiceImpl}.
 */
public class QuartzCommandSchedulerServiceImplTest {
  //
  // Test Methods
  //

  @Test
  public void testAddCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    JobDetail expectedJobDetail = new JobDetail("commandName", "reporting", QuartzCommandJob.class);
    expectedJobDetail.getJobDataMap().put("command", "commandLine");
    schedulerMock.addJob(eqJobDetail(expectedJobDetail), eq(true));
    expectLastCall().once();

    Subject mockSubject = createMock(Subject.class);
    ThreadContext.bind(mockSubject);
    expect(mockSubject.getPrincipals()).andReturn(createMock(PrincipalCollection.class)).anyTimes();

    replay(schedulerMock, mockSubject);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.addCommand("commandName", "reporting", new CommandStub("commandName", "commandLine"));

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  public void testDeleteCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    expect(schedulerMock.deleteJob("commandName", "reporting")).andReturn(true).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.deleteCommand("commandName", "reporting");

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  public void testScheduleCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger expectedCronTrigger = new CronTrigger("commandName-trigger", "reporting", "commandName", "reporting",
        "0 * * * * ?");
    expect(schedulerMock.scheduleJob(eqCronTrigger(expectedCronTrigger))).andReturn(null).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.scheduleCommand("commandName", "reporting", "0 * * * * ?");

    // Verify behaviour
    verify(schedulerMock);
  }

  @Test
  public void testUnscheduleCommand() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger cronTrigger = new CronTrigger("commandName-trigger", "reporting", "commandName", "reporting",
        "0 * * * * ?");
    expect(schedulerMock.getTriggersOfJob("commandName", "reporting")).andReturn(new Trigger[] { cronTrigger }).once();
    expect(schedulerMock.unscheduleJob("commandName-trigger", "reporting")).andReturn(true).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    sut.unscheduleCommand("commandName", "reporting");

    // Verify behaviour
    verify(schedulerMock);

  }

  @Test
  public void testGetCommandSchedule() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger cronTrigger = new CronTrigger("commandName-trigger", "reporting", "commandName", "reporting",
        "0 * * * * ?");
    expect(schedulerMock.getTriggersOfJob("commandName", "reporting")).andReturn(new Trigger[] { cronTrigger }).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertNotNull(commandSchedule);
    assertEquals("0 * * * * ?", commandSchedule);
  }

  @Test
  public void testGetCommandSchedule_ReturnsFirstScheduleIfCommandHasMultipleSchedules() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    CronTrigger cronTrigger1 = new CronTrigger("commandName-trigger1", "reporting", "commandName", "reporting",
        "0 * * * * ?");
    CronTrigger cronTrigger2 = new CronTrigger("commandName-trigger2", "reporting", "commandName", "reporting",
        "0 0/2 * * * ?");
    expect(schedulerMock.getTriggersOfJob("commandName", "reporting"))
        .andReturn(new Trigger[] { cronTrigger1, cronTrigger2 }).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertNotNull(commandSchedule);
    assertEquals("0 * * * * ?", commandSchedule);
  }

  @Test
  public void testGetCommandSchedule_ReturnsNullIfCommandNotScheduled() throws Exception {
    // Setup
    Scheduler schedulerMock = createMock(Scheduler.class);
    expect(schedulerMock.getTriggersOfJob("commandName", "reporting")).andReturn(new Trigger[] { }).once();

    replay(schedulerMock);

    // Exercise
    QuartzCommandSchedulerServiceImpl sut = new QuartzCommandSchedulerServiceImpl(schedulerMock);
    String commandSchedule = sut.getCommandSchedule("commandName", "reporting");

    // Verify
    assertEquals(null, commandSchedule);
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

        boolean matches = true;
        matches &= actualJobDetail.getName().equals(expected.getName());
        matches &= actualJobDetail.getGroup().equals(expected.getGroup());
        matches &= actualJobDetail.getJobClass().getName().equals(expected.getJobClass().getName());
        matches &= actualJobDetail.getJobDataMap().getString("command")
            .equals(expected.getJobDataMap().getString("command"));

        return matches;
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqJobDetail(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with name \"");
      buffer.append(expected.getName());
      buffer.append("\", group \"");
      buffer.append(expected.getGroup());
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

        boolean matches = true;
        matches &= actualCronTrigger.getName().equals(expected.getName());
        matches &= actualCronTrigger.getGroup().equals(expected.getGroup());
        matches &= actualCronTrigger.getJobName().equals(expected.getJobName());
        matches &= actualCronTrigger.getJobGroup().equals(expected.getJobGroup());
        matches &= actualCronTrigger.getCronExpression().equals(expected.getCronExpression());

        return matches;
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqCronTrigger(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with name \"");
      buffer.append(expected.getName());
      buffer.append("\", group \"");
      buffer.append(expected.getGroup());
      buffer.append("\", jobName \"");
      buffer.append(expected.getJobName());
      buffer.append("\", jobGroup \"");
      buffer.append(expected.getJobGroup());
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
