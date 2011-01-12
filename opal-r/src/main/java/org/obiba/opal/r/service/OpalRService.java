package org.obiba.opal.r.service;

import org.obiba.opal.core.runtime.Service;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpalRService implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalRService.class);

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
  // Service methods
  //

  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public void start() {
    log.info("start R service: host={} port={} username={} password={} encoding={}", new Object[] { host, port, username, password, encoding });
  }

  @Override
  public void stop() {

  }

}
