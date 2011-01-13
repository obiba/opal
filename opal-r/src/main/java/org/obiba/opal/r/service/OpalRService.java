package org.obiba.opal.r.service;

import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gets connection to the R server.
 */
@Component
public class OpalRService implements Service, ROperationTemplate {

  private String host;

  private Integer port;

  private String username;

  private String password;

  private String encoding;

  @Autowired
  public OpalRService(@Value("${org.obiba.opal.Rserve.host}") String host, @Value("${org.obiba.opal.Rserve.port}") Integer port, @Value("${org.obiba.opal.Rserve.username}") String username, @Value("${org.obiba.opal.Rserve.password}") String password, @Value("${org.obiba.opal.Rserve.encoding}") String encoding) {
    super();
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.encoding = encoding;
  }

  /**
   * Creates a new connection to R server.
   * @return
   * @throws RserveException
   */
  public RConnection newConnection() throws RserveException {
    RConnection conn;

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
    try {
      RConnection connection = newConnection();
      rop.doWithConnection(connection);
      connection.close();
    } catch(RserveException e) {
      throw new RRuntimeException(e);
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

}
