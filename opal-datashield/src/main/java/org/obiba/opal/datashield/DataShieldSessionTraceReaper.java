/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import jakarta.annotation.PreDestroy;
import org.obiba.opal.r.service.OpalRSessionManager;
import org.obiba.opal.r.service.RServerSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ends the trace of a DataSHIELD session that is gone without having been closed.
 * <p/>
 * A session span is held open for the lifetime of its session, and an open span is never exported -
 * so a session that ends any other way than through its CLOSE endpoint would cost the whole trace,
 * not just its root. Sessions do end that way: {@link OpalRSessionManager} expires the idle ones,
 * and they go with the R server they belong to. As with the session gauge, the manager is asked
 * rather than notified - it already knows, and its answer cannot be stale.
 */
@Component
public class DataShieldSessionTraceReaper {

  private final OpalRSessionManager opalRSessionManager;

  @Autowired
  public DataShieldSessionTraceReaper(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  /**
   * On the same cadence as the R session reaper it follows, and never ahead of it: a session it has
   * not evicted yet is still live, and its trace stays open.
   */
  @Scheduled(fixedDelay = 60 * 1000)
  public void endTracesOfGoneSessions() {
    // a supplier, so that the open traces are listed before the manager is asked: a session opened
    // in between is then not taken for gone
    DataShieldSessionTraces.retain(() -> opalRSessionManager.getRSessions().stream()
        .map(RServerSession::getId)
        .collect(Collectors.toSet()));
  }

  /**
   * On shutdown the sessions are all about to be gone, and their traces are worth having: a restart
   * in the middle of a session is exactly the kind of thing the trace is read to explain.
   */
  @PreDestroy
  public void endOpenTraces() {
    DataShieldSessionTraces.endAll();
  }
}
