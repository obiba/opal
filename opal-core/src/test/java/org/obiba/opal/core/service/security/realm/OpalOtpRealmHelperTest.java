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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.domain.OpalGeneralConfig;
import org.obiba.opal.core.domain.security.SubjectProfile;
import org.obiba.opal.core.service.NoSuchSubjectProfileException;
import org.obiba.opal.core.service.OpalGeneralConfigService;
import org.obiba.opal.core.service.SubjectProfileService;
import org.obiba.opal.core.service.security.TotpService;
import org.obiba.shiro.NoSuchOtpException;
import org.obiba.shiro.authc.UsernamePasswordOtpToken;
import org.springframework.test.util.ReflectionTestUtils;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class OpalOtpRealmHelperTest {

  private static final String USERNAME = "jdoe";

  private SubjectProfileService subjectProfileService;

  private TotpService totpService;

  private OpalGeneralConfig config;

  private OpalOtpRealmHelper helper;

  private SubjectProfile profile;

  @Before
  public void setUp() {
    subjectProfileService = mock(SubjectProfileService.class);
    totpService = mock(TotpService.class);
    OpalGeneralConfigService configService = mock(OpalGeneralConfigService.class);
    config = new OpalGeneralConfig();
    config.setOtpStrategy("TOTP");
    when(configService.getConfig()).thenReturn(config);

    profile = new SubjectProfile(USERNAME, OpalUserRealm.OPAL_REALM);
    when(subjectProfileService.getProfile(USERNAME)).thenReturn(profile);

    helper = new OpalOtpRealmHelper();
    ReflectionTestUtils.setField(helper, "subjectProfileService", subjectProfileService);
    ReflectionTestUtils.setField(helper, "totpService", totpService);
    ReflectionTestUtils.setField(helper, "configService", configService);
  }

  @Test
  public void no2FAPassesWithoutCode() {
    helper.checkOtp(new UsernamePasswordToken(USERNAME, "pwd"), USERNAME);
    verifyZeroInteractions(totpService);
  }

  @Test
  public void firstLoginWithoutProfilePasses() {
    config.setEnforced2FA(true);
    when(subjectProfileService.getProfile(USERNAME)).thenThrow(new NoSuchSubjectProfileException(USERNAME));
    helper.checkOtp(new UsernamePasswordToken(USERNAME, "pwd"), USERNAME);
  }

  @Test(expected = NoSuchOtpException.class)
  public void secretWithoutCodeIsChallenged() {
    profile.setSecret("abc");
    helper.checkOtp(new UsernamePasswordToken(USERNAME, "pwd"), USERNAME);
  }

  @Test
  public void secretWithWrongCodeFails() {
    profile.setSecret("abc");
    when(totpService.validateCode("000000", "abc")).thenReturn(false);
    try {
      helper.checkOtp(new UsernamePasswordOtpToken(USERNAME, "pwd", "000000"), USERNAME);
      fail("expected AuthenticationException");
    } catch (AuthenticationException e) {
      assertThat(e).isNotInstanceOf(NoSuchOtpException.class);
    }
  }

  @Test
  public void secretWithValidCodePasses() {
    profile.setSecret("abc");
    when(totpService.validateCode("123456", "abc")).thenReturn(true);
    helper.checkOtp(new UsernamePasswordOtpToken(USERNAME, "pwd", "123456"), USERNAME);
  }

  @Test
  public void enforcedWithoutSecretIssuesTemporarySecret() {
    config.setEnforced2FA(true);
    SubjectProfile withTmp = new SubjectProfile(USERNAME, OpalUserRealm.OPAL_REALM);
    withTmp.setTmpSecret("tmp");
    when(subjectProfileService.getProfile(USERNAME)).thenReturn(profile, withTmp);
    when(totpService.getQrImageDataUri(USERNAME, "tmp")).thenReturn("data:qr");
    try {
      helper.checkOtp(new UsernamePasswordToken(USERNAME, "pwd"), USERNAME);
      fail("expected NoSuchOtpException");
    } catch (NoSuchOtpException e) {
      assertThat(e.getQrImage()).isEqualTo("data:qr");
    }
    verify(subjectProfileService).updateProfileTmpSecret(USERNAME, true);
  }

  @Test
  public void temporarySecretWithValidCodeIsConfirmed() {
    profile.setTmpSecret("tmp");
    when(totpService.validateCode("123456", "tmp")).thenReturn(true);
    helper.checkOtp(new UsernamePasswordOtpToken(USERNAME, "pwd", "123456"), USERNAME);
    verify(subjectProfileService).updateProfileSecret(USERNAME, true);
  }

  @Test
  public void temporarySecretWithWrongCodeIsNotConfirmed() {
    profile.setTmpSecret("tmp");
    when(totpService.validateCode("000000", "tmp")).thenReturn(false);
    try {
      helper.checkOtp(new UsernamePasswordOtpToken(USERNAME, "pwd", "000000"), USERNAME);
      fail("expected AuthenticationException");
    } catch (AuthenticationException e) {
      // expected
    }
    verify(subjectProfileService, never()).updateProfileSecret(anyString(), anyBoolean());
  }
}
