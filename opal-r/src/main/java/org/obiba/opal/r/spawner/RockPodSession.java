package org.obiba.opal.r.spawner;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.rock.RockSession;
import org.obiba.opal.r.service.AbstractRServerSession;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.spi.r.*;

import java.io.InputStream;
import java.io.OutputStream;

public class RockPodSession extends RockSession implements RServerSession, RServerConnection {

  private final RockPod pod;

  protected RockPodSession(String serverName, RockPod pod, App app, AppCredentials credentials, String user, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, pod.getName(), app, credentials, user, transactionalThreadFactory, eventBus);
    this.pod = pod;
    openSession();
  }

  @Override
  public void close() {
    super.close();
    // TODO terminate pod
  }

  protected String getRSessionsResourceUrl() {
    return String.format("%s/r/sessions", getServerUrl());
  }

  protected String getRSessionResourceUrl(String path) {
    return String.format("%s/r/session/%s%s", getServerUrl(), getRockSessionId(), path);
  }

  private String getServerUrl() {
    if (Strings.isNullOrEmpty(pod.getService_ip())) {
      return String.format("http://%s:%s", pod.getIp(), pod.getPort());
    }
    return String.format("http://%s:%s", pod.getService_ip(), pod.getService_port());
  }
}
