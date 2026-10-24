/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.realm.OpalOIDCRealm;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * Perform the authentication, either by username-password token or by obiba ticket token.
 */
@Component
public class AuthenticationExecutorImpl extends AbstractAuthenticationExecutor implements InitializingBean {

  @Value("${org.obiba.opal.security.login.maxTry}")
  private int maxTry;

  @Value("${org.obiba.opal.security.login.trialTime}")
  private int trialTime;

  @Value("${org.obiba.opal.security.login.banTime}")
  private int banTime;

  @Value("${org.obiba.opal.server.context-path}")
  private String contextPath;

  private static final Logger log = LoggerFactory.getLogger(AuthenticationExecutorImpl.class);

  private static final String ENSURED_PROFILE = "ensuredProfile";

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Override
  public void afterPropertiesSet() throws Exception {
    configure();
  }

  public void configure() {
    configureBan(maxTry, trialTime, banTime);
  }

  @Override
  public String getContextPath() {
    return contextPath;
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
      oidcGroups(subject.getPrincipals()).ifPresent(groups -> subjectProfileService.applyProfileGroups(username, groups));
      if (subjectSession != null) {
        subjectSession.setAttribute(ENSURED_PROFILE, true);
      }
    }
  }

  /**
   * The groups of an OIDC login are known from its UserInfo: store them now, so that the profile does not wait for the
   * first authorized request of the session to catch up with the ID provider (obiba/opal#4235).
   */
  private Optional<Set<String>> oidcGroups(PrincipalCollection principals) {
    if (!(SecurityUtils.getSecurityManager() instanceof RealmSecurityManager manager)) return Optional.empty();
    return manager.getRealms().stream()
        .filter(realm -> realm instanceof OpalOIDCRealm && principals.getRealmNames().contains(realm.getName()))
        .findFirst()
        .map(realm -> Set.copyOf(((OpalOIDCRealm) realm).getGroups(principals)));
  }
}
