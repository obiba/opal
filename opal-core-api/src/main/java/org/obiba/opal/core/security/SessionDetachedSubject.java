/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DelegatingSubject;

/**
 * Shiro's DelegatingSubject impl that is not tied to a session, but only to the principals of a given original session.
 */
public class SessionDetachedSubject extends DelegatingSubject {

  SessionDetachedSubject(DelegatingSubject source) {
    super(source.getPrincipals(), source.isAuthenticated(), null, null, source.getSecurityManager());
  }

  @Override
  public void login(AuthenticationToken token) throws AuthenticationException {
    //no login allowed
  }

  @Override
  public void logout() {
    //no logout possible
  }

  @Override
  protected boolean isSessionCreationEnabled() {
    return false; //no session creation allowed
  }

  /**
   * Returns a session detached subject. This makes sure the job is immune to sessiontimeouts/logouts (OPAL-2717)
   *
   * @param original
   * @return session detached Subject
   */
  public static Subject asSessionDetachedSubject(Subject original) {
    if(original.getSession(false) != null && original instanceof DelegatingSubject) {
      //only creates a detached subject if has a session and is a DelegatingSubject
      return new SessionDetachedSubject((DelegatingSubject) original);
    }
    return original;
  }
}
