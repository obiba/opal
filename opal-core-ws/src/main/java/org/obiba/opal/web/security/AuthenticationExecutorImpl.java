/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.shiro.web.filter.AuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Perform the authentication, either by username-password token or by obiba ticket token.
 */
@Component
public class AuthenticationExecutorImpl implements AuthenticationExecutor {

  private static final Logger log = LoggerFactory.getLogger(AuthenticationExecutorImpl.class);

  private static final String ENSURED_PROFILE = "ensuredProfile";

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public Subject login(AuthenticationToken token) throws AuthenticationException {
    Subject subject = SecurityUtils.getSubject();
    if (!subject.isAuthenticated()) {
      subject.login(token);
      ThreadContext.bind(subject);
    }
    ensureProfile(subject);
    return subject;
  }


  private void ensureProfile(Subject subject) {
    Object principal = subject.getPrincipal();

    if(!subjectProfileService.supportProfile(principal)) {
      return;
    }

    Session subjectSession = subject.getSession(false);
    boolean ensuredProfile = subjectSession != null && subjectSession.getAttribute(ENSURED_PROFILE) != null;
    if(!ensuredProfile) {
      String username = principal.toString();
      log.info("Ensure HOME folder for {}", username);
      subjectProfileService.ensureProfile(subject.getPrincipals());
      if(subjectSession != null) {
        subjectSession.setAttribute(ENSURED_PROFILE, true);
      }
    }
  }

}
