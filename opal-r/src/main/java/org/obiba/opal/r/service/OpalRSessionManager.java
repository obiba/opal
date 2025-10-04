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
import jakarta.ws.rs.ForbiddenException;
import org.apache.shiro.SecurityUtils;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.service.security.CryptoService;
import org.obiba.opal.r.service.event.RServerServiceStoppedEvent;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.r.service.tasks.SubjectRSessions;
import org.obiba.opal.spi.r.RRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager implements DisposableBean {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  public static final String WORKSPACES_FORMAT = System.getenv().get("OPAL_HOME") + File.separatorChar + "data"
      + File.separatorChar + "R" + File.separatorChar + "workspaces" + File.separatorChar + "%s";


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
      RServerService service = rServerManagerService.getRServer(safeProfile.getCluster());
      RServerSession rSession = service.newRServerSession(principal);
      rSession.setProfile(safeProfile);
      SubjectRSessions rSessions = getRSessions(principal);
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
      rSessions = new SubjectRSessions(cryptoService);
      rSessionMap.put(principal, rSessions);
    }
    return rSessions;
  }

  private String getSubjectPrincipal() {
    if (!SecurityUtils.getSubject().isAuthenticated()) throw new ForbiddenException();
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

}
