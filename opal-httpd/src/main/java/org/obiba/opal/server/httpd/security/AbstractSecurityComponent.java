/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.server.httpd.security;

import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;

abstract class AbstractSecurityComponent {

  private final SessionsSecurityManager securityManager;

  public AbstractSecurityComponent(SessionsSecurityManager securityManager) {
    if(securityManager == null) throw new IllegalArgumentException("securityManager cannot be null");
    this.securityManager = securityManager;
  }

  protected SessionsSecurityManager getSecurityManager() {
    return securityManager;
  }

  protected boolean isValidSessionId(String sessionId) {
    if(sessionId != null) {
      SessionKey key = new DefaultSessionKey(sessionId);
      try {
        return securityManager.getSessionManager().getSession(key) != null;
      } catch(SessionException e) {
        // Means that the session does not exist or has expired.
      }
    }
    return false;
  }

}
