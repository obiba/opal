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

import com.google.common.collect.Sets;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Realm for users having subject credentials.
 */
public abstract class OpalBaseRealm extends AuthorizingRealm {

  @Autowired
  protected SubjectCredentialsService subjectCredentialsService;

  /**
   * Get the groups from the stored subject credentials.
   *
   * @param principals
   * @return
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Collection<?> thisPrincipals = principals.fromRealm(getName());
    if (thisPrincipals != null && !thisPrincipals.isEmpty()) {
      Object primary = thisPrincipals.iterator().next();
      PrincipalCollection simplePrincipals = new SimplePrincipalCollection(primary, getName());

      Set<String> roleNames = Sets.newHashSet(getName());
      String username = (String) getAvailablePrincipal(simplePrincipals);
      SubjectCredentials subjectCredentials = subjectCredentialsService.getSubjectCredentials(username);
      if (subjectCredentials != null) {
        roleNames.addAll(subjectCredentials.getGroups());
      }
      return new SimpleAuthorizationInfo(roleNames);

    }
    return new SimpleAuthorizationInfo();
  }

}
