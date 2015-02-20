/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.impl.DefaultCommandJobService.FutureCommandJob;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultCommandJobService}.
 */
public class DefaultCommandJobServiceTest {
  //
  // Instance Variables
  //

  private DefaultCommandJobService sut;

  private Executor mockExecutor;

  private Subject mockSubject;

  private CommandJob commandJob;

  private List<FutureCommandJob> futureCommandJobs;

  private List<FutureCommandJob> jobsTerminated;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    Command<?> cmd = createMock(Command.class);
    cmd.setShell((OpalShell) EasyMock.anyObject());
    expectLastCall().once();
    //expect(cmd.getName()).andReturn("").anyTimes();
    commandJob = new CommandJob("", cmd);

    mockSubject = createMock(Subject.class);

    final Capture<Runnable> c = new Capture<>();
    expect(mockSubject.associateWith(EasyMock.capture(c))).andAnswer(new IAnswer<Runnable>() {

      @Override
      public Runnable answer() throws Throwable {
        return c.getValue();
      }

    }).anyTimes();

    expect(mockSubject.getPrincipal()).andReturn("testUser").anyTimes();
    expect(mockSubject.getSession(false)).andStubReturn(null);

    ThreadContext.bind(mockSubject);
    replay(mockSubject);

    // Expect commandJob executed once.
    mockExecutor = createMock(Executor.class);
    mockExecutor.execute(eqFutureCommandJob(new FutureCommandJob(mockSubject, commandJob)));
    expectLastCall().once();

    // Expect current user is "testUser".
    // mockUserProvider = createMock(UserProvider.class);
    // expect(mockUserProvider.getUsername()).andReturn("testUser").atLeastOnce();

    sut = new DefaultCommandJobService() {
      @Override
      protected List<FutureCommandJob> getFutureCommandJobs() {
        return futureCommandJobs != null ? futureCommandJobs : super.getFutureCommandJobs();
      }

      @Override
      List<FutureCommandJob> getTerminatedJobs() {
        return jobsTerminated != null ? jobsTerminated : super.getTerminatedJobs();
      }
    };
    sut.setExecutor(mockExecutor);

