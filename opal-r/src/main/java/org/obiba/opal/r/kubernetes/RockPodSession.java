package org.obiba.opal.r.kubernetes;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.rock.RockSession;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RockPodSession extends RockSession {

  private static final Logger log = LoggerFactory.getLogger(RockPodSession.class);

  private final PodsService podsService;

  private final PodRef pod;

  protected RockPodSession(String serverName, PodRef pod, AppCredentials credentials, String user, PodsService podsService, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, pod.getName(), credentials, user, transactionalThreadFactory, eventBus);
    this.pod = pod;
    this.podsService = podsService;
    openSession();
  }

  @Override
  public void close() {
    if (isClosed()) return;
    super.close();
    // terminate pod
    podsService.deletePod(pod);
  }

  public PodRef getPodRef() {
    return pod;
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
}
