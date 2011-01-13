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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.obiba.opal.core.runtime.security.OpalSecurityManager;
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
public class OpalRSessionManager implements ROperationTemplate {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  private OpalRService opalRService;

  private OpalSecurityManager opalSecurityManager;

  private Map<String, List<OpalRSession>> rSessionMap = new HashMap<String, List<OpalRSession>>();

  @Autowired
  public OpalRSessionManager(OpalRService opalRService, OpalSecurityManager opalSecurityManager) {
    super();
    this.opalRService = opalRService;
    this.opalSecurityManager = opalSecurityManager;
  }

  @PostConstruct
  public void start() {
    opalSecurityManager.addSessionListener(new OpalSessionListener());
  }

  @PreDestroy
  public void stop() {
    for(String sessionId : rSessionMap.keySet()) {
      doClearRSessions(sessionId);
    }
    rSessionMap.clear();
  }

  /**
   * Stores a new R session from the provided R connection and set it as the current one (for the invoking Opal user).
   * @param connection
   */
  public void setSubjectCurrentRSession(RConnection connection) {
    setCurrentRSession(getSubjectSessionId(), connection);
  }

  /**
   * Creates a new R connection, stores the corresponding R session and set it as the current one (for the invoking Opal
   * user).
   */
  public void newSubjectCurrentRSession() {
    setCurrentRSession(getSubjectSessionId(), opalRService.newConnection());
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
        log.warn("Failed closing R session of " + sessionId, e);
      }
    }
  }

  private void setCurrentRSession(String sessionId, RConnection connection) {
    List<OpalRSession> rSessions = getRSessions(sessionId);
    rSessions.add(0, new OpalRSession(connection));
  }

  private OpalRSession getSubjectCurrentRSession() {
    return getCurrentRSession(getSubjectSessionId());
  }

  private OpalRSession getCurrentRSession(String sessionId) {
    List<OpalRSession> rSessions = getRSessions(sessionId);
    return rSessions.size() == 0 ? null : rSessions.get(0);
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

  //
  // Nested classes and interfaces
  //

  /**
   * Opal session lifecycle callback: does housekeeping with R sessions that have no living Opal session.
   */
  private final class OpalSessionListener implements SessionListener {
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
  }
}
