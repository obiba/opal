package org.obiba.opal.r.spawner;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.rock.RockSession;
import org.obiba.opal.spi.r.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class RockPodSession extends RockSession {

  private static final Logger log = LoggerFactory.getLogger(RockPodSession.class);

  private final RockPod pod;

  protected RockPodSession(String serverName, RockPod pod, App app, AppCredentials credentials, String user, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, pod.getName(), app, credentials, user, transactionalThreadFactory, eventBus);
    this.pod = pod;
    openSession();
  }

  @Override
  public void close() {
    if (isClosed()) return;
    super.close();
    // terminate pod
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getAppResourceUrl(String.format("/pod/%s", pod.getName())));
    } catch (RestClientException e) {
      log.error("Error when reading R server state", e);
    }
  }

  @Override
  protected String getRSessionsResourceUrl() {
    return String.format("%s/r/sessions/", getServerUrl());
  }

  @Override
  protected String getRSessionResourceUrl(String path) {
    return String.format("%s/r/session/%s%s", getServerUrl(), getRockSessionId(), path);
  }

  private String getServerUrl() {
    if (Strings.isNullOrEmpty(pod.getService_ip())) {
      return String.format("http://%s:%s", pod.getIp(), pod.getPort());
    }
    return String.format("http://%s:%s", pod.getService_ip(), pod.getService_port());
  }

  private String getAppResourceUrl(String path) {
    return getApp().getServer() + path;
  }
}
