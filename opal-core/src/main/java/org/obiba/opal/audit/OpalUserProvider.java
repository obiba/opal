/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.audit;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.magma.audit.UserProvider;
import org.springframework.stereotype.Component;

@Component
public class OpalUserProvider implements UserProvider {

  public static final String UNKNOWN_USERNAME = "Unknown";

  @Override
  public String getUsername() {
    // TODO: Defaulting to "Unknown" as a temporary patch.
    Subject subject = ThreadContext.getSubject();
    return subject != null && subject.getPrincipal() != null ? subject.getPrincipal().toString() : UNKNOWN_USERNAME;
  }

}
