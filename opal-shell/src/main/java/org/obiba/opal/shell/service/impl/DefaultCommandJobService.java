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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.obiba.magma.audit.UserProvider;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link CommandJobService}.
 */
@Component
public class DefaultCommandJobService implements CommandJobService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultCommandJobService.class);

  //
  // Instance Variables
  //

  @Autowired
  private UserProvider userProvider;

  private Executor executor;

  private boolean isRunning;

  private int lastJobId;

  private Object jobIdLock;

  /**
   * Jobs submitted for execution, but not yet executed.
   */
  private BlockingQueue<Runnable> jobsNotStarted;

  /**
   * Jobs in the process of being executed.
   */
  private List<FutureCommandJob> jobsStarted;

  /**
   * Jobs that have terminated.
   */
  private List<FutureCommandJob> jobsTerminated;

  /**
   * Comparator for sorting jobs in order of submit time, most recent first.
   */
  private Comparator<CommandJob> jobComparator;

  //
  // Constructors
  //

  public DefaultCommandJobService() {
    jobIdLock = new Object();
    jobComparator = new CommandJobComparator();

    jobsNotStarted = new LinkedBlockingQueue<Runnable>(); // thread-safe
    jobsStarted = Collections.synchronizedList(new ArrayList<FutureCommandJob>());
    jobsTerminated = Collections.synchronizedList(new ArrayList<FutureCommandJob>());

    executor = createExecutor();
  }

  //
  // Service Methods
  //

  public void start() {
    isRunning = true;
  }

  public void stop() {
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  //
  // CommandJobService Methods
  //

  public Integer launchCommand(CommandJob commandJob) {
    Integer id = nextJobId();

    commandJob.setId(id);

    // TODO: Temporarily setting the owner to "Unknown." Previously, the owner was set to the value of
    // userProvider.getUsername(). However, this causes an UnavailableSecurityManagerException if no user
    // is logged in (this could happen when a command is launched by a Quartz trigger at a time when no
    // user has logged into Opal).
    commandJob.setOwner("Unknown");

    commandJob.setSubmitTime(getCurrentTime());

    FutureCommandJob futureCommandJob = new FutureCommandJob(commandJob);
    executor.execute(futureCommandJob);

    return id;
  }

  public CommandJob getCommand(Integer id) {
    for(CommandJob job : getHistory()) {
      if(job.getId().equals(id)) {
        return job;
      }
    }
    return null;
  }

  public List<CommandJob> getHistory() {
    List<CommandJob> allJobs = new ArrayList<CommandJob>();

    for(FutureCommandJob futureCommandJob : getFutureCommandJobs()) {
      allJobs.add(futureCommandJob.getCommandJob());
    }
    Collections.sort(allJobs, jobComparator);

    return allJobs;
  }

  public void cancelCommand(Integer id) {
    for(FutureCommandJob futureCommandJob : getFutureCommandJobs()) {
      CommandJob job = futureCommandJob.getCommandJob();
      if(job.getId().equals(id)) {
        if(!isCancellable(job)) {
          log.info("CommandJob {} is not cancellable (current status: {})", id, job.getStatus());
          throw new IllegalStateException("commandJob not cancellable");
        }
        job.setStatus(Status.CANCEL_PENDING);
        futureCommandJob.cancel(true);
        return;
      }
    }
    throw new NoSuchCommandJobException(id);
  }

  public void deleteCommand(Integer id) {
    for(FutureCommandJob futureCommandJob : getFutureCommandJobs()) {
      CommandJob job = futureCommandJob.getCommandJob();
      if(job.getId().equals(id)) {
        if(!isDeletable(job)) {
          log.info("CommandJob {} is not deletable (current status: {})", id, job.getStatus());
          throw new IllegalStateException("commandJob not deletable");
        }
        getTerminatedJobs().remove(futureCommandJob);
        return;
      }
    }
    throw new NoSuchCommandJobException(id);
  }

  public void deleteCompletedCommands() {
    for(FutureCommandJob futureCommandJob : getFutureCommandJobs()) {
      CommandJob job = futureCommandJob.getCommandJob();
      if(isDeletable(job)) {
        getTerminatedJobs().remove(futureCommandJob);
      }
    }
  }

  //
  // Methods
  //

  public void setExecutor(Executor executor) {
    this.executor = executor;
  }

  public void setUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
  }

  protected Executor createExecutor() {
    return new ThreadPoolExecutor(10, 10, 0, TimeUnit.MILLISECONDS, jobsNotStarted) {
      @Override
      protected void beforeExecute(Thread t, Runnable r) {
        getStartedJobs().add((FutureCommandJob) r);
      }

      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        getStartedJobs().remove(r);
        getTerminatedJobs().add((FutureCommandJob) r);
      }
    };
  }

  /**
   * Generates an id for a {@link CommandJob}.
   * 
   * The sequence 1, 2, 3, ..., is returned.
   * 
   * @return an id for a {@link CommandJob}
   */
  protected Integer nextJobId() {
    synchronized(jobIdLock) {
      return ++lastJobId;
    }
  }

  protected Date getCurrentTime() {
    return new Date();
  }

  protected List<FutureCommandJob> getFutureCommandJobs() {
    List<FutureCommandJob> allFutureCommandJobs = new ArrayList<FutureCommandJob>();

    synchronized(this) {
      for(Runnable runnable : getNotStartedJobs()) {
        allFutureCommandJobs.add((FutureCommandJob) runnable);
      }

      for(FutureCommandJob futureCommandJob : getStartedJobs()) {
        allFutureCommandJobs.add(futureCommandJob);
      }

      for(FutureCommandJob futureCommandJob : getTerminatedJobs()) {
        allFutureCommandJobs.add(futureCommandJob);
      }
    }

    return allFutureCommandJobs;
  }

  BlockingQueue<Runnable> getNotStartedJobs() {
    return jobsNotStarted;
  }

  List<FutureCommandJob> getStartedJobs() {
    return jobsStarted;
  }

  List<FutureCommandJob> getTerminatedJobs() {
    return jobsTerminated;
  }

  public boolean isDeletable(CommandJob commandJob) {
    switch(commandJob.getStatus()) {
    case SUCCEEDED:
    case FAILED:
    case CANCELED:
      return true;
    default:
      return false;
    }
  }

  private boolean isCancellable(CommandJob commandJob) {
    return commandJob.getStatus().equals(Status.NOT_STARTED) || commandJob.getStatus().equals(Status.IN_PROGRESS);
  }

  //
  // Inner Classes
  //

  static class FutureCommandJob extends FutureTask<Object> {

    private CommandJob commandJob;

    public FutureCommandJob(CommandJob commandJob) {
      super(commandJob, null);

      this.commandJob = commandJob;
    }

    public CommandJob getCommandJob() {
      return commandJob;
    }
  }

  static class CommandJobComparator implements Comparator<CommandJob>, Serializable {

    private static final long serialVersionUID = 1L;

    public int compare(CommandJob o1, CommandJob o2) {
      if(o1.getSubmitTime().after(o2.getSubmitTime())) {
        return -1;
      } else if(o1.getSubmitTime().before(o2.getSubmitTime())) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
