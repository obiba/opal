/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
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
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created.
 */
@Component
public class OpalRSessionManager implements ROperationTemplate, SessionListener {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  private final OpalRService opalRService;

  private final Map<String, List<OpalRSession>> rSessionMap = new HashMap<String, List<OpalRSession>>();

  private Map<String, OpalRSession> currentRSessionMap = new HashMap<String, OpalRSession>();

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
    currentRSessionMap.clear();
  }

  /**
   * Get the R session identifiers (for the invoking Opal user).
   * @return
   */
  public List<String> getSubjectRSessionIds() {
    List<String> ids = new ArrayList<String>();
    for(OpalRSession rSession : getRSessions(getSubjectSessionId())) {
      ids.add(rSession.getId());
    }
    return ids;
  }

  /**
   * Get the current R session identifier (for the invoking Opal user).
   * @return
   */
  public String getSubjectCurrentRSessionId() {
    OpalRSession rSession = currentRSessionMap.get(getSubjectSessionId());
    return rSession != null ? rSession.getId() : null;
  }

  /**
   * Check if there is such a R session with the provided identifier (for the invoking Opal user).
   */
  public boolean hasSubjectRSession(String rSessionId) {
    return getSubjectRSession(rSessionId) != null;
  }

  /**
   * Remove the R session with the provided identifier (for the invoking Opal user).
   * @param rSessionId
   */
  public synchronized void removeSubjectRSession(String rSessionId) {
    OpalRSession rSession = getSubjectRSession(rSessionId);
    if(rSession != null) {
      String sessionId = getSubjectSessionId();
      getRSessions(sessionId).remove(rSession);
      if(rSession.equals(currentRSessionMap.get(sessionId))) {
        currentRSessionMap.remove(sessionId);
      }
    }
    // else ignore
  }

  /**
   * Set the current R session with the provided identifier (for the invoking Opal user).
   * @param rSessionId
   */
  public synchronized void setSubjectCurrentRSession(String rSessionId) {
    OpalRSession rSession = getSubjectRSession(rSessionId);
    String sessionId = getSubjectSessionId();
    if(rSession != null) {
      currentRSessionMap.put(sessionId, rSession);
    } else
      throw new IllegalArgumentException("No such R session: " + rSessionId + " (user session: " + sessionId + ")");
  }

  /**
   * Stores a new R session from the provided R connection and set it as the current one (for the invoking Opal user).
   * @param connection
   * @return R session identifier
   */
  public String setSubjectCurrentRSession(RConnection connection) {
    return setCurrentRSession(getSubjectSessionId(), connection);
  }

  /**
   * Creates a new R connection, stores the corresponding R session and set it as the current one (for the invoking Opal
   * user).
   * @return R session identifier
   */
  public String newSubjectCurrentRSession() {
    return setCurrentRSession(getSubjectSessionId(), opalRService.newConnection());
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Executes the R operation on the current R session of the invoking Opal user. If no current R session is defined,
   * creates a new one.
   */
  @Override
  public void execute(ROperation rop) {
    OpalRSession rSession = getSubjectCurrentRSession();
    RConnection connection;
    if(rSession == null) {
      connection = opalRService.newConnection();
    } else {
      connection = rSession.newConnection();
    }
    rop.doWithConnection(connection);
    if(rSession == null) {
      setSubjectCurrentRSession(connection);
    } else {
      try {
        connection.detach();
      } catch(RserveException e) {
        throw new RRuntimeException(e);
      }
    }
  }

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

  private OpalRSession getSubjectRSession(String rSessionId) {
    for(OpalRSession rs : getRSessions(getSubjectSessionId())) {
      if(rs.getId().equals(rSessionId)) {
        return rs;
      }
    }
    return null;
  }

  private synchronized void clearRSessions(String sessionId) {
    log.info("clearRSessions({})", sessionId);
    if(rSessionMap.containsKey(sessionId)) {
      doClearRSessions(sessionId);
      rSessionMap.remove(sessionId);
      currentRSessionMap.remove(sessionId);
    }
  }

  private void doClearRSessions(String sessionId) {
    for(OpalRSession rSession : rSessionMap.get(sessionId)) {
      try {
        rSession.close();
      } catch(Exception e) {
        log.warn("Failed closing R session of " + sessionId, e);
      }
    }
  }

  private synchronized String setCurrentRSession(String sessionId, RConnection connection) {
    List<OpalRSession> rSessions = getRSessions(sessionId);
    OpalRSession current = new OpalRSession(connection);
    rSessions.add(current);
    currentRSessionMap.put(sessionId, current);
    return current.getId();
  }

  private OpalRSession getSubjectCurrentRSession() {
    return getCurrentRSession(getSubjectSessionId());
  }

  private OpalRSession getCurrentRSession(String sessionId) {
    return currentRSessionMap.get(sessionId);
  }

  private synchronized List<OpalRSession> getRSessions(String sessionId) {
    List<OpalRSession> rSessions = rSessionMap.get(sessionId);
    if(rSessions == null) {
      rSessions = new ArrayList<OpalRSession>();
      rSessionMap.put(sessionId, rSessions);
    }
    return rSessions;
  }

  private String getSubjectSessionId() {
    return SecurityUtils.getSubject().getSession().getId().toString();
  }
}
