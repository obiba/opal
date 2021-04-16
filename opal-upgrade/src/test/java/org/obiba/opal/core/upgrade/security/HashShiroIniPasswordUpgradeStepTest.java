/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.upgrade.security;

import java.io.File;
import java.util.Map;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.config.Ini;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.upgrade.v2_0_x.HashShiroIniPasswordUpgradeStep;
import org.springframework.util.ResourceUtils;

import com.google.common.collect.ImmutableMap;

import static org.fest.assertions.api.Assertions.assertThat;

public class HashShiroIniPasswordUpgradeStepTest {

  private static final Map<String, String> usernamePassword = ImmutableMap.of( //
      "user1", "password$as123*(sd)",//
      "user2", "1234password", //
      "user3", "this is a password");

  private File iniFile;

  private File destIniFile;

  private File iniBackup;

  private final HashShiroIniPasswordUpgradeStep upgradeStep = new HashShiroIniPasswordUpgradeStep();

  private final PasswordService passwordService = new DefaultPasswordService();

  @Before
  public void setUp() throws Exception {
    iniFile = ResourceUtils.getFile("classpath:org/obiba/opal/core/upgrade/security/shiro-password.ini");
    destIniFile = ResourceUtils.getFile(iniFile.getAbsolutePath() + ".hashed");
    iniBackup = ResourceUtils.getFile(iniFile.getAbsolutePath() + ".opal1-backup");

    upgradeStep.setSrcIniFile(iniFile);
    upgradeStep.setDestIniFile(destIniFile);
  }

  @Test
  public void testExecute() throws Exception {

    String iniFileMd5 = new Md5Hash(iniFile).toHex();

    upgradeStep.execute(null);

    assertThat(iniBackup.exists()).isTrue();
    assertThat(new Md5Hash(iniBackup).toHex()).isEqualTo(iniFileMd5);
    assertThat(destIniFile.exists()).isTrue();
    assertThat(new Md5Hash(destIniFile).toHex()).isNotEqualTo(iniFileMd5);

    Ini ini = new Ini();
    ini.loadFromPath(destIniFile.getAbsolutePath());
    Ini.Section section = ini.getSection(IniRealm.USERS_SECTION_NAME);
    assertThat(section).isNotNull();
    assertThat(section).hasSize(3);
    for(String username : usernamePassword.keySet()) {
      assertThat(section.containsKey(username)).isTrue();
      String[] passwordAndRolesArray = StringUtils.split(section.get(username));
      String encrypted = passwordAndRolesArray[0];
      assertThat(passwordService.passwordsMatch(usernamePassword.get(username), encrypted)).isTrue();
    }

  }

}
