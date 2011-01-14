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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager implements ROperationTemplate, SessionListener {

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
   * @return null if current R session is not set
   */
  public String getSubjectCurrentRSessionId() {
    OpalRSession currentRSession = getRSessions(getSubjectSessionId()).getCurrentRSession();
    if(currentRSession != null) {
      return currentRSession.getId();
    }
    throw new NoSuchRSessionException();
  }

  /**
   * Get if a current R session is defined (for the invoking Opal user).
   * @return
   */
  public boolean hasSubjectCurrentRSession() {
    return getRSessions(getSubjectSessionId()).hasCurrentRSession();
  }

  /**
   * Check if there is such a R session with the provided identifier (for the invoking Opal user).
   */
  public boolean hasSubjectRSession(String rSessionId) {
    return getRSessions(getSubjectSessionId()).hasRSession(rSessionId);
  }

  /**
   * Remove the R session with the provided identifier (for the invoking Opal user).
   * @param rSessionId
   */
  public void removeSubjectRSession(String rSessionId) {
    getRSessions(getSubjectSessionId()).removeRSession(rSessionId);
  }

  /**
   * Set the current R session with the provided identifier (for the invoking Opal user).
   * @param rSessionId
   */
  public void setSubjectCurrentRSession(String rSessionId) {
    getRSessions(getSubjectSessionId()).setCurrentRSession(rSessionId);
  }

  /**
   * Stores a new R session from the provided R connection and set it as the current one (for the invoking Opal user).
   * @param connection
   * @return R session identifier
   */
  public String addSubjectCurrentRSession(RConnection connection) {
    return addCurrentRSession(getSubjectSessionId(), connection);
  }

  /**
   * Creates a new R connection, stores the corresponding R session and set it as the current one (for the invoking Opal
   * user).
   * @return R session identifier
   */
  public String newSubjectCurrentRSession() {
    return addCurrentRSession(getSubjectSessionId(), opalRService.newConnection());
  }

  //
  // ROperationTemplate methods
  //

  /**
   * Executes the R operation on the current R session of the invoking Opal user. If no current R session is defined, a
   * {@link NoSuchRSessionException} is thrown.
   * @see #hasSubjectCurrentRSession(), {@link #setSubjectCurrentRSession(String)}
   */
  @Override
  public void execute(ROperation rop) {
    if(!hasSubjectCurrentRSession()) throw new NoSuchRSessionException();
    OpalRSession rSession = getSubjectCurrentRSession();
    RConnection connection = rSession.newConnection();
    rop.doWithConnection(connection);
    rSession.close(connection);
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
        log.warn("Failed closing R session: " + rSession.getId(), e);
      }
    }
  }

  private String addCurrentRSession(String sessionId, RConnection connection) {
    SubjectRSessions rSessions = getRSessions(sessionId);
    OpalRSession current = new OpalRSession(connection);
    rSessions.addRSession(current);
    return current.getId();
  }

  private OpalRSession getSubjectCurrentRSession() {
    return getCurrentRSession(getSubjectSessionId());
  }

  private OpalRSession getCurrentRSession(String sessionId) {
    return rSessionMap.get(sessionId).getCurrentRSession();
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

    private List<OpalRSession> rSessions = new ArrayList<OpalRSession>();

    private OpalRSession currentRSession;

    public boolean hasCurrentRSession() {
      return currentRSession != null;
    }

    public OpalRSession getCurrentRSession() {
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
        log.warn("Failed closing R session: " + rSessionId, e);
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
