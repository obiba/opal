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
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.web.filter.AbstractAuthenticationExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.StreamSupport;

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

  @Autowired
  private TotpService totpService;

  @Autowired
  private OpalGeneralConfigService configService;

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
  protected void processRequest(HttpServletRequest request, AuthenticationToken token) {
    if (token.getPrincipal() instanceof String && token.getCredentials() != null) {
      OpalGeneralConfig config = configService.getConfig();
      if (config.hasOtpStrategy()) {
        String otpHeader = request.getHeader("X-Opal-" + config.getOtpStrategy());
        validateOtp(config.getOtpStrategy(), otpHeader, token, config.isEnforced2FA());
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

  private void validateOtp(String strategy, String code, AuthenticationToken token, boolean enforced2FA) {
    String username = token.getPrincipal().toString();
    try {
      SubjectProfile profile = subjectProfileService.getProfile(username);
      boolean otpRealm = StreamSupport.stream(profile.getRealms().spliterator(), false)
          .anyMatch(realm -> realm.equals("opal-user-realm") || realm.equals("opal-ini-realm"));
      if ("TOTP".equals(strategy) && otpRealm) {
        if (profile.hasSecret()) {
          if (Strings.isNullOrEmpty(code)) {
            throw new NoSuchOtpException("X-Opal-" + strategy);
          }
          if (!totpService.validateCode(code, profile.getSecret())) {
            throw new AuthenticationException("Wrong TOTP");
          }
        } else if (profile.hasTmpSecret()) {
          if (Strings.isNullOrEmpty(code)) {
            throw new NoSuchOtpException("X-Opal-" + strategy, totpService.getQrImageDataUri(profile.getPrincipal(), profile.getTmpSecret()));
          }
          if (!totpService.validateCode(code, profile.getTmpSecret())) {
            throw new AuthenticationException("Wrong TOTP");
          }
          // this will make the temporary secret permanent
          subjectProfileService.updateProfileSecret(profile.getPrincipal(), true);
        } else if (enforced2FA) {
          // make a temporary secret
          subjectProfileService.updateProfileTmpSecret(profile.getPrincipal(), true);
          profile = subjectProfileService.getProfile(username);
          throw new NoSuchOtpException("X-Opal-" + strategy, totpService.getQrImageDataUri(profile.getPrincipal(), profile.getTmpSecret()));
        }
        // else 2FA not activated
      }

    } catch (NoSuchSubjectProfileException e) {
      // first login or wrong username
    }
  }

}
