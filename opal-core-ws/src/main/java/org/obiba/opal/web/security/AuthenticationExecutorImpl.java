/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import com.google.common.base.Joiner;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Set;

/**
 * Perform the authentication, either by username-password token or by obiba ticket token.
 */
@Component
public class AuthenticationExecutorImpl extends AbstractAuthenticationExecutor {

  @Value("${org.obiba.opal.security.login.maxTry}")
  private int maxTry;

  @Value("${org.obiba.opal.security.login.trialTime}")
  private int trialTime;

  @Value("${org.obiba.opal.security.login.banTime}")
  private int banTime;

  private static final Logger log = LoggerFactory.getLogger(AuthenticationExecutorImpl.class);

  private static final String ENSURED_PROFILE = "ensuredProfile";

  @Autowired
  private SubjectProfileService subjectProfileService;

  @PostConstruct
  public void configure() {
    configureBan(maxTry, trialTime, banTime);
  }

  @Override
  protected void ensureProfile(Subject subject) {
    Object principal = subject.getPrincipal();

    if (!subjectProfileService.supportProfile(principal)) {
      return;
    }

    Session subjectSession = subject.getSession(false);
    boolean ensuredProfile = subjectSession != null && subjectSession.getAttribute(ENSURED_PROFILE) != null;
    if (!ensuredProfile) {
      String username = principal.toString();
      log.debug("Ensure HOME folder for {}", username);
      subjectProfileService.ensureProfile(subject.getPrincipals());
      if (subjectSession != null) {
        subjectSession.setAttribute(ENSURED_PROFILE, true);
      }
    }
  }

}
