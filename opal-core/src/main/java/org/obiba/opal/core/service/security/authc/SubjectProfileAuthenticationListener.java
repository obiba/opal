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
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.shiro.authc.TicketAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectProfileAuthenticationListener implements AuthenticationListener {

  private static final Logger log = LoggerFactory.getLogger(SubjectProfileAuthenticationListener.class);

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private SubjectAclService subjectAclService;

  @Override
  public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
    log.debug("Subject with token '{}' successfully authenticated as principal '{}'", token.getPrincipal(),
        info.getPrincipals().getPrimaryPrincipal());
    // if authenticated by ticket, the session created has to be properly configured
    if(token instanceof TicketAuthenticationToken) {
      subjectAclService.addSubjectPermission("rest", "/auth/session/" + SecurityUtils.getSubject().getSession().getId(),
          SubjectAcl.SubjectType.USER.subjectFor(info.getPrincipals().getPrimaryPrincipal().toString()), "*:GET/*");
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
