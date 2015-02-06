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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.runtime.ServiceListener;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager implements SessionListener, ServiceListener<OpalRService> {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  @Autowired
  private TransactionalThreadFactory transactionalThreadFactory;

  private OpalRService opalRService;

  private final Map<String, SubjectRSessions> rSessionMap = new HashMap<>();

  @PreDestroy
  public void stop() {
    for(String principal : rSessionMap.keySet()) {
      doClearRSessions(principal);
    }
    rSessionMap.clear();
  }

  @Autowired
  public void setOpalRService(OpalRService opalRService) {
    this.opalRService = opalRService;
    opalRService.addListener(this);
  }

  @Override
  public void onServiceStart(OpalRService service) {

  }

  @Override
  public void onServiceStop(OpalRService service) {
    stop();
  }

  /**
   * Get the R session identifiers (for the invoking Opal user session).
   *
   * @return
   */
  public List<OpalRSession> getSubjectRSessions() {
    return new ImmutableList.Builder<OpalRSession>().addAll(getRSessions(getSubjectPrincipal())).build();
  }

  /**
   * Get if a current R session is defined (for the invoking Opal user session).
   *
   * @return
   */
  public boolean hasSubjectCurrentRSession() {
    return getRSessions(getSubjectPrincipal()).hasCurrentRSession();
  }

  /**
   * Check if there is such a R session with the provided identifier (for the invoking Opal user session).
   */
  public boolean hasSubjectRSession(String rSessionId) {
    return getRSessions(getSubjectPrincipal()).hasRSession(rSessionId);
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
   * Set the current R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   */
  public void setSubjectCurrentRSession(String rSessionId) {
    getRSessions(getSubjectPrincipal()).setCurrentRSession(rSessionId);
  }

  /**
   * Stores a new R session from the provided R connection and set it as the current one (for the invoking Opal user
   * session).
   *
   * @param connection
   * @return R session
   */
  public OpalRSession addSubjectCurrentRSession(RConnection connection) {
    return addCurrentRSession(getSubjectPrincipal(), connection);
  }

  /**
   * Creates a new R connection, stores the corresponding R session and set it as the current one (for the invoking Opal
   * user session).
   *
   * @return R session
   */
  public OpalRSession newSubjectCurrentRSession() {
    return addCurrentRSession(getSubjectPrincipal(), opalRService.newConnection());
  }

  /**
   * Get the current R session (for the invoking Opal user session).
   *
   * @return
   */
  public OpalRSession getSubjectCurrentRSession() {
    return getCurrentRSession(getSubjectPrincipal());
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

  //
  // SessionListener methods
  //

  @Override
  public void onStop(Session session) {
    clearRSessions(session.getId().toString());
  }

  @Override
  public void onStart(Session session) {

  }

  @Override
  public void onExpiration(Session session) {
    clearRSessions(session.getId().toString());
  }

  //
  // private methods
  //

  private synchronized void clearRSessions(String principal) {
    if(rSessionMap.containsKey(principal)) {
      log.debug("clearRSessions({})", principal);
      doClearRSessions(principal);
      rSessionMap.remove(principal);
    }
  }

  private void doClearRSessions(String principal) {
    for(OpalRSession rSession : rSessionMap.get(principal)) {
      try {
        rSession.close();
      } catch(Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
  }

  private OpalRSession addCurrentRSession(String principal, RConnection connection) {
    SubjectRSessions rSessions = getRSessions(principal);
    OpalRSession current = new OpalRSession(connection, transactionalThreadFactory);
    rSessions.addRSession(current);
    return current;
  }

  private OpalRSession getCurrentRSession(String principal) {
    return getRSessions(principal).getCurrentRSession();
  }

  private OpalRSession getRSession(String principal, String rSessionId) {
    return getRSessions(principal).getRSession(rSessionId);
  }

  private synchronized SubjectRSessions getRSessions(String principal) {
    SubjectRSessions rSessions = rSessionMap.get(principal);
    if(rSessions == null) {
      rSessions = new SubjectRSessions();
      rSessionMap.put(principal, rSessions);
    }
    return rSessions;
  }

  private String getSubjectPrincipal() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

  //
  // Nested classes
  //

  private static final class SubjectRSessions implements Iterable<OpalRSession> {

    private final List<OpalRSession> rSessions = new ArrayList<>();

    private OpalRSession currentRSession;

    public boolean hasCurrentRSession() {
      return currentRSession != null;
    }

    public OpalRSession getCurrentRSession() {
      if(currentRSession == null) throw new NoSuchRSessionException();
      return currentRSession;
    }

    public void setCurrentRSession(String rSessionId) {
      currentRSession = getRSession(rSessionId);
    }

    private boolean hasRSession(String rSessionId) {
      for(OpalRSession rs : rSessions) {
        if(rs.getId().equals(rSessionId)) {
          return true;
        }
      }
      return false;
    }

    private void addRSession(OpalRSession rSession) {
      rSessions.add(rSession);
      currentRSession = rSession;
    }

    public void removeRSession(String rSessionId) {
      OpalRSession rSession = getRSession(rSessionId);
      if(currentRSession != null && currentRSession.getId().equals(rSessionId)) {
        currentRSession = null;
      }
      rSessions.remove(rSession);
      try {
        rSession.close();
      } catch(Exception e) {
        log.warn("Failed closing R session: {}", rSessionId, e);
      }
    }

    public void removeRSessions() {
      for(OpalRSession rSession : rSessions) {
        try {
          rSession.close();
        } catch(Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
      rSessions.clear();
    }

    private OpalRSession getRSession(String rSessionId) {
      for(OpalRSession rs : rSessions) {
        if(rs.getId().equals(rSessionId)) {
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
