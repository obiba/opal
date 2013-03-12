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
import org.apache.shiro.subject.Subject;
import org.obiba.magma.audit.UserProvider;

public class OpalUserProvider implements UserProvider {

  public String getUsername() {
    // TODO: Defaulting to "Unknown" as a temporary patch.
    Subject subject = SecurityUtils.getSubject();
    return (subject != null && subject.getPrincipal() != null) ? subject.getPrincipal().toString() : "Unknown";
  }

}
