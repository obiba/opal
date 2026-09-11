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

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * The password must be verified before anything related to the one-time password (issue #4198).
 */
public class OpalUserRealmTest {

  private static final String USERNAME = "jdoe";
  private static final String PASSWORD = "secret";
  private static final String SALT = "salt";
  private static final int ITERATIONS = 2;

  private OpalOtpRealmHelper otpHelper;

  private OpalUserRealm realm;

  @Before
  public void setUp() {
    OpalConfiguration configuration = mock(OpalConfiguration.class);
    when(configuration.getSecretKey()).thenReturn(SALT);
    OpalConfigurationService configurationService = mock(OpalConfigurationService.class);
    when(configurationService.getOpalConfiguration()).thenReturn(configuration);

    SubjectCredentialsService subjectCredentialsService = mock(SubjectCredentialsService.class);
    when(subjectCredentialsService.getSubjectCredentials(USERNAME)).thenReturn(SubjectCredentials.Builder.create()
        .name(USERNAME)
        .authenticationType(SubjectCredentials.AuthenticationType.PASSWORD)
        .password(new Sha512Hash(PASSWORD, SALT, ITERATIONS).toHex())
        .enabled(true)
        .build());

    otpHelper = mock(OpalOtpRealmHelper.class);

    realm = new OpalUserRealm();
    ReflectionTestUtils.setField(realm, "nbHashIterations", ITERATIONS);
    ReflectionTestUtils.setField(realm, "opalConfigurationService", configurationService);
    ReflectionTestUtils.setField(realm, "subjectCredentialsService", subjectCredentialsService);
    ReflectionTestUtils.setField(realm, "otpHelper", otpHelper);
    realm.afterPropertiesSet();
  }

  @Test
  public void wrongPasswordFailsBeforeOtpCheck() {
    try {
      realm.getAuthenticationInfo(new UsernamePasswordToken(USERNAME, "wrong"));
      org.fest.assertions.api.Assertions.fail("expected IncorrectCredentialsException");
    } catch (IncorrectCredentialsException e) {
      // expected
    }
    verifyZeroInteractions(otpHelper);
  }

  @Test
  public void rightPasswordTriggersOtpCheck() {
    UsernamePasswordToken token = new UsernamePasswordToken(USERNAME, PASSWORD);
    assertThat(realm.getAuthenticationInfo(token)).isNotNull();
    verify(otpHelper).checkOtp(any(AuthenticationToken.class), eq(USERNAME));
  }
}
