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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.InstallLocalPackageOperation;
import org.obiba.opal.r.service.*;
import org.obiba.opal.r.service.event.RServerServiceStartedEvent;
import org.obiba.opal.r.service.event.RServerServiceStoppedEvent;
import org.obiba.opal.spi.r.*;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.obiba.opal.web.r.NoSuchRPackageException;
import org.obiba.opal.web.r.RPackageResourceHelper;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  @Autowired
  private EventBus eventBus;

  @Autowired
  private OpalFileSystemService opalFileSystemService;

  private final List<RserveSession> sessions = Collections.synchronizedList(Lists.newArrayList());

  //
  // ROperationTemplate methods
  //

  /**
   * Creates a new R connection, do the operation with it and closes the R connection when done.
   */
  @Override
  public synchronized void execute(ROperation rop) {
    String user = "opal/system";
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
      eventBus.post(new RServerServiceStartedEvent("default", getName()));
    } catch (RestClientException e) {
      log.warn("Error when starting legacy R server: " + e.getMessage());
    }
  }

  @Override
  public void stop() {
    try {
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(getRServerResourceUrl());
      eventBus.post(new RServerServiceStoppedEvent("default", getName()));
    } catch (RestClientException e) {
      log.warn("Error when stopping legacy R server: " + e.getMessage());
    }
  }

  @Override
  public RServerSession newRServerSession(String user) {
    RServerConnection rConnection = newConnection();
    RserveSession session = new RserveSession(getName(), (RserveConnection) rConnection, transactionalThreadFactory,
        Strings.isNullOrEmpty(user) ? "opal/system" : user, eventBus);
    session.setProfile(new RServerProfile() {
      @Override
      public String getName() {
        return RServerManagerService.DEFAULT_CLUSTER_NAME;
      }

      @Override
      public String getCluster() {
        return RServerManagerService.DEFAULT_CLUSTER_NAME;
      }
    });
    sessions.add(session);
    return session;
  }

  @Override
  public String getName() {
    return RSERVE_NAME;
  }

  @Override
  public RServerState getState() {
    try {
      return getStateInternal();
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.error("Cannot get legacy R server state", e);
      else
        log.error("Cannot get legacy R server state: {}", e.getMessage());
      return new RserveState();
    }
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
          .map(new RPackageResourceHelper.StringsToRPackageDto(RServerManagerService.DEFAULT_CLUSTER_NAME, getName(), matrix))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Error when reading installed packages", e);
    }
    return pkgs;
  }

  @Override
  public List<OpalR.RPackageDto> getInstalledPackageDto(String name) {
    List<OpalR.RPackageDto> pkgs = getInstalledPackagesDtos().stream()
        .filter(dto -> dto.getName().equals(name))
        .collect(Collectors.toList());
    if (pkgs.isEmpty())
      throw new NoSuchRPackageException(name);
    return pkgs;
  }

  @Override
  public Map<String, List<Opal.EntryDto>> getDataShieldPackagesProperties() {
    Map<String, List<Opal.EntryDto>> dsPackages = Maps.newHashMap();
    try {
      DataSHIELDPackagesROperation rop = new DataSHIELDPackagesROperation();
      execute(rop);
      RNamedList<RServerResult> dsList = rop.getResult().asNamedList();
      for (Object name : dsList.getNames()) {
        dsPackages.put(name.toString(), getDataShieldPackagePropertiesDtos(dsList.get(name.toString()).asNamedList()));
      }
    } catch (Exception e) {
      log.error("DataShield packages properties extraction failed", e);
    }
    return dsPackages;
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
    execute(String.format("remotes::install_github('%s', ref='%s', upgrade=FALSE)", name, ref));
  }

  @Override
  public void installBioconductorPackage(String name) {
    ensureCRANPackage("BiocManager");
    execute(String.format("BiocManager::install('%s', ask = FALSE)", name));
  }

  @Override
  public void installLocalPackage(String path) {
    InstallLocalPackageOperation rop = new InstallLocalPackageOperation(opalFileSystemService.getFileSystem().resolveLocalFile(path));
    execute(rop);
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

  private List<Opal.EntryDto> getDataShieldPackagePropertiesDtos(RNamedList<RServerResult> properties) {
    List<Opal.EntryDto> entries = Lists.newArrayList();
    if (properties == null) return entries;
    for (String property : properties.getNames()) {
      Opal.EntryDto.Builder builder = Opal.EntryDto.newBuilder();
      builder.setKey(property);
      builder.setValue(Joiner.on(", ").join(properties.get(property).asStrings()));
      entries.add(builder.build());
    }
    return entries;
  }

  private RserveState getStateInternal() {
    RestTemplate restTemplate = new RestTemplate();
    RserveState state = restTemplate.getForObject(getRServerResourceUrl(), RserveState.class);
    state.setRSessionCount(getSessions().size());
    state.setBusyRSessionCount(getBusySessions().size());
    return state;
  }

  private ROperationWithResult getInstalledPackages() {
    String fieldStr = Joiner.on("','").join(defaultFields);
    String cmd = "installed.packages(fields=c('" + fieldStr + "'))";
    RScriptROperation rop = new RScriptROperation(cmd, RSerialize.NATIVE);
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

  public boolean isServiceAvailable() {
    try {
      getStateInternal();
      return true;
    } catch (Exception e) {
      // not functional
      return false;
    }
  }

  private static class DataSHIELDPackagesROperation extends AbstractROperationWithResult {

    private static final String DATASHIELD_FIND_SCRIPT = ".datashield.find.R";

    @Override
    protected void doWithConnection() {
      setResult(null);
      try (InputStream is = new ClassPathResource(DATASHIELD_FIND_SCRIPT).getInputStream();) {
        writeFile(DATASHIELD_FIND_SCRIPT, is);
        eval(String.format("base::source('%s')", DATASHIELD_FIND_SCRIPT));
      } catch (IOException | RServerException e) {
        throw new RRuntimeException(e);
      }
      setResult(eval(".datashield.find()", RSerialize.NATIVE));
    }
  }

}
