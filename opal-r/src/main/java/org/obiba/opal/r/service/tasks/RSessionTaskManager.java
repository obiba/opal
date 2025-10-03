package org.obiba.opal.r.service.tasks;

import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.RServerManagerService;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RSessionTaskManager implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(RSessionTaskManager.class);

  protected final TransactionalThreadFactory transactionalThreadFactory;

  private final RServerManagerService rServerManagerService;

  /**
   * All R session tasks.
   */
  private final List<RSessionTask> tasksList = Collections.synchronizedList(new LinkedList<>());

  @Autowired
  public RSessionTaskManager(TransactionalThreadFactory transactionalThreadFactory, RServerManagerService rServerManagerService) {
    this.transactionalThreadFactory = transactionalThreadFactory;
    this.rServerManagerService = rServerManagerService;
  }

  public RServerSession makeRSession(String principal, RServerProfile profile, SubjectRSessions rSessions) throws RServerException {
    RSessionCreationTask task = buildCreationTask(principal, profile, rSessions);
    tasksList.add(task);
    return makeRSession(task);
  }

  public RSessionTask getTask(String id) {
    return tasksList.stream().filter((tsk) -> tsk.getId().equals(id)).findFirst().orElseThrow(() -> new NoSuchElementException(id));
  }

  @Override
  public void destroy() {
    tasksList.clear();
  }

  private RSessionCreationTask buildCreationTask(String principal, RServerProfile profile, SubjectRSessions rSessions) {
    return new RSessionCreationTask(UUID.randomUUID().toString(), principal, profile, rSessions);
  }

  private RServerSession makeRSession(RSessionCreationTask task) throws RServerException {
    RServerService service = rServerManagerService.getRServer(task.getProfile().getCluster());
    RServerSession rSession = service.newRServerSession(task.getPrincipal(), task.getId());
    rSession.setProfile(task.getProfile());
    task.getRSessions().addRSession(rSession);
    task.completed();
    return rSession;
  }

}
