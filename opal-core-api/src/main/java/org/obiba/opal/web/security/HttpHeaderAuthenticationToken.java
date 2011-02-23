/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import org.apache.shiro.authc.AuthenticationToken;

public class HttpHeaderAuthenticationToken implements AuthenticationToken {

  private static final long serialVersionUID = 4520790559763117320L;

  private final String sessionId;

  public HttpHeaderAuthenticationToken(String sessionId) {
    this.sessionId = sessionId;
  }

  @Override
  public Object getPrincipal() {
    return getSessionId();
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  public String getSessionId() {
    return sessionId;
  }

}
