/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.rock;

import com.google.common.eventbus.EventBus;
import org.obiba.opal.core.domain.AppCredentials;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.core.tx.TransactionalThreadFactory;
import org.obiba.opal.r.service.RContextInitiator;
import org.obiba.opal.r.service.RServerProfile;
import org.obiba.opal.spi.r.RServerConnection;
import org.obiba.opal.spi.r.RServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RockAppSession extends RockSession implements RServerConnection {

  private static final Logger log = LoggerFactory.getLogger(RockAppSession.class);

  private final App app;

  protected RockAppSession(String serverName, RServerProfile profile, RContextInitiator rContextInitiator, App app, AppCredentials credentials, String user, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    this(serverName, null, profile, rContextInitiator, app, credentials, user, transactionalThreadFactory, eventBus);
  }

  protected RockAppSession(String serverName, String id, RServerProfile profile, RContextInitiator rContextInitiator, App app, AppCredentials credentials, String user, TransactionalThreadFactory transactionalThreadFactory, EventBus eventBus) throws RServerException {
    super(serverName, id, profile, rContextInitiator, credentials, user, transactionalThreadFactory, eventBus);
    this.app = app;
    openSession();
  }

  //
  // Private methods
  //

  protected App getApp() {
    return app;
  }

  @Override
  protected String getRSessionsResourceUrl() {
    return String.format("%s/r/sessions", app.getServer());
  }

  @Override
  protected String getRSessionResourceUrl(String path) {
    return String.format("%s/r/session/%s%s", app.getServer(), rockSessionId, path);
  }
}
