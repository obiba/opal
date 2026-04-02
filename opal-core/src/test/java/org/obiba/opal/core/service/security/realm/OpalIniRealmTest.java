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

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obiba.opal.core.service.security.realm.OpalIniRealm.quoteShiro2CryptPassword;

public class OpalIniRealmTest {

  // The actual argon2id hash from shiro.ini.bak / the reported bug
  private static final String ARGON2_HASH_WITH_ROLE =
      "$shiro2$argon2id$v=19$t=1,m=65536,p=4$q4uNaPW6sY1mDaLvlRcXPw$/zeLc6+GYP9//fahi+8qDj/a1KlJLvtQjRRTvbOePb0,admin";

  private static final String ARGON2_HASH_NO_ROLE =
      "$shiro2$argon2id$v=19$t=1,m=65536,p=4$q4uNaPW6sY1mDaLvlRcXPw$/zeLc6+GYP9//fahi+8qDj/a1KlJLvtQjRRTvbOePb0";

  private static final String ARGON2_HASH_WITH_MULTIPLE_ROLES =
      "$shiro2$argon2id$v=19$t=1,m=65536,p=4$q4uNaPW6sY1mDaLvlRcXPw$/zeLc6+GYP9//fahi+8qDj/a1KlJLvtQjRRTvbOePb0,admin,manager";

  private static final String SHIRO1_HASH_WITH_ROLE =
      "$shiro1$SHA-256$500000$dxucP0IgyO99rdL0Ltj1Qg==$qssS60kTC7TqE61/JFrX/OEk0jsZbYXjiGhR7/t+XNY=,admin";

  // --- null / pass-through cases ---

  @Test
  public void testNull_returnsNull() {
    assertThat(quoteShiro2CryptPassword(null)).isNull();
  }

  @Test
  public void testShiro1Hash_isNotModified() {
    assertThat(quoteShiro2CryptPassword(SHIRO1_HASH_WITH_ROLE)).isEqualTo(SHIRO1_HASH_WITH_ROLE);
  }

  @Test
  public void testPlainText_isNotModified() {
    assertThat(quoteShiro2CryptPassword("password")).isEqualTo("password");
  }

  // --- core fix: shiro2 argon2 hash with roles ---

  @Test
  public void testArgon2HashWithRole_hashIsQuoted() {
    String result = quoteShiro2CryptPassword(ARGON2_HASH_WITH_ROLE);

    // The password portion must be wrapped in double quotes
    assertThat(result).startsWith("\"$shiro2$");
    // The role delimiter comes *after* the closing quote
    assertThat(result).contains("\",admin");
    // The hash data must not be truncated at the internal commas
    assertThat(result).contains("t=1,m=65536,p=4");
  }

  @Test
  public void testArgon2HashWithRole_fullExpectedValue() {
    String expected =
        "\"$shiro2$argon2id$v=19$t=1,m=65536,p=4$q4uNaPW6sY1mDaLvlRcXPw$/zeLc6+GYP9//fahi+8qDj/a1KlJLvtQjRRTvbOePb0\",admin";
    assertThat(quoteShiro2CryptPassword(ARGON2_HASH_WITH_ROLE)).isEqualTo(expected);
  }

  @Test
  public void testArgon2HashWithMultipleRoles_onlyHashIsQuoted() {
    String result = quoteShiro2CryptPassword(ARGON2_HASH_WITH_MULTIPLE_ROLES);

    assertThat(result).startsWith("\"$shiro2$");
    // Roles remain outside the quotes and are still comma-separated
    assertThat(result).contains("\",admin,manager");
  }

  // --- no roles: no quoting needed ---

  @Test
  public void testArgon2HashNoRole_isNotModified() {
    // No comma after the last '$', so the value must pass through unchanged
    assertThat(quoteShiro2CryptPassword(ARGON2_HASH_NO_ROLE)).isEqualTo(ARGON2_HASH_NO_ROLE);
  }

  // --- edge cases ---

  @Test
  public void testIncompleteShiro2Value_isNotModified() {
    // Malformed: starts with $shiro2$ but no closing hash segment
    String incomplete = "$shiro2$argon2id";
    assertThat(quoteShiro2CryptPassword(incomplete)).isEqualTo(incomplete);
  }

  @Test
  public void testShiro2HashEndsWithDollar_isNotModified() {
    // Last char is '$', meaning there is no hash data at all
    String endsWithDollar = "$shiro2$argon2id$v=19$t=1,m=65536,p=4$salt$";
    assertThat(quoteShiro2CryptPassword(endsWithDollar)).isEqualTo(endsWithDollar);
  }
}

