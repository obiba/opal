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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.obiba.core.util.StringUtil;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.obiba.opal.r.RScriptROperation;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Gets connection to the R server.
 */
@Component
public class OpalRService implements Service, ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(OpalRService.class);

  @Value("${OPAL_HOME}")
  private File opalHomeFile;

  @Value("${org.obiba.opal.R.exec}")
  private String exec;

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

  private int rserveStatus = -1;

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
      // make sure the environment is correctly configured each time a connection is created
      ensureLibPaths(conn);
    } catch(RserveException e) {
      log.error("Error while connecting to R: {}", e.getMessage());
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
    return !isEnabled() || rserveStatus == 0;
  }

  @Override
  public void start() {
    if(!isEnabled() || rserveStatus == 0) return;

    // fresh start, try to kill any remains of R server
    try {
      newRConnection().shutdown();
    } catch(Exception e) {
      // ignore
    }

    try {
      // launch the Rserve daemon and wait for it to complete
      Process rserve = buildRProcess().start();
      rserveStatus = rserve.waitFor();
      if(rserveStatus == 0) {
        log.info("R server started");
      } else {
        log.error("R server start failed with status: {}", rserveStatus);
        rserveStatus = -1;
      }
    } catch(Exception e) {
      log.error("R server start failed", e);
      rserveStatus = -1;
    }
  }

  private ProcessBuilder buildRProcess() {
    List<String> args = getArguments();
    log.info("Starting R server: {}", StringUtil.collectionToString(args, " "));
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(getWorkingDirectory());
    pb.redirectErrorStream(true);
    pb.redirectOutput(ProcessBuilder.Redirect.appendTo(getRserveLog()));
    return pb;
  }

  @Override
  public void stop() {
    if(rserveStatus != 0) return;

    try {
      log.info("Shutting down R server...");
      newConnection().shutdown();
      log.info("R server shut down");
    } catch(Exception e) {
      log.error("R server shutdown failed", e);
    }
    rserveStatus = -1;
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  /**
   * True if the R server is controlled by Opal.
   *
   * @return
   */
  public boolean isEnabled() {
    return !Strings.isNullOrEmpty(exec) && (Strings.isNullOrEmpty(host) || "localhost".equals(host));
  }

  private List<String> getArguments() {
    List<String> args = Lists.newArrayList(exec, "CMD", "Rserve", "--vanilla");
    if(port != null && port > 0) {
      args.add("--RS-port");
      args.add(port.toString());
    }
    if(!Strings.isNullOrEmpty(encoding)) {
      args.add("--RS-encoding");
      args.add(encoding);
    }
    File workDir = getWorkingDirectory();
    args.add("--RS-workdir");
    args.add(workDir.getAbsolutePath());

    File conf = getRservConf();
    if(conf.exists()) {
      args.add("--RS-conf");
      args.add(conf.getAbsolutePath());
    }

    return args;
  }

  private File getWorkingDirectory() {
    File dir = new File(opalHomeFile, "work" + File.separator + "R");
    if(!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private File getLibDirectory() {
    File dir = new File(opalHomeFile, "data" + File.separator + "R" + File.separator + "library");
    if(!dir.exists()) {
      dir.mkdirs();
    }
    return dir;
  }

  private File getRserveLog() {
    File rserveLog = new File(opalHomeFile, "logs" + File.separator + "Rserve.log");
    if(!rserveLog.getParentFile().exists()) {
      rserveLog.getParentFile().mkdirs();
    }
    return rserveLog;
  }

  private File getRservConf() {
    return new File(opalHomeFile, "conf" + File.separator + "Rserv.conf");
  }

  /**
   * Create a new RConnection given the R server settings.
   * @return
   * @throws RserveException
   */
  private RConnection newRConnection() throws RserveException {
    RConnection conn;

    if(host.trim().length() > 0) {
      conn = port == null ? new RConnection(host.trim()) : new RConnection(host.trim(), port);
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

  /**
   * Set the lib paths in the given R connection. Applies only if the R server is locally managed.
   * @param conn
   */
  private void ensureLibPaths(RConnection conn) {
    if(isEnabled()) {
      String libDir = getLibDirectory().getAbsolutePath();
      try {
        new RScriptROperation(".libPaths(\"" + libDir + "\")").doWithConnection(conn);
      } catch(Exception e) {
        log.warn("Unable to set libPaths to '{}': {}", libDir, e.getMessage());
      }
    }
  }

}
