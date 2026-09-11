/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service.security.realm;

import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * One-time password check for the users of Opal's own realms. To be applied once the password has been verified:
 * the OTP challenge (and the temporary secret it may create) must not be reachable by someone who only knows the
 * user name.
 */
@Component
public class OpalOtpRealmHelper {

  @Autowired
  private SubjectProfileService subjectProfileService;

  @Autowired
  private TotpService totpService;

  @Autowired
  private OpalGeneralConfigService configService;

  public void checkOtp(AuthenticationToken token, String username) {
    OpalGeneralConfig config = configService.getConfig();
    if (!config.hasOtpStrategy() || !"TOTP".equals(config.getOtpStrategy())) return;

    SubjectProfile profile;
    try {
      profile = subjectProfileService.getProfile(username);
    } catch (NoSuchSubjectProfileException e) {
      // first login, the profile does not exist yet
      return;
    }

    String header = "X-Opal-" + config.getOtpStrategy();
    String code = token instanceof UsernamePasswordOtpToken otpToken ? otpToken.getOtp() : null;
    if (profile.hasSecret()) {
      if (Strings.isNullOrEmpty(code)) {
        throw new NoSuchOtpException(header);
      }
      if (!totpService.validateCode(code, profile.getSecret())) {
        throw new AuthenticationException("Wrong TOTP");
      }
    } else if (profile.hasTmpSecret()) {
      if (Strings.isNullOrEmpty(code)) {
        throw new NoSuchOtpException(header, getQrImage(profile), false);
      }
      if (!totpService.validateCode(code, profile.getTmpSecret())) {
        throw new AuthenticationException("Wrong TOTP");
      }
      // this will make the temporary secret permanent
      subjectProfileService.updateProfileSecret(profile.getPrincipal(), true);
    } else if (config.isEnforced2FA()) {
      // make a temporary secret
      subjectProfileService.updateProfileTmpSecret(profile.getPrincipal(), true);
      profile = subjectProfileService.getProfile(username);
      throw new NoSuchOtpException(header, getQrImage(profile), false);
    }
    // else 2FA not activated
  }

  private String getQrImage(SubjectProfile profile) {
    return totpService.getQrImageDataUri(profile.getPrincipal(), profile.getTmpSecret());
  }
}
