/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.audit;

import org.apache.shiro.SecurityUtils;
import org.obiba.magma.audit.UserProvider;

public class OpalUserProvider implements UserProvider {

  public String getUsername() {
    return SecurityUtils.getSubject().getPrincipal().toString();
  }

}
