package org.obiba.opal.r.kubernetes;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.rock.RockSession;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class RockPodSession extends RockSession {

  private static final Logger log = LoggerFactory.getLogger(RockPodSession.class);

  private final RockPodSessionHelper rockPodSessionHelper;

  private PodRef pod;

  private boolean terminatePod = true;

  private CompletableFuture<Void> readyFuture;

  protected RockPodSession(String serverName, String id, PodRef pod, String user, RockPodSessionHelper rockPodSessionHelper, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, id, RockPodSessionHelper.DEFAULT_CREDENTIALS, user, transactionalThreadFactory, eventBus);
    this.pod = pod;
    this.rockPodSessionHelper = rockPodSessionHelper;
    init();
  }

  public void setTerminatePod(boolean terminatePod) {
    this.terminatePod = terminatePod;
  }

  @Override
  public void close() {
    if (isClosed()) return;
    super.close();
    readyFuture = null;
    // terminate pod
    if (terminatePod) rockPodSessionHelper.deletePod(pod);
  }

  public PodRef getPod() {
    return pod;
  }

  void setPod(PodRef pod) {
    this.pod = pod;
  }

  public PodRef getPodRef() {
    return rockPodSessionHelper.getPod(pod.getPodSpec(), pod.getName());
  }

  public void join() {
    if (readyFuture == null || readyFuture.isDone()) return;
    try {
      log.info("Joining pod init {}", pod.getName());
      readyFuture.join();
    } catch (Exception e) {
      log.warn("Waiting for RockPodSession has been closed", e);
    }
  }

  @Override
  protected String getRSessionsResourceUrl() {
    return String.format("%s/r/sessions", getServerUrl());
  }

  @Override
  protected String getRSessionResourceUrl(String path) {
    return String.format("%s/r/session/%s%s", getServerUrl(), getRockSessionId(), path);
  }

  private String getServerUrl() {
    return String.format("http://%s:%s", pod.getName(), pod.getPort());
  }

  private void init() {
    // Update pod state in a thread
    readyFuture = CompletableFuture.runAsync(new RServerReadyTask());
  }

  private final class RServerReadyTask implements Runnable {

    public RServerReadyTask() {}

    @Override
    public void run() {
      try {
        log.info("Pod running check");
        PodRef pod = rockPodSessionHelper.ensureRunningPod(getPod().getPodSpec(), getPod().getName(), podRef -> addEvent(podRef.getStatus()));
        setPod(pod);
        log.info("Rock app ready check");
        rockPodSessionHelper.ensureRServerReady(pod);
        log.info("Rock R session opening");
        openSession();
        log.info("Rock R session opened");
      } catch (Exception e) {
        log.error("Rock R session opening failed", e);
        setFailed(e.getMessage());
      }
    }
  }
}
