/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
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
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.event.RServiceStoppedEvent;
import org.obiba.opal.spi.r.FileReadROperation;
import org.obiba.opal.spi.r.FileWriteROperation;
import org.obiba.opal.spi.r.RScriptROperation;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.ws.rs.ForbiddenException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  static final String WORKSPACES_FORMAT = System.getenv().get("OPAL_HOME") + File.separatorChar + "data"
      + File.separatorChar + "R" + File.separatorChar + "workspaces" + File.separatorChar + "%s";

  private static final String R_IMAGE_FILE = ".RData";

  @Value("${org.obiba.opal.r.sessionTimeout}")
  private Long rSessionTimeout;

  @Autowired
  private TransactionalThreadFactory transactionalThreadFactory;

  private OpalRService opalRService;

  private final Map<String, SubjectRSessions> rSessionMap = Maps.newConcurrentMap();

  @PreDestroy
  public void stop() {
    for (String principal : rSessionMap.keySet()) {
      doClearRSessions(principal);
    }
    rSessionMap.clear();
  }

  @Autowired
  public void setOpalRService(OpalRService opalRService) {
    this.opalRService = opalRService;
  }

  @Subscribe
  public void onRServiceStopped(RServiceStoppedEvent event) {
    try {
      stop();
    } catch(Exception e) {
      log.warn("Error while stopping R session manager", e);
    }
  }

  /**
   * Get the R sessions.
   *
   * @return
   */
  public List<OpalRSession> getRSessions() {
    ImmutableList.Builder<OpalRSession> builder = ImmutableList.builder();

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
  public List<OpalRSession> getSubjectRSessions() {
    return new ImmutableList.Builder<OpalRSession>().addAll(getRSessions(getSubjectPrincipal())).build();
  }

  /**
   * Get the R session with the provided identifier.
   *
   * @param rSessionId
   */
  public OpalRSession getRSession(String rSessionId) {
    for (SubjectRSessions rSessions : rSessionMap.values()) {
      if (rSessions.hasRSession(rSessionId)) {
        return rSessions.getRSession(rSessionId);
      }
    }
    throw new NoSuchRSessionException(rSessionId);
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
  public OpalRSession newSubjectRSession() {
    return addRSession(getSubjectPrincipal(), opalRService.newConnection());
  }

  /**
   * Get the R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   * @return
   */
  public OpalRSession getSubjectRSession(String rSessionId) {
    return getRSession(getSubjectPrincipal(), rSessionId);
  }

  public void removeSubjectRSessions() {
    getRSessions(getSubjectPrincipal()).removeRSessions();
  }

  /**
   * Check for expired {@link org.obiba.opal.r.service.OpalRSession}s, close them if any and clean subject
   * {@link org.obiba.opal.r.service.OpalRSession} pool.
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

  //
  // private methods
  //

  private synchronized void checkRSessions(String principal) {
    if (!rSessionMap.containsKey(principal)) return;
    log.debug("clearRSessions({})", principal);
    SubjectRSessions subjectRSessions = rSessionMap.get(principal);
    for (OpalRSession rSession : subjectRSessions) {
      if (rSession.hasExpired(rSessionTimeout)) {
        try {
          rSession.close();
        } catch (Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
    }
    subjectRSessions.clean();
  }

  private void doClearRSessions(String principal) {
    for (OpalRSession rSession : rSessionMap.get(principal)) {
      try {
        rSession.close();
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
  }

  private OpalRSession addRSession(String principal, RConnection connection) {
    SubjectRSessions rSessions = getRSessions(principal);
    OpalRSession rSession = new OpalRSession(connection, transactionalThreadFactory, principal);
    rSessions.addRSession(rSession);
    return rSession;
  }

  private OpalRSession getRSession(String principal, String rSessionId) {
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

  private static final class SubjectRSessions implements Iterable<OpalRSession> {

    private final List<OpalRSession> rSessions = Collections.synchronizedList(new ArrayList<OpalRSession>());

    void saveRSession(String rSessionId, String saveId) {
      OpalRSession rSession = getRSession(rSessionId);
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
      OpalRSession rSession = getRSession(rSessionId);
      restoreSessionImage(rSession, restoreId);
      restoreSessionFiles(rSession, restoreId);
    }

    void removeRSession(String rSessionId) {
      OpalRSession rSession = getRSession(rSessionId);
      try {
        rSession.close();
        rSessions.remove(rSession);
      } catch (Exception e) {
        log.warn("Failed closing R session: {}", rSessionId, e);
      }
    }

    void removeRSessions() {
      for (OpalRSession rSession : rSessions) {
        try {
          rSession.close();
        } catch (Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
      rSessions.clear();
    }

    void clean() {
      List<OpalRSession> toRemove = Lists.newArrayList();
      for (OpalRSession rSession : rSessions) {
        if (rSession.isClosed()) {
          toRemove.add(rSession);
        }
      }
      rSessions.removeAll(toRemove);
    }

    private void saveRSessionFiles(OpalRSession rSession, String saveId) {
      // save the files (if any)
      String rscript = "list.files(path = '.', recursive = TRUE)";
      RScriptROperation rop = new RScriptROperation(rscript, false);
      rSession.execute(rop);
      if (!rop.hasResult()) return;
      REXPString files = (REXPString) rop.getResult();
      Lists.newArrayList(files.asStrings()).forEach(file -> {
        FileReadROperation readop = new FileReadROperation(file, new File(rSession.getWorkspace(saveId), file));
        rSession.execute(readop);
      });
    }

    private void saveRSessionImage(OpalRSession rSession, String saveId) {
      // then save the memory image
      String rscript = "base::save.image()";
      RScriptROperation rop = new RScriptROperation(rscript, false);
      rSession.execute(rop);
      FileReadROperation readop = new FileReadROperation(R_IMAGE_FILE, new File(rSession.getWorkspace(saveId), R_IMAGE_FILE));
      rSession.execute(readop);
    }

    private void restoreSessionImage(OpalRSession rSession, String restoreId) {
      File source = new File(rSession.getWorkspace(restoreId), R_IMAGE_FILE);
      if (!source.exists()) return;
      FileWriteROperation writeop = new FileWriteROperation(R_IMAGE_FILE, source);
      rSession.execute(writeop);
      String rscript = String.format("base::load('%s')", R_IMAGE_FILE);
      RScriptROperation rop = new RScriptROperation(rscript, false);
      rSession.execute(rop);
    }

    private void restoreSessionFiles(OpalRSession rSession, String restoreId) {
      File source = rSession.getWorkspace(restoreId);
      FileUtils.listFiles(source, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE).forEach(file -> {
        String destination = file.getAbsolutePath().replace(source.getAbsolutePath(), "");
        if (destination.startsWith("/")) destination = destination.substring(1);
        if (destination.contains("/")) {
          // make sure destination directory exists
          String rscript = String.format("base::dir.create('%s', showWarnings=FALSE, recursive=TRUE)", destination.substring(0, destination.lastIndexOf("/")));
          RScriptROperation rop = new RScriptROperation(rscript, false);
          rSession.execute(rop);
        }
        FileWriteROperation writeop = new FileWriteROperation(destination, file);
        rSession.execute(writeop);
      });
    }

    private boolean hasRSession(String rSessionId) {
      for (OpalRSession rs : rSessions) {
        if (rs.getId().equals(rSessionId)) {
          return true;
        }
      }
      return false;
    }

    private void addRSession(OpalRSession rSession) {
      rSessions.add(rSession);
    }

    private OpalRSession getRSession(String rSessionId) {
      for (OpalRSession rs : rSessions) {
        if (rs.getId().equals(rSessionId)) {
          return rs;
        }
      }
      throw new NoSuchRSessionException(rSessionId);
    }

    @Override
    public Iterator<OpalRSession> iterator() {
      return rSessions.iterator();
    }

  }
}
