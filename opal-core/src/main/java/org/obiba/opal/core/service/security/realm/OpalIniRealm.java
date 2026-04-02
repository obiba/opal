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

import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.realm.text.IniRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpalIniRealm extends IniRealm {

  private static final String INI_REALM = "opal-ini-realm";

  private static final String SHIRO2_PREFIX = "$shiro2$";

  @Autowired
  public OpalIniRealm(RolePermissionResolver rolePermissionResolver) {
    super("classpath:shiro.ini");
    setPermissionResolver(new OpalPermissionResolver());
    setRolePermissionResolver(rolePermissionResolver);
    setCredentialsMatcher(new PasswordMatcher());
  }

  @Override
  public String getName() {
    return INI_REALM;
  }

  /**
   * Overrides the default user definition processing to handle Shiro 2's Argon2 hash format.
   * <p>
   * The Argon2 crypt format contains commas in the parameters section
   * (e.g. {@code $shiro2$argon2id$v=19$t=1,m=65536,p=4$salt$hash}). The parent implementation
   * splits the entire value by comma to separate the password from roles, which incorrectly
   * truncates the hash at the first comma in the Argon2 parameters.
   * </p>
   * <p>
   * This override wraps {@code $shiro2$} hashed passwords in double quotes before delegating
   * to the parent, leveraging the quote-aware splitting in {@code StringUtils.split()}.
   * </p>
   */
  @Override
  protected void processUserDefinitions(Map<String, String> userDefs) {
    if (userDefs == null || userDefs.isEmpty()) {
      super.processUserDefinitions(userDefs);
      return;
    }
    Map<String, String> fixedDefs = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : userDefs.entrySet()) {
      fixedDefs.put(entry.getKey(), quoteShiro2CryptPassword(entry.getValue()));
    }
    super.processUserDefinitions(fixedDefs);
  }

  /**
   * Wraps a {@code $shiro2$} hashed password in double quotes to protect internal commas
   * from being interpreted as role delimiters.
   * <p>
   * The Shiro 2 Argon2 crypt string has exactly 6 {@code $} delimiters:
   * {@code $shiro2$algorithm$version$params$salt$hashdata}. After the hash data,
   * a comma starts the role definitions.
   * </p>
   */
  private String quoteShiro2CryptPassword(String value) {
    if (value == null || !value.startsWith(SHIRO2_PREFIX)) {
      return value;
    }
    // Count 6 '$' delimiters to find the start of the hash data section
    int dollarCount = 0;
    int idx = 0;
    while (idx < value.length() && dollarCount < 6) {
      if (value.charAt(idx) == '$') {
        dollarCount++;
      }
      idx++;
    }
    if (dollarCount < 6) {
      return value;
    }
    // idx now points to the first character of the base64 hash data.
    // Base64 (without padding) uses [A-Za-z0-9+/], so a comma signals the role separator.
    int commaIdx = value.indexOf(',', idx);
    if (commaIdx == -1) {
      // No roles after the hash, no quoting needed
      return value;
    }
    return "\"" + value.substring(0, commaIdx) + "\"" + value.substring(commaIdx);
  }
}
