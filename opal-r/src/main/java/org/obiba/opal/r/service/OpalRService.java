/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.service;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.service.event.RServiceStartedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.spi.r.ROperation;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.RRuntimeException;
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
public class OpalRService implements Service, ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(OpalRService.class);

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
  private EventBus eventBus;

  /**
   * Creates a new connection to R server.
   *
   * @return
   * @throws RserveException
   */
  public RConnection newConnection() {
    RConnection conn;

    try {
      conn = newRConnection();
    } catch(RserveException e) {
      log.error("Error while connecting to R ({}:{}): {}", getHost(), getPort(), e.getMessage());
      throw new RRuntimeException(e);
    }

    return conn;
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Creates a new R connection, do the operation with it and closes the R connection when done.
   */
  @Override
  public synchronized void execute(ROperation rop) {
    RConnection connection = newConnection();
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
      RServerState state = getRServerState();
      return state.isRunning();
    } catch(RestClientException e) {
      log.warn("Error when checking R server: " + e.getMessage());
    }
    return false;
  }

  @Override
  public void start() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.put(getRServerResourceUrl(), null);
    } catch(RestClientException e) {
      log.warn("Error when starting R server: " + e.getMessage());
    }
    eventBus.post(new RServiceStartedEvent());
  }

  @Override
  public void stop() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getRServerResourceUrl());
    } catch(RestClientException e) {
      log.warn("Error when stopping R server: " + e.getMessage());
    }
    eventBus.post(new RServiceStoppedEvent());
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  public String getRServerResourceUrl() {
    return "http://" + getHost() +":" + rServerPort + "/rserver";
  }

  private RServerState getRServerState() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRServerResourceUrl(), RServerState.class);
  }

  private String getHost() {
    return host.trim().isEmpty() ? "127.0.0.1" : host.trim();
  }

  private int getPort() {
    return port == null ? DEFAULT_RSERVE_PORT : port;
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

    if(conn.needLogin()) {
      conn.login(username, password);
    }

    if(encoding != null) {
      conn.setStringEncoding(encoding);
    }

    return conn;
  }

  public static class RServerState {

    private boolean isRunning;

    private Integer port;

    private String encoding;

    public void setRunning(boolean running) {
      isRunning = running;
    }

    public void setPort(Integer port) {
      this.port = port;
    }

    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    public boolean isRunning() {
      return isRunning;
    }

    public Integer getPort() {
      return port;
    }

    public String getEncoding() {
      return encoding;
    }
  }

}
