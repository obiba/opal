/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.rserve;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.r.service.event.RServiceStartedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RServerConnection;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Gets connection to the R server.
 */
@Component
public class RserveRService implements Service, RServerService, ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(RserveRService.class);

  public static final int DEFAULT_RSERVE_PORT = 6311;

  @Value("${org.obiba.rserver.port}")
  private Integer rServerPort;

  @Value("${org.obiba.opal.Rserve.host}")
  private String host;

  @Value("${org.obiba.opal.Rserve.port}")
  private Integer port;

  @Value("${org.obiba.opal.Rserve.username}")
  private String username;

  @Value("${org.obiba.opal.Rserve.password}")
  private String password;

  @Value("${org.obiba.opal.Rserve.encoding}")
  private String encoding;

  @Autowired
  private TransactionalThreadFactory transactionalThreadFactory;

  @Autowired
  private EventBus eventBus;

  //
  // ROperationTemplate methods
  //

  /**
   * Creates a new R connection, do the operation with it and closes the R connection when done.
   */
  @Override
  public synchronized void execute(ROperation rop) {
    RServerConnection connection = newConnection();
    try {
      rop.doWithConnection(connection);
    } finally {
      connection.close();
    }
  }

  //
  // Service methods
  //

  @Override
  public boolean isRunning() {
    try {
      RServerState state = getState();
      return state.isRunning();
    } catch (RestClientException e) {
      log.warn("Error when checking R server: " + e.getMessage());
    }
    return false;
  }

  @Override
  public void start() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.put(getRServerResourceUrl(), null);
    } catch (RestClientException e) {
      log.warn("Error when starting R server: " + e.getMessage());
    }
    eventBus.post(new RServiceStartedEvent());
  }

  @Override
  public void stop() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getRServerResourceUrl());
    } catch (RestClientException e) {
      log.warn("Error when stopping R server: " + e.getMessage());
    }
    eventBus.post(new RServiceStoppedEvent());
  }

  @Override
  public RServerSession newRServerSession(String user) {
    RServerConnection rConnection = newConnection();
    return new RserveSession((RserveConnection) rConnection, transactionalThreadFactory, user);
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  @Override
  public RServerState getState() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRServerResourceUrl(), RServerState.class);
  }

  //
  // Private methods
  //

  private String getRServerResourceUrl() {
    return "http://" + getHost() + ":" + rServerPort + "/rserver";
  }

  private String getHost() {
    return host.trim().isEmpty() ? "127.0.0.1" : host.trim();
  }

  private int getPort() {
    return port == null ? DEFAULT_RSERVE_PORT : port;
  }

  /**
   * Creates a new connection to R server.
   *
   * @return
   * @throws RserveException
   */
  private RServerConnection newConnection() {
    RConnection conn;

    try {
      conn = newRConnection();
    } catch (RserveException e) {
      log.error("Error while connecting to R ({}:{}): {}", getHost(), getPort(), e.getMessage());
      throw new RRuntimeException(e);
    }

    return new RserveConnection(conn);
  }

  /**
   * Create a new RConnection given the R server settings.
   *
   * @return
   * @throws RserveException
   */
  private RConnection newRConnection() throws RserveException {
    RConnection conn;

    conn = new RConnection(getHost(), getPort());

    if (conn.needLogin()) {
      conn.login(username, password);
    }

    if (encoding != null) {
      conn.setStringEncoding(encoding);
    }

    return conn;
  }

}
