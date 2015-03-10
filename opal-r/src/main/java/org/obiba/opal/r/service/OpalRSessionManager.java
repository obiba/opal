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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.apache.shiro.SecurityUtils;
import org.obiba.opal.core.runtime.ServiceListener;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.rosuda.REngine.Rserve.RConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Maps R Sessions with its invoking Opal user (through its Opal Session). Current R session of an Opal user is the last
 * R session created or a R session explicitly set.
 */
@Component
public class OpalRSessionManager implements ServiceListener<OpalRService> {

  private static final Logger log = LoggerFactory.getLogger(OpalRSessionManager.class);

  @Value("${org.obiba.opal.r.sessionTimeout}")
  private Long rSessionTimeout;

  @Autowired
  private TransactionalThreadFactory transactionalThreadFactory;

  private OpalRService opalRService;

  private final Map<String, SubjectRSessions> rSessionMap = Maps.newConcurrentMap();

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
   * Get the R sessions.
   *
   * @return
   */
  public List<OpalRSession> getRSessions() {
    ImmutableList.Builder<OpalRSession> builder = ImmutableList.builder();

    for(SubjectRSessions rSessions : rSessionMap.values()) {
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
    for(SubjectRSessions rSessions : rSessionMap.values()) {
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
    for(SubjectRSessions rSessions : rSessionMap.values()) {
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
    for(String principal : rSessionMap.keySet()) {
      checkRSessions(principal);
    }
  }

  //
  // private methods
  //

  private synchronized void checkRSessions(String principal) {
    if(!rSessionMap.containsKey(principal)) return;
    log.debug("clearRSessions({})", principal);
    SubjectRSessions subjectRSessions = rSessionMap.get(principal);
    for(OpalRSession rSession : subjectRSessions) {
      if(rSession.hasExpired(rSessionTimeout)) {
        try {
          rSession.close();
        } catch(Exception e) {
          log.warn("Failed closing R session: {}", rSession.getId(), e);
        }
      }
    }
    subjectRSessions.clean();
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

    private final List<OpalRSession> rSessions = Collections.synchronizedList(new ArrayList<OpalRSession>());

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
    }

    public void removeRSession(String rSessionId) {
      OpalRSession rSession = getRSession(rSessionId);
      try {
        rSession.close();
        rSessions.remove(rSession);
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

    public void clean() {
      List<OpalRSession> toRemove = Lists.newArrayList();
      for(OpalRSession rSession : rSessions) {
        if(rSession.isClosed()) {
          toRemove.add(rSession);
        }
      }
      rSessions.removeAll(toRemove);
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
