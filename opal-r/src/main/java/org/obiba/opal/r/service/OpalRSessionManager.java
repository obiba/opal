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
import org.obiba.opal.r.ROperation;
import org.obiba.opal.r.ROperationTemplate;
import org.obiba.opal.r.RRuntimeException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class OpalRSessionManager implements ROperationTemplate {

  private OpalRService opalRService;

  private Map<String, List<OpalRSession>> rSessionMap = new HashMap<String, List<OpalRSession>>();

  @Autowired
  public OpalRSessionManager(OpalRService opalRService) {
    super();
    this.opalRService = opalRService;
  }

  @PreDestroy
  public void clearSessions() {
    for(String sessionId : rSessionMap.keySet()) {
      for(OpalRSession rSession : rSessionMap.get(sessionId)) {
        try {
          rSession.close();
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
    rSessionMap.clear();
  }

  //
  // ROperationTemplate methods
  //

  @Override
  public void execute(ROperation rop) {
    OpalRSession rSession = getCurrentRSession();
    RConnection connection;
    if(rSession == null) {
      connection = opalRService.newConnection();
    } else {
      connection = rSession.newConnection();
    }
    rop.doWithConnection(connection);
    if(rSession == null) {
      setCurrentRSession(connection);
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

  private void setCurrentRSession(RConnection connection) {
    String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
    setCurrentRSession(sessionId, connection);
  }

  private void setCurrentRSession(String sessionId, RConnection connection) {
    List<OpalRSession> rSessions = getRSessions(sessionId);
    rSessions.add(0, new OpalRSession(connection));
  }

  private OpalRSession getCurrentRSession() {
    String sessionId = SecurityUtils.getSubject().getSession().getId().toString();
    return getCurrentRSession(sessionId);
  }

  private OpalRSession getCurrentRSession(String sessionId) {
    List<OpalRSession> rSessions = getRSessions(sessionId);
    return rSessions.size() == 0 ? null : rSessions.get(0);
  }

  private List<OpalRSession> getRSessions(String sessionId) {
    List<OpalRSession> rSessions = rSessionMap.get(sessionId);
    if(rSessions == null) {
      rSessions = new ArrayList<OpalRSession>();
      rSessionMap.put(sessionId, rSessions);
    }
    return rSessions;
  }

}
