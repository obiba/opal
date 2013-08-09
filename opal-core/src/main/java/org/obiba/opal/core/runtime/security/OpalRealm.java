/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import java.util.Collection;

import javax.sql.DataSource;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OpalRealm extends JdbcRealm {

  @Autowired
  public OpalRealm(@Qualifier("opal-config") DataSource opalDataSource) {
    setDataSource(opalDataSource);
    setAuthenticationQuery("select password from user where name = ? and enabled is true");
    setUserRolesQuery(
        "select g.name from user_groups as ug, user as u, groups as g where u.name = ? and u.id = ug.user_id and ug.group_id = g.id");
    setCacheManager(new MemoryConstrainedCacheManager());
    setCredentialsMatcher(new HashedCredentialsMatcher("SHA"));
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection thisPrincipals = principals.fromRealm(getName());
    if(thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Object primary = thisPrincipals.iterator().next();
      return super.doGetAuthorizationInfo(new SimplePrincipalCollection(primary, getName()));
    } else return new SimpleAuthorizationInfo();
  }

}
