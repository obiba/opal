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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OpalIniRealm extends IniRealm {

  private static final String INI_REALM = "opal-ini-realm";

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
}
