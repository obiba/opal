/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import org.apache.shiro.authc.AuthenticationToken;

public class BackgroundJobServiceAuthToken implements AuthenticationToken {

  private static final long serialVersionUID = 1598701332628205293L;

  @Override
  public Object getPrincipal() {
    return null;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

}
