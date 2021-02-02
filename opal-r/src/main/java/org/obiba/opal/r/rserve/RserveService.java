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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.AbstractRServerSession;
import org.obiba.opal.r.service.RServerService;
import org.obiba.opal.r.service.RServerSession;
import org.obiba.opal.r.service.RServerState;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gets connection to the legacy R server.
 */
@Component
public class RserveService implements RServerService, ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(RserveService.class);

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

  private final List<RserveSession> sessions = Collections.synchronizedList(Lists.newArrayList());

  //
  // ROperationTemplate methods
  //

  /**
   * Creates a new R connection, do the operation with it and closes the R connection when done.
   */
  @Override
  public synchronized void execute(ROperation rop) {
    RServerSession rSession = newRServerSession(SecurityUtils.getSubject().getPrincipal().toString());
    try {
      rSession.execute(rop);
    } finally {
      rSession.close();
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
      log.warn("Error when checking legacy R server: " + e.getMessage());
    }
    return false;
  }

  @Override
  public void start() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.put(getRServerResourceUrl(), null);
    } catch (RestClientException e) {
      log.warn("Error when starting legacy R server: " + e.getMessage());
    }
  }

  @Override
  public void stop() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getRServerResourceUrl());
    } catch (RestClientException e) {
      log.warn("Error when stopping legacy R server: " + e.getMessage());
    }
  }

  @Override
  public RServerSession newRServerSession(String user) {
    RServerConnection rConnection = newConnection();
    RserveSession session = new RserveSession((RserveConnection) rConnection, transactionalThreadFactory, user);
    sessions.add(session);
    return session;
  }

  @Override
  public String getName() {
    return "r";
  }

  @Override
  public RServerState getState() {
    RestTemplate restTemplate = new RestTemplate();
    RserveState state = restTemplate.getForObject(getRServerResourceUrl(), RserveState.class);
    state.setRSessionCount(getSessions().size());
    state.setBusyRSessionCount(getBusySessions().size());
    return state;
  }

  @Override
  public App getApp() {
    return null;
  }

  @Override
  public boolean isFor(App app) {
    return false;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackagesDtos() {
    List<OpalR.RPackageDto> pkgs = Lists.newArrayList();
    try {
      ROperationWithResult rop = getInstalledPackages();
      RMatrix<String> matrix = rop.getResult().asStringMatrix();
      pkgs = matrix.iterateRows().stream()
          .map(new RPackageResourceHelper.StringsToRPackageDto(matrix))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    }
    return pkgs;
  }

  @Override
  public void removePackage(String name) {
    execute(String.format("remove.packages('%s')", name));
  }

  @Override
  public void ensureCRANPackage(String name) {
    execute(String.format("if (!require(%s)) { install.packages('%s') }", name, name));
  }

  @Override
  public void installCRANPackage(String name) {
    execute(String.format("install.packages('%s')", name));
  }

  @Override
  public void installGitHubPackage(String name, String ref) {
    ensureCRANPackage("remotes");
    execute(String.format("remotes::install_github('%s', ref='%s', upgrade=TRUE)", name, ref));
  }

  @Override
  public void installBioconductorPackage(String name) {
    ensureCRANPackage("BiocManager");
    execute(String.format("BiocManager::install('%s', ask = FALSE)", name));
  }

  //
  // Private methods
  //

  private ROperationWithResult getInstalledPackages() {
    String fieldStr = Joiner.on("','").join(defaultFields);
    String cmd = "installed.packages(fields=c('" + fieldStr + "'))";
    RScriptROperation rop = new RScriptROperation(cmd, false);
    execute(rop);
    return rop;
  }

  private List<RserveSession> getSessions() {
    List<RserveSession> closedSessions = sessions.stream()
        .filter(RserveSession::isClosed).collect(Collectors.toList());
    sessions.removeAll(closedSessions);
    return sessions;
  }

  private List<RserveSession> getBusySessions() {
    List<RserveSession> closedSessions = sessions.stream()
        .filter(RserveSession::isClosed).collect(Collectors.toList());
    sessions.removeAll(closedSessions);
    return sessions.stream().filter(AbstractRServerSession::isBusy).collect(Collectors.toList());
  }

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

  private void execute(String cmd) {
    RScriptROperation rop = new RScriptROperation(cmd, false);
    execute(rop);
  }

}
