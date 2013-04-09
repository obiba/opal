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
public class OpalRSessionManager implements SessionListener {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  private final OpalRService opalRService;

  private final Map<String, SubjectRSessions> rSessionMap = new HashMap<String, SubjectRSessions>();

  @Autowired
  public OpalRSessionManager(OpalRService opalRService) {
    this.opalRService = opalRService;
  }

  @PreDestroy
  public void stop() {
    for(String sessionId : rSessionMap.keySet()) {
      doClearRSessions(sessionId);
    }
    rSessionMap.clear();
  }

  /**
   * Get the R session identifiers (for the invoking Opal user session).
   *
   * @return
   */
  public List<OpalRSession> getSubjectRSessions() {
    return new ImmutableList.Builder<OpalRSession>().addAll(getRSessions(getSubjectSessionId())).build();
  }

  /**
   * Get if a current R session is defined (for the invoking Opal user session).
   *
   * @return
   */
  public boolean hasSubjectCurrentRSession() {
    return getRSessions(getSubjectSessionId()).hasCurrentRSession();
  }

  /**
   * Check if there is such a R session with the provided identifier (for the invoking Opal user session).
   */
  public boolean hasSubjectRSession(String rSessionId) {
    return getRSessions(getSubjectSessionId()).hasRSession(rSessionId);
  }

  /**
   * Remove the R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   */
  public void removeSubjectRSession(String rSessionId) {
    getRSessions(getSubjectSessionId()).removeRSession(rSessionId);
  }

  /**
   * Set the current R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   */
  public void setSubjectCurrentRSession(String rSessionId) {
    getRSessions(getSubjectSessionId()).setCurrentRSession(rSessionId);
  }

  /**
   * Stores a new R session from the provided R connection and set it as the current one (for the invoking Opal user
   * session).
   *
   * @param connection
   * @return R session
   */
  public OpalRSession addSubjectCurrentRSession(RConnection connection) {
    return addCurrentRSession(getSubjectSessionId(), connection);
  }

  /**
   * Creates a new R connection, stores the corresponding R session and set it as the current one (for the invoking Opal
   * user session).
   *
   * @return R session
   */
  public OpalRSession newSubjectCurrentRSession() {
    return addCurrentRSession(getSubjectSessionId(), opalRService.newConnection());
  }

  /**
   * Get the current R session (for the invoking Opal user session).
   *
   * @return
   */
  public OpalRSession getSubjectCurrentRSession() {
    return getCurrentRSession(getSubjectSessionId());
  }

  /**
   * Get the R session with the provided identifier (for the invoking Opal user session).
   *
   * @param rSessionId
   * @return
   */
  public OpalRSession getSubjectRSession(String rSessionId) {
    return getRSession(getSubjectSessionId(), rSessionId);
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

  private synchronized void clearRSessions(String sessionId) {
    log.info("clearRSessions({})", sessionId);
    if(rSessionMap.containsKey(sessionId)) {
      doClearRSessions(sessionId);
      rSessionMap.remove(sessionId);
    }
  }

  private void doClearRSessions(String sessionId) {
    for(OpalRSession rSession : rSessionMap.get(sessionId)) {
      try {
        rSession.close();
      } catch(Exception e) {
        log.warn("Failed closing R session: {}", rSession.getId(), e);
      }
    }
  }

  private OpalRSession addCurrentRSession(String sessionId, RConnection connection) {
    SubjectRSessions rSessions = getRSessions(sessionId);
    OpalRSession current = new OpalRSession(connection);
    rSessions.addRSession(current);
    return current;
  }

  private OpalRSession getCurrentRSession(String sessionId) {
    return getRSessions(sessionId).getCurrentRSession();
  }

  private OpalRSession getRSession(String sessionId, String rSessionId) {
    return getRSessions(sessionId).getRSession(rSessionId);
  }

  private synchronized SubjectRSessions getRSessions(String sessionId) {
    SubjectRSessions rSessions = rSessionMap.get(sessionId);
    if(rSessions == null) {
      rSessions = new SubjectRSessions();
      rSessionMap.put(sessionId, rSessions);
    }
    return rSessions;
  }

  private String getSubjectSessionId() {
    return SecurityUtils.getSubject().getSession().getId().toString();
  }

  //
  // Nested classes
  //

  private static final class SubjectRSessions implements Iterable<OpalRSession> {

    private final List<OpalRSession> rSessions = new ArrayList<OpalRSession>();

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
