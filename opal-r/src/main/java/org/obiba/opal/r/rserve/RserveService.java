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
import org.obiba.opal.web.r.NoSuchRPackageException;
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

  public static final String RSERVE_NAME = "rserve";

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

  @Value("${org.obiba.opal.r.repos}")
  private String defaultRepos;

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
    String user = "opal";
    try {
      Object principal = SecurityUtils.getSubject().getPrincipal();
      if (principal != null) user = principal.toString();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.warn("Exception when retrieving subject", e);
      else
        log.warn("Exception when retrieving subject: {}", e.getMessage());
    }
    RServerSession rSession = newRServerSession(user);
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
    RserveSession session = new RserveSession(getName(), (RserveConnection) rConnection, transactionalThreadFactory, user);
    sessions.add(session);
    return session;
  }

  @Override
  public String getName() {
    return RSERVE_NAME;
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
  public OpalR.RPackageDto getInstalledPackageDto(String name) {
    return getInstalledPackagesDtos().stream().filter(dto -> dto.getName().equals(name))
        .findFirst().orElseThrow(() -> new NoSuchRPackageException(name));
  }

  @Override
  public List<String> getInstalledDataSHIELDPackageNames() {
    DataSHIELDPackagesROperation rop = new DataSHIELDPackagesROperation();
    execute(rop);
    return Lists.newArrayList(rop.getResult().asStrings());
  }

  @Override
  public void removePackage(String name) {
    execute(String.format("remove.packages('%s')", name));
  }

  @Override
  public void ensureCRANPackage(String name) {
    execute(String.format("if (!require(%s)) { install.packages('%s', repos=c('%s')) }", name, name, Joiner.on("','").join(getDefaultRepos())));
  }

  @Override
  public void installCRANPackage(String name) {
    execute(String.format("install.packages('%s', repos=c('%s'))", name, Joiner.on("','").join(getDefaultRepos())));
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

  @Override
  public void updateAllCRANPackages() {
    String cmd = ".libPaths()";
    RScriptROperation rop = new RScriptROperation(cmd, false);
    execute(rop);
    String libpath = rop.getResult().asStrings()[0];
    cmd = "getwd()";
    rop = new RScriptROperation(cmd, false);
    execute(cmd);
    log.info("getwd={}", rop.getResult().asStrings()[0]);
    String repos = Joiner.on("','").join(getDefaultRepos());
    cmd = String.format("update.packages(ask = FALSE, repos = c('%s'), instlib = '%s')", repos, libpath);
    execute(cmd);
  }

  @Override
  public String[] getLog(Integer nbLines) {
    String cmd;
    if (nbLines > 0)
      cmd = String.format("tail(readLines(con = file('../../../logs/Rserve.log', 'r')), %s)", nbLines);
    else
      cmd = "readLines(con = file('../../../logs/Rserve.log', 'r'))";
    RScriptROperation rop = new RScriptROperation(cmd, false);
    execute(rop);
    return rop.getResult().asStrings();
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

  private List<String> getDefaultRepos() {
    return Lists.newArrayList(defaultRepos.split(",")).stream().map(String::trim).collect(Collectors.toList());
  }

  private class DataSHIELDPackagesROperation extends AbstractROperationWithResult {

    public static final String AGGREGATE_METHODS = "AggregateMethods";

    public static final String ASSIGN_METHODS = "AssignMethods";

    public static final String OPTIONS = "Options";

    @Override
    protected void doWithConnection() {
      setResult(null);
      // DS fields
      eval(String.format("base::assign('dsFields', c('%s'))", Joiner.on("','").join(AGGREGATE_METHODS, ASSIGN_METHODS, OPTIONS)));
      // extract DS fields from DESCRIPTION files
      eval("assign('pkgs', Map(function(p) { x <- as.list(p) ; x[names(x) %in% dsFields] }, " +
          "         Filter(function(p) any(names(p) %in% dsFields), " +
          "                lapply(installed.packages()[,1], function(p) as.data.frame(read.dcf(system.file('DESCRIPTION', package=p)))))))");
      // extract DS fields from DATASHIELD files
      eval("assign('x', lapply(installed.packages()[,1], function(p) system.file('DATASHIELD', package=p)))");
      eval("assign('y', lapply(x[lapply(x, nchar)>0], function(f) as.list(as.data.frame(read.dcf(f)))))");
      // merge and prepare DS field values as arrays of strings
      eval("assign('pkgs', lapply(append(pkgs, y), function(p) lapply(p, function(pp)  gsub('^\\\\s+|\\\\s+$', '', gsub('\\n', '', unlist(strsplit(pp, ',')))))))");
      setResult(eval("pkgs", false));
    }
  }

}
