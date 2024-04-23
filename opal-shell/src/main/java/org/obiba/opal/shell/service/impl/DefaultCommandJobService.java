/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.service.impl;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.security.SessionDetachedSubject;
import org.obiba.opal.shell.CommandJob;
import org.obiba.opal.shell.service.CommandJobService;
import org.obiba.opal.shell.service.NoSuchCommandJobException;
import org.obiba.opal.web.model.Commands.CommandStateDto.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private Executor executor;

  private final Map<String, Executor> projectExecutors;

  private boolean isRunning;

  private final AtomicInteger lastJobId;

  /**
   * Jobs submitted for execution, but not yet executed.
   */
  private final BlockingQueue<Runnable> jobsNotStarted;

  private final Map<String, BlockingQueue<Runnable>> projectJobsNotStarted;

  /**
   * Jobs in the process of being executed.
   */
  private final List<FutureCommandJob> jobsStarted;

  /**
   * Jobs that have terminated.
   */
  private final List<FutureCommandJob> jobsTerminated;

  /**
   * Comparator for sorting jobs in order of submit time, most recent first.
   */
  private final Comparator<CommandJob> jobComparator;

  //
  // Constructors
  //

  public DefaultCommandJobService() {
    lastJobId = new AtomicInteger();
    jobComparator = new CommandJobComparator();

    jobsNotStarted = new LinkedBlockingQueue<>(); // thread-safe
    jobsStarted = Collections.synchronizedList(new ArrayList<FutureCommandJob>());
    jobsTerminated = Collections.synchronizedList(new ArrayList<FutureCommandJob>());

    executor = createExecutor(jobsNotStarted);

    projectJobsNotStarted = Maps.newConcurrentMap();
    projectExecutors = Maps.newConcurrentMap();
  }

  //
  // Service Methods
  //

  @Override
  public void start() {
    isRunning = true;
  }

  @Override
  public void stop() {
    isRunning = false;
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public String getName() {
    return "task";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  //
  // CommandJobService Methods
  //

  @Override
  public CommandJob launchCommand(CommandJob commandJob, Subject originalOwner) {
    Subject owner = SessionDetachedSubject.asSessionDetachedSubject(originalOwner);
    commandJob.setId(nextJobId());
    commandJob.setOwner(owner.getPrincipal().toString());
    commandJob.setSubmitTime(getCurrentTime());

    if (commandJob.hasProject()) {
      return launchProjectCommand(commandJob, owner);
    } else {
      executor.execute(new FutureCommandJob(owner, commandJob));
      return commandJob;
    }
  }

  private CommandJob launchProjectCommand(CommandJob commandJob, Subject owner) {
    if (!projectJobsNotStarted.containsKey(commandJob.getProject())) {
      projectJobsNotStarted.put(commandJob.getProject(), new LinkedBlockingQueue<>());
      projectExecutors.put(commandJob.getProject(), createExecutor(projectJobsNotStarted.get(commandJob.getProject())));
    }
    projectExecutors.get(commandJob.getProject()).execute(new FutureCommandJob(owner, commandJob));
    return commandJob;
  }

  @Override
  public CommandJob launchCommand(CommandJob commandJob) {
    return launchCommand(commandJob, SecurityUtils.getSubject());
  }

  @Override
  public CommandJob getCommand(Integer id) {
    for(CommandJob job : getHistory()) {
      if(job.getId().equals(id)) {
        return job;
      }
    }
    return null;
  }

  @Override
  public List<CommandJob> getHistory() {
    List<CommandJob> allJobs = new ArrayList<>();

    for(FutureCommandJob futureCommandJob : getFutureCommandJobs()) {
      allJobs.add(futureCommandJob.getCommandJob());
    }
    Collections.sort(allJobs, jobComparator);

    return allJobs;
  }

  @Override
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

  @Override
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

  @Override
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

  protected Executor createExecutor(BlockingQueue<Runnable> queue) {
    return new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, queue) {
      @Override
      protected void beforeExecute(Thread t, Runnable r) {
        log.info("Starting task {}", ((FutureCommandJob) r).commandJob.getId());
        t.setPriority(Thread.MIN_PRIORITY);
        getStartedJobs().add((FutureCommandJob) r);
      }

      @Override
      protected void afterExecute(Runnable r, Throwable t) {
        log.info("CommandJob {} finished executing", ((FutureCommandJob) r).commandJob.getId());
        if(t != null) {
          log.warn("CommandJob {} threw an exception: {}", t.getMessage());
        }
        getStartedJobs().remove(r);
        getTerminatedJobs().add((FutureCommandJob) r);
      }
    };
  }

  /**
   * Generates an id for a {@link CommandJob}.
   * <p/>
   * The sequence 1, 2, 3, ..., is returned.
   *
   * @return an id for a {@link CommandJob}
   */
  protected Integer nextJobId() {
    return lastJobId.incrementAndGet();
  }

  protected Date getCurrentTime() {
    return new Date();
  }

  protected List<FutureCommandJob> getFutureCommandJobs() {
    List<FutureCommandJob> allFutureCommandJobs = new ArrayList<>();

    synchronized(this) {
      for(Runnable runnable : getNotStartedJobs()) {
        allFutureCommandJobs.add((FutureCommandJob) runnable);
      }
      for (String project : projectJobsNotStarted.keySet()) {
        for (Runnable runnable : projectJobsNotStarted.get(project)) {
          allFutureCommandJobs.add((FutureCommandJob) runnable);
        }
      }
      allFutureCommandJobs.addAll(getStartedJobs());
      allFutureCommandJobs.addAll(getTerminatedJobs());
    }

    return allFutureCommandJobs.stream()
        .filter(job -> isCommandJobReadable(job.getCommandJob()))
        .collect(Collectors.toList());
  }

  private boolean isCommandJobReadable(CommandJob job) {
    Subject subject = SecurityUtils.getSubject();
    if (job.hasProject()) {
      // case no access to project
      if (!subject.isPermitted("rest:/project/" + job.getProject() + ":GET")) return false;
      // project admin can see all about the project
      if (subject.isPermitted("rest:/project/" + job.getProject() + ":POST")) return true;
      // project regular user can only see own jobs
      return subject.getPrincipal().toString().equals(job.getOwner());
    } else {
      // no project context, only for admins
      return subject.isPermitted("rest:/" + ":POST");
    }
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
    return commandJob.getStatus() == Status.NOT_STARTED || commandJob.getStatus() == Status.IN_PROGRESS;
  }

  //
  // Inner Classes
  //

  static class FutureCommandJob extends FutureTask<Object> {

    private final CommandJob commandJob;

    FutureCommandJob(Subject subject, CommandJob commandJob) {
      super(subject.associateWith(commandJob), null);
      this.commandJob = commandJob;
    }

    public CommandJob getCommandJob() {
      return commandJob;
    }
  }

  static class CommandJobComparator implements Comparator<CommandJob>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(CommandJob o1, CommandJob o2) {
      if(o1.getSubmitTime().after(o2.getSubmitTime())) {
        return -1;
      }
      return o1.getSubmitTime().before(o2.getSubmitTime()) ? 1 : 0;
    }
  }

}