    replay(cmd, mockExecutor);
  }

  @After
  public void tearDown() {
    ThreadContext.unbindSubject();
  }

  //
  // Test Methods
  //

  @Test
  public void testNotRunning() {
    assertThat(sut.isRunning()).isFalse();
  }

  @Test
  public void testStart() {
    // Exercise
    sut.start();

    // Verify
    assertThat(sut.isRunning()).isTrue();
  }

  @Test
  public void testStop() {
    // Exercise
    sut.stop();

    // Verify
    assertThat(sut.isRunning()).isFalse();
  }

  @Test
  public void testGetCommand() {
    // Test-specific setup
    CommandJobService sut = new DefaultCommandJobService() {
      @Override
      public List<CommandJob> getHistory() {
        return createJobHistory();
      }
    };

    // Exercise
    Integer jobId = 2;
    CommandJob job = sut.getCommand(jobId);

    // Verify
    assertThat(job).isNotNull();
    assertThat(job.getId()).isEqualTo(jobId);
  }

  @Test
  public void testGetCommand_ReturnsNullIfJobDoesNotExist() {
    // Test-specific setup
    CommandJobService sut = new DefaultCommandJobService() {
      @Override
      public List<CommandJob> getHistory() {
        return createJobHistory();
      }
    };

    // Exercise
    Integer bogusJobId = 99;
    CommandJob job = sut.getCommand(bogusJobId);

    // Verify
    assertThat(job).isNull();
  }

  @Test
  public void testLaunchCommand_AssignsIdToCommandJob() {
    sut.launchCommand(commandJob);
    assertThat(commandJob.getId()).isNotNull();
  }

  @Test
  public void testLaunchCommand_MakesTheCurrentUserTheOwnerOfTheCommandJob() {
    sut.launchCommand(commandJob);
    verify(mockExecutor);
    assertThat(commandJob.getOwner()).isEqualTo(SecurityUtils.getSubject().getPrincipal().toString());
  }

  @Test
  public void testLaunchCommand_ExecutesCommandJob() {
    sut.launchCommand(commandJob);
    verify(mockExecutor);
  }

  @Test
  public void testGetHistory_ReturnsAllJobsInReverseOrderOfSubmission() {
    // Test-specific setup
    futureCommandJobs = new ArrayList<>();
    futureCommandJobs.add(new FutureCommandJob(mockSubject, createCommandJob(1, new Date(1), null)));
    futureCommandJobs.add(new FutureCommandJob(mockSubject, createCommandJob(2, new Date(2), null)));
    futureCommandJobs.add(new FutureCommandJob(mockSubject, createCommandJob(3, new Date(3), null)));

    // Exercise
    List<CommandJob> history = sut.getHistory();

    // Verify
    assertThat(history).isNotNull();

    assertThat(history).hasSize(3);
    assertThat(history.get(0).getId()).isEqualTo(3); // task 3 first, since it was submitted last
    assertThat(history.get(1).getId()).isEqualTo(2); // then task 2
    assertThat(history.get(2).getId()).isEqualTo(1); // then task 1
  }

  @Test
  public void testCancelCommand_ChangesJobStatusToCancelPending() {
    // Test-specific setup
    initCommandJob(Status.IN_PROGRESS);

    // Exercise
    sut.cancelCommand(commandJob.getId());

    // Verify
    assertThat(commandJob.getStatus()).isEqualTo(Status.CANCEL_PENDING);
  }

  @Test(expected = IllegalStateException.class)
  public void testCancelCommand_ThrowsIllegalStateExceptionIfCommandInSucceededState() {
    initCommandJob(Status.SUCCEEDED);

    sut.cancelCommand(commandJob.getId());
  }

  @Test(expected = IllegalStateException.class)
  public void testCancelCommand_ThrowsIllegalStateExceptionIfCommandInFailedState() {
    initCommandJob(Status.FAILED);

    sut.cancelCommand(commandJob.getId());
  }

  @Test(expected = IllegalStateException.class)
  public void testCancelCommand_ThrowsIllegalStateExceptionIfCommandInCanceledState() {
    initCommandJob(Status.CANCELED);

    sut.cancelCommand(commandJob.getId());
  }

  @Test(expected = IllegalStateException.class)
  public void testDeleteCommand_ThrowsIllegalStateExceptionIfCommandInNotStartedState() {
    initCommandJob(Status.NOT_STARTED);

    sut.deleteCommand(commandJob.getId());
  }

  @Test(expected = IllegalStateException.class)
  public void testDeleteCommand_ThrowsIllegalStateExceptionIfCommandInInProgressState() {
    initCommandJob(Status.IN_PROGRESS);

    sut.deleteCommand(commandJob.getId());
  }

  @Test(expected = IllegalStateException.class)
  public void testDeleteCommand_ThrowsIllegalStateExceptionIfCommandInCancelPendingState() {
    initCommandJob(Status.CANCEL_PENDING);

    sut.deleteCommand(commandJob.getId());
  }

  @Test
  public void testDeleteCompletedCommands() {
    // Test-specific setup

    // Create some completed/terminated commands -- these SHOULD be deleted.
    FutureCommandJob succeededJob = new FutureCommandJob(mockSubject,
        createCommandJob(1, new Date(1), Status.SUCCEEDED));
    FutureCommandJob failedJob = new FutureCommandJob(mockSubject, createCommandJob(1, new Date(1), Status.FAILED));
    FutureCommandJob cancelledJob = new FutureCommandJob(mockSubject,
        createCommandJob(1, new Date(1), Status.CANCELED));

    // Put them in the list of future command jobs.
    futureCommandJobs = new ArrayList<>();
    futureCommandJobs.add(succeededJob);
    futureCommandJobs.add(failedJob);
    futureCommandJobs.add(cancelledJob);

    // Put them in the sub-list of terminated jobs.
    // Note: These are normally put there by the executor's afterExecute callback.
    jobsTerminated = new ArrayList<>();
    jobsTerminated.add(succeededJob);
    jobsTerminated.add(failedJob);
    jobsTerminated.add(cancelledJob);

    // Exercise
    sut.deleteCompletedCommands();

    // Verify that all the completed/terminated jobs were removed
    assertThat(jobsTerminated).isEmpty();
  }

  @Test
  public void testIsDeletable_ReturnsTrueForSucceededJob() {
    // Test-specific setup
    initCommandJob(Status.SUCCEEDED);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isTrue();
  }

  @Test
  public void testIsDeletable_ReturnsTrueForFailedJob() {
    // Test-specific setup
    initCommandJob(Status.FAILED);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isTrue();
  }

  @Test
  public void testIsDeletable_ReturnsTrueForCanceledJob() {
    // Test-specific setup
    initCommandJob(Status.CANCELED);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isTrue();
  }

  @Test
  public void testIsDeletable_ReturnsFalseForNotStartedJob() {
    // Test-specific setup
    initCommandJob(Status.NOT_STARTED);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isFalse();
  }

  @Test
  public void testIsDeletable_ReturnsFalseForInProgressJob() {
    // Test-specific setup
    initCommandJob(Status.IN_PROGRESS);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isFalse();
  }

  @Test
  public void testIsDeletable_ReturnsFalseForCancelPendingJob() {
    // Test-specific setup
    initCommandJob(Status.CANCEL_PENDING);

    // Exercise
    boolean isDeletable = sut.isDeletable(commandJob);

    // Verify
    assertThat(isDeletable).isFalse();
  }

  //
  // Helper Methods
  //

  private List<CommandJob> createJobHistory() {
    List<CommandJob> jobHistory = new ArrayList<>();

    jobHistory.add(createCommandJob(1, new Date(1l), null));
    jobHistory.add(createCommandJob(2, new Date(2l), null));
    jobHistory.add(createCommandJob(3, new Date(3l), null));

    return jobHistory;
  }

  private CommandJob createCommandJob(Integer id, Date submitTime, Status status) {
    Command<?> cmd = createMock(Command.class);
    cmd.setShell((OpalShell) EasyMock.anyObject());
    expectLastCall().once();

    CommandJob aCommandJob = new CommandJob("foo", cmd);
    aCommandJob.setId(id);
    aCommandJob.setSubmitTime(submitTime);
    aCommandJob.setStatus(status);

    replay(cmd);
    return aCommandJob;
  }

  private void initCommandJob(Status status) {
    commandJob.setId(1);
    // commandJob.setOwner("testUser");
    commandJob.setSubmitTime(new Date());
    commandJob.setStatus(status);

    futureCommandJobs = new ArrayList<>();
    futureCommandJobs.add(new FutureCommandJob(mockSubject, commandJob));
  }

  //
  // Inner Classes
  //

  static class FutureCommandJobMatcher implements IArgumentMatcher {

    private final FutureCommandJob expected;

    FutureCommandJobMatcher(FutureCommandJob expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      return actual instanceof FutureCommandJob &&
          ((FutureCommandJob) actual).getCommandJob().equals(expected.getCommandJob());
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqFutureCommandJob(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with commandJob \"");
      buffer.append(expected.getCommandJob());
      buffer.append("\")");
    }

  }

  static Runnable eqFutureCommandJob(FutureCommandJob in) {
    EasyMock.reportMatcher(new FutureCommandJobMatcher(in));
    return null;
  }
}
