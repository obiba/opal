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

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.obiba.shiro.web.filter.NoSuchOtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

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

  @Autowired
  private TotpService totpService;

  @Autowired
  private OpalGeneralConfigService configService;

  @PostConstruct
  public void configure() {
    configureBan(maxTry, trialTime, banTime);
  }

  @Override
  protected void processRequest(HttpServletRequest request, AuthenticationToken token) {
    if (token.getPrincipal() instanceof String && token.getCredentials() != null) {
      OpalGeneralConfig config = configService.getConfig();
      if (config.hasOtpStrategy()) {
        String otpHeader = request.getHeader("X-Opal-" + config.getOtpStrategy());
        validateOtp(config.getOtpStrategy(), otpHeader, token);
      }
    }
    super.processRequest(request, token);
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

  private void validateOtp(String strategy, String code, AuthenticationToken token) {
    String username = token.getPrincipal().toString();
    try {
      SubjectProfile profile = subjectProfileService.getProfile(username);
      if (profile.hasSecret() && "TOTP".equals(strategy)) {
        if (Strings.isNullOrEmpty(code)) {
          throw new NoSuchOtpException(strategy);
        }
        if (!totpService.validateCode(code, profile.getSecret())) {
           throw new AuthenticationException("Wrong TOTP");
        }
      } // else 2FA not activated
    } catch (NoSuchSubjectProfileException e) {
      // first login or wrong username
    }
  }

}
