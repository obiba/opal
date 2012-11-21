/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.r.service;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gets connection to the R server.
 */
@Component
public class OpalRService implements Service, ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(OpalRService.class);

  private String host;

  private Integer port;

  private String username;

  private String password;

  private String encoding;

  @Autowired
  public OpalRService(@Value("${org.obiba.opal.Rserve.host}") String host,
      @Value("${org.obiba.opal.Rserve.port}") Integer port, @Value("${org.obiba.opal.Rserve.username}") String username,
      @Value("${org.obiba.opal.Rserve.password}") String password,
      @Value("${org.obiba.opal.Rserve.encoding}") String encoding) {
    super();
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.encoding = encoding;
  }

  /**
   * Creates a new connection to R server.
   *
   * @return
   * @throws RserveException
   */
  public RConnection newConnection() {
    RConnection conn;

    try {
      if(host.trim().length() > 0) {
        if(port != null) {
          conn = new RConnection(host.trim(), port.intValue());
        } else {
          conn = new RConnection(host.trim());
        }
      } else {
        conn = new RConnection();
      }

      if(conn.needLogin()) {
        conn.login(username, password);
      }

      if(encoding != null) {
        conn.setStringEncoding(encoding);
      }
    } catch(RserveException e) {
      log.error("Error while connecting to R.", e);
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
  public void execute(ROperation rop) {
    RConnection connection = newConnection();
    try {
      rop.doWithConnection(connection);
    } finally {
      connection.close();
    }
  }

  @Override
  public void execute(Iterable<ROperation> rops) {
    RConnection connection = newConnection();
    try {
      for(ROperation rop : rops) {
        rop.doWithConnection(connection);
      }
    } finally {
      connection.close();
    }
  }

  //
  // Service methods
  //

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }
}
