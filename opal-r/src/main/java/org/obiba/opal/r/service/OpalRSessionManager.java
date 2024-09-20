/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.shiro.SecurityUtils;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.r.service.event.RServerServiceStoppedEvent;
import org.obiba.opal.r.service.event.RServerSessionStartedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.spi.r.FileReadROperation;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RScriptROperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ForbiddenException;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  public static final String WORKSPACES_FORMAT = System.getenv().get("OPAL_HOME") + File.separatorChar + "data"
      + File.separatorChar + "R" + File.separatorChar + "workspaces" + File.separatorChar + "%s";

  private static final String R_IMAGE_FILE = ".RData";

  private static final String R_IMAGE_FILE_ENC = ".RData.enc";

  @Value("${org.obiba.opal.r.sessionTimeout}")
  private Long rSessionTimeout;

  @Value("${org.obiba.opal.r.sessionTimeout.R}")
  private Long rSessionTimeoutR;

  @Value("${org.obiba.opal.r.sessionTimeout.DataSHIELD}")
  private Long rSessionTimeoutDataSHIELD;

  @Value("${org.obiba.opal.r.sessionTimeout.Import}")
  private Long rSessionTimeoutImport;

  @Value("${org.obiba.opal.r.sessionTimeout.Export}")
  private Long rSessionTimeoutExport;

  @Value("${org.obiba.opal.r.sessionTimeout.Analyse}")
  private Long rSessionTimeoutAnalyse;

  @Value("${org.obiba.opal.r.sessionTimeout.SQL}")
  private Long rSessionTimeoutSQL;

  @Value("${org.obiba.opal.r.sessionTimeout.View}")
  private Long rSessionTimeoutView;

  @Value("${org.obiba.opal.r.workspaces.expires:365}")
  private long rWorkspaceExpires;

  @Autowired
  private RServerManagerService rServerManagerService;

  @Autowired
  private CryptoService cryptoService;

  private final Map<String, SubjectRSessions> rSessionMap = Maps.newConcurrentMap();

  @Override
  public void destroy() throws Exception {
    stop();
  }

  public void stop() {
    for (String principal : rSessionMap.keySet()) {
      doClearRSessions(principal);
    }
    rSessionMap.clear();
  }

  @Subscribe
  public void onRServiceStopped(RServiceStoppedEvent event) {
    try {
      stop();
    } catch (Exception e) {
      log.warn("Error while stopping R session manager", e);
    }
  }

  /**
   * Terminate the R sessions that are related to a R server in a cluster.
   *
   * @param event
   */
  @Subscribe
  public void onRServerServiceStopped(RServerServiceStoppedEvent event) {
    try {
      for (SubjectRSessions sessions : rSessionMap.values()) {
        sessions.removeRSessions(event.getCluster(), event.getName());
      }
    } catch (Exception e) {
      log.warn("Error while stopping R session manager", e);
    }
  }

  /**
   * Get the R sessions.
   *
   * @return
   */
  public List<RServerSession> getRSessions() {
    ImmutableList.Builder<RServerSession> builder = ImmutableList.builder();

    for (SubjectRSessions rSessions : rSessionMap.values()) {
      builder.addAll(rSessions);
    }

    return builder.build();
  }

  /**
   * Get the R sessions (for the invoking Opal user session).
   *
   * @return
   */
  public List<RServerSession> getSubjectRSessions() {
    return new ImmutableList.Builder<RServerSession>().addAll(getRSessions(getSubjectPrincipal())).build();
  }

  /**
   * Get the R session with the provided identifier.
   *
   * @param rSessionId
   */
  public RServerSession getRSession(String rSessionId) {
    for (SubjectRSessions rSessions : rSessionMap.values()) {
      if (rSessions.hasRSession(rSessionId)) {
        return rSessions.getRSession(rSessionId);
      }
    }
    throw new NoSuchRSessionException(rSessionId);
  }

  /**
   * Check the R session with the provided identifier exists.
   *
   * @param rSessionId
   * @return
   */
  public boolean hasRSession(String rSessionId) {
    for (SubjectRSessions rSessions : rSessionMap.values()) {
      if (rSessions.hasRSession(rSessionId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Remove the R session with the provided identifier.
   *
   * @param rSessionId
   */
  public void removeRSession(String rSessionId) {
    for (SubjectRSessions rSessions : rSessionMap.values()) {
      if (rSessions.hasRSession(rSessionId)) {
        rSessions.removeRSession(rSessionId);
        return;
      }
    }
  }

  /**
   * Remove the R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   */
  public void removeSubjectRSession(String rSessionId) {
    getRSessions(getSubjectPrincipal()).removeRSession(rSessionId);
  }

  /**
   * Save the workspace of the R session and store it within opal server data.
   *
   * @param rSessionId
   * @param saveId
   */
  public void saveSubjectRSession(String rSessionId, String saveId) {
    getRSessions(getSubjectPrincipal()).saveRSession(rSessionId, saveId);
  }

  /**
   * Restore a previously saved user's R session into the current R session.
   *
   * @param rSessionId
   * @param restoreId
   */
  public void restoreSubjectRSession(String rSessionId, String restoreId) {
    getRSessions(getSubjectPrincipal()).restoreRSession(rSessionId, restoreId);
  }

  /**
   * Creates a new R connection, stores the corresponding R session.
   *
   * @return R session
   */
  public RServerSession newSubjectRSession() {
    return newSubjectRSession(null);
  }

  /**
   * Creates a new R connection in the provided profile, stores the corresponding R session.
   *
   * @param profile
   * @return R session
   */
  public RServerSession newSubjectRSession(RServerProfile profile) {
    return newSubjectRSession(getSubjectPrincipal(), profile);
  }

  /**
   * Get the R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   * @return
   */
  public RServerSession getSubjectRSession(String rSessionId) {
    return getRSession(getSubjectPrincipal(), rSessionId);
  }

  public void removeSubjectRSessions() {
    getRSessions(getSubjectPrincipal()).removeRSessions();
  }

  /**
   * Check for expired {@link RServerSession}s, close them if any and clean subject
   * {@link RServerSession} pool.
   */
  @Scheduled(fixedDelay = 60 * 1000)
  public void checkRSessions() {
    rSessionMap.keySet().forEach(this::checkRSessions);
  }

  /**
   * Get all user workspaces for a given execution context (one file per user).
   *
   * @param executionContext
   * @return
   */
  public List<File> getWorkspaces(String executionContext) {
    File workspaces = new File(String.format(WORKSPACES_FORMAT, executionContext));
    if (!workspaces.exists()) return Lists.newArrayList();
    File[] files = workspaces.listFiles();
    if (files == null) return Lists.newArrayList();
    return Lists.newArrayList(files);
  }

  /**
   * Get all user workspaces for any execution context (one file per execution context).
   *
   * @return
   */
  public List<File> getWorkspaces() {
    return getWorkspaces("");
  }

  /**
   * Check for workspace folders that are older than allowed, every hour.
   */
  @Scheduled(fixedDelay = 3600 * 1000)
  public void removeExpiredWorkspaces() {
    File workspacesFolder = new File(String.format(WORKSPACES_FORMAT, ""));
    if (!workspacesFolder.exists()) return;
    File[] files = workspacesFolder.listFiles();
    if (files == null) return;
    for (File context : files) {
      log.debug("Checking for expired R workspaces in context {}", context.getName());
      File[] users = context.listFiles();
      if (users != null) {
        for (File userFolder : users) {
          removeExpiredUserWorkspaces(userFolder);
        }
      }
    }
  }

  //
  // private methods
  //

  private void removeExpiredUserWorkspaces(File userFolder) {
    File[] workspaces = userFolder.listFiles();
    if (workspaces == null) return;
    for (File workspace : workspaces) {
      LocalDate wsDate = new Date(workspace.lastModified()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      int days = Period.between(wsDate, LocalDate.now()).getDays();
      if (days > rWorkspaceExpires) {
        log.info("R Workspace expired: {}/{}/{} ({}d old), removing...", userFolder.getParentFile().getName(), userFolder.getName(), workspace.getName(), days);
        try {
          FileUtil.delete(workspace);
        } catch (IOException e) {
          log.info("Workspace removal failed", e);
        }
      }
    }
  }

  private synchronized void checkRSessions(String principal) {
    if (!rSessionMap.containsKey(principal)) return;
    log.debug("clearRSessions({})", principal);
    SubjectRSessions subjectRSessions = rSessionMap.get(principal);
    for (RServerSession rSession : subjectRSessions) {
      if (!rSession.isBusy()) {
        Long contextTimeout = getRSessionTimeoutByContext(rSession);
        long timeout = contextTimeout == null ? rSessionTimeout : contextTimeout;
        if (rSession.hasExpired(timeout)) {
          try {
            rSession.close();
          } catch (Exception e) {
            log.warn("Failed closing R session: {}", rSession.getId(), e);
          }
        }
      }
    }
    subjectRSessions.clean();
  }

  private Long getRSessionTimeoutByContext(RServerSession rSession) {
    String context = rSession.getExecutionContext();
    switch (context) {
      case "DataSHIELD":
        return rSessionTimeoutDataSHIELD;
      case "R":
        return rSessionTimeoutR;
      case "Import":
        return rSessionTimeoutImport;
      case "Export":
        return rSessionTimeoutExport;
      case "SQL":
        return rSessionTimeoutSQL;
      case "Analyse":
        return rSessionTimeoutAnalyse;
    }
    if (context.startsWith("View"))
      return rSessionTimeoutView;
    return rSessionTimeout;
  }

  private void doClearRSessions(String principal) {
    for (RServerSession rSession : rSessionMap.get(principal)) {
      try {
        rSession.close();
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
  }

  public RServerSession newSubjectRSession(String principal, RServerProfile profile) {
    try {
      RServerProfile safeProfile = asSafeRServerProfile(profile);
      SubjectRSessions rSessions = getRSessions(principal);
      RServerSession rSession = rServerManagerService.getRServer(safeProfile.getCluster()).newRServerSession(principal);
      rSession.setProfile(safeProfile);
      rSessions.addRSession(rSession);
      return rSession;
    } catch (Exception e) {
      throw new RRuntimeException(e);
    }
  }

  private RServerProfile asSafeRServerProfile(RServerProfile profile) {
    if (profile != null) return profile;
    return rServerManagerService.getDefaultRServerProfile();
  }

  private RServerSession getRSession(String principal, String rSessionId) {
    return getRSessions(principal).getRSession(rSessionId);
  }

  private synchronized SubjectRSessions getRSessions(String principal) {
    SubjectRSessions rSessions = rSessionMap.get(principal);
    if (rSessions == null) {
      rSessions = new SubjectRSessions();
      rSessionMap.put(principal, rSessions);
    }
    return rSessions;
  }

  private String getSubjectPrincipal() {
    if (!SecurityUtils.getSubject().isAuthenticated()) throw new ForbiddenException();
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  //
  // Nested classes
  //

  private final class SubjectRSessions implements Iterable<RServerSession> {

    private final List<RServerSession> rSessions = Collections.synchronizedList(new ArrayList<RServerSession>());

    void saveRSession(String rSessionId, String saveId) {
      RServerSession rSession = getRSession(rSessionId);
      // make sure the session storage folder is empty
      File store = rSession.getWorkspace(saveId);
      Lists.newArrayList(store.listFiles()).forEach(file -> {
        try {
          FileUtil.delete(file);
        } catch (IOException e) {
          // ignore
        }
      });
      saveRSessionFiles(rSession, saveId);
      saveRSessionImage(rSession, saveId);
    }

    void restoreRSession(String rSessionId, String restoreId) {
      RServerSession rSession = getRSession(rSessionId);
      restoreSessionImage(rSession, restoreId);
      restoreSessionFiles(rSession, restoreId);
    }

    void removeRSession(String rSessionId) {
      removeRSession(getRSession(rSessionId));
    }

    void removeRSession(RServerSession rSession) {
      try {
        rSession.close();
        rSessions.remove(rSession);
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }

    void removeRSessions(String clusterName, String serverName) {
      List<RServerSession> sessionsToRemove = rSessions.stream()
          .filter(s -> clusterName.equals(s.getProfile().getCluster()) && serverName.equals(s.getRServerServiceName()))
          .collect(Collectors.toList());
      for (RServerSession rSession : sessionsToRemove) {
        try {
          removeRSession(rSession);
        } catch (Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
    }

    void removeRSessions() {
      for (RServerSession rSession : rSessions) {
        try {
          rSession.close();
        } catch (Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
      rSessions.clear();
    }

    void clean() {
      List<RServerSession> toRemove = Lists.newArrayList();
      for (RServerSession rSession : rSessions) {
        if (rSession.isClosed()) {
          toRemove.add(rSession);
        }
      }
      rSessions.removeAll(toRemove);
    }

    private void saveRSessionImage(RServerSession rSession, String saveId) {
      // then save the memory image
      String rscript = "base::save.image()";
      RScriptROperation rop = new RScriptROperation(rscript, false);
      rSession.execute(rop);
      // clean legacy
      File destination = new File(rSession.getWorkspace(saveId), R_IMAGE_FILE);
      if (destination.exists()) destination.delete();
      destination = new File(rSession.getWorkspace(saveId), R_IMAGE_FILE_ENC);
      if (destination.exists()) destination.delete();
      try {
        FileReadROperation readop = new FileReadROperation(R_IMAGE_FILE, cryptoService.newCipherOutputStream(new FileOutputStream(destination)));
        rSession.execute(readop);
      } catch (FileNotFoundException e) {
        throw new RRuntimeException(e);
      }
    }

    private void restoreSessionImage(RServerSession rSession, String restoreId) {
      File source = new File(rSession.getWorkspace(restoreId), R_IMAGE_FILE_ENC);
      if (!source.exists()) {
        // legacy
        source = new File(rSession.getWorkspace(restoreId), R_IMAGE_FILE);
      }
      if (!source.exists()) return;

      try {
        FileWriteROperation writeop = null;
        if (R_IMAGE_FILE_ENC.equals(source.getName())) {
          writeop = new FileWriteROperation(R_IMAGE_FILE, cryptoService.newCipherInputStream(new FileInputStream(source)));
        } else {
          // legacy
          writeop = new FileWriteROperation(R_IMAGE_FILE, source);
        }
        rSession.execute(writeop);
        String rscript = String.format("base::load('%s')", R_IMAGE_FILE);
        RScriptROperation rop = new RScriptROperation(rscript, false);
        rSession.execute(rop);
      } catch (FileNotFoundException e) {
        throw new RRuntimeException(e);
      }
    }

    private void saveRSessionFiles(RServerSession rSession, String saveId) {
      rSession.saveRSessionFiles(saveId);
    }

    private void restoreSessionFiles(RServerSession rSession, String restoreId) {
      File source = rSession.getWorkspace(restoreId);
      FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).forEach(file -> {
        String destination = file.getAbsolutePath().replace(source.getAbsolutePath(), "");
        if (destination.startsWith("/")) destination = destination.substring(1);
        if (!R_IMAGE_FILE_ENC.equals(destination) && !R_IMAGE_FILE.equals(destination)) {
          if (destination.contains("/")) {
            // make sure destination directory exists
            String rscript = String.format("base::dir.create('%s', showWarnings=FALSE, recursive=TRUE)", destination.substring(0, destination.lastIndexOf("/")));
            RScriptROperation rop = new RScriptROperation(rscript, false);
            rSession.execute(rop);
          }
          FileWriteROperation writeop = new FileWriteROperation(destination, file);
          rSession.execute(writeop);
        }
      });
    }

    private boolean hasRSession(String rSessionId) {
      for (RServerSession rs : rSessions) {
        if (rs.getId().equals(rSessionId)) {
          return true;
        }
      }
      return false;
    }

    private void addRSession(RServerSession rSession) {
      rSessions.add(rSession);
    }

    private RServerSession getRSession(String rSessionId) {
      for (RServerSession rs : rSessions) {
        if (rs.getId().equals(rSessionId)) {
          return rs;
        }
      }
      throw new NoSuchRSessionException(rSessionId);
    }

    @Override
    public Iterator<RServerSession> iterator() {
      return rSessions.iterator();
    }

  }
}
