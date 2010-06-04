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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.obiba.magma.audit.UserProvider;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link CommandJobService}.
 */
public class DefaultCommandJobService implements CommandJobService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(DefaultCommandJobService.class);

  //
  // Instance Variables
  //

  private ExecutorService executor;

  private UserProvider userProvider;

  private boolean isRunning;

  private List<FutureCommandJob> history;

  private long lastJobId;

  private Object jobIdLock;

  //
  // Constructors
  //

  public DefaultCommandJobService() {
    history = new ArrayList<FutureCommandJob>();

    // TODO: Inject this dependency.
    executor = Executors.newFixedThreadPool(10);

    jobIdLock = new Object();
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

  public synchronized Long launchCommand(CommandJob commandJob) {
    Long id = nextJobId();

    commandJob.setId(id);
    commandJob.setOwner(userProvider.getUsername());
    commandJob.setSubmitTime(getCurrentTime());

    FutureCommandJob futureCommandJob = new FutureCommandJob(commandJob);
    executor.execute(futureCommandJob);

    history.add(0, futureCommandJob);

    return id;
  }

  public CommandJob getCommand(Long id) {
    for(CommandJob job : getHistory()) {
      if(job.getId().equals(id)) {
        return job;
      }
    }
    return null;
  }

  public synchronized List<CommandJob> getHistory() {
    List<CommandJob> commandJobList = new ArrayList<CommandJob>();
    for(FutureCommandJob futureCommandJob : history) {
      commandJobList.add(futureCommandJob.getCommandJob());
    }

    return commandJobList;
  }

  public synchronized void cancelCommand(Long id) {
    for(int i = 0; i < history.size(); i++) {
      FutureCommandJob futureCommandJob = history.get(i);
      CommandJob job = futureCommandJob.getCommandJob();
      if(job.getId().equals(id)) {
        if(!isCancellable(job)) {
          log.info("CommandJob {} is not cancellable (current status: {})", id, job.getStatus());
          throw new IllegalStateException("commandJob is finished");
        }
        job.setStatus(Status.CANCEL_PENDING);
        futureCommandJob.cancel(true);
        return;
      }
    }
    throw new NoSuchCommandJobException(id);

  }

  public synchronized void deleteCommand(Long id) {
    for(int i = 0; i < history.size(); i++) {
      CommandJob job = history.get(i).getCommandJob();
      if(job.getId().equals(id)) {
        if(!isDeletable(job)) {
          log.info("CommandJob {} is not deletable (current status: {})", id, job.getStatus());
          throw new IllegalStateException("commandJob is running");
        }
        history.remove(i);
        return;
      }
    }
    throw new NoSuchCommandJobException(id);
  }

  //
  // Methods
  //

  public void setExecutorService(ExecutorService executor) {
    this.executor = executor;
  }

  public void setUserProvider(UserProvider userProvider) {
    this.userProvider = userProvider;
  }

  /**
   * Generates an id for a {@link CommandJob}.
   * 
   * The sequence 1, 2, 3, ..., is returned.
   * 
   * @return an id for a {@link CommandJob}
   */
  protected Long nextJobId() {
    synchronized(jobIdLock) {
      return ++lastJobId;
    }
  }

  protected Date getCurrentTime() {
    return new Date();
  }

  private boolean isDeletable(CommandJob commandJob) {
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
}
