/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.authc;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.service.SubjectProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectProfileAuthenticationListener implements AuthenticationListener {

  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
    Object credentials = info.getCredentials();
    if(credentials != null && credentials.toString().startsWith(SET_COOKIE_HEADER + ":")) {
      SecurityUtils.getSubject().getSession()
          .setAttribute(SET_COOKIE_HEADER, credentials.toString().substring(SET_COOKIE_HEADER.length() + 1));
    }
  }

  @Override
  public void onFailure(AuthenticationToken token, AuthenticationException ae) {
  }

  @Override
  public void onLogout(PrincipalCollection principals) {
    Object primaryPrincipal = principals.getPrimaryPrincipal();
    if(subjectProfileService.supportProfile(primaryPrincipal)) {
      subjectProfileService.updateProfile(primaryPrincipal.toString());
    }
  }

}
