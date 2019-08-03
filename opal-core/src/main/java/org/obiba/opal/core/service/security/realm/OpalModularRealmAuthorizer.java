/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security.realm;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.apache.shiro.authz.ModularRealmAuthorizer;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.domain.security.SubjectToken;
import org.obiba.opal.core.service.SubjectTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A authorizer that applies a posteriori restrictions to permissions granted to a {@link SubjectToken}.
 */
public class OpalModularRealmAuthorizer extends ModularRealmAuthorizer {

  private static final Logger log = LoggerFactory.getLogger(OpalModularRealmAuthorizer.class);

  private final SubjectTokenService subjectTokenService;

  private final com.google.common.cache.Cache<String, List<String>> projectRestrictionsCache = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.SECONDS)
      .build();

  public OpalModularRealmAuthorizer(Collection<Realm> realms, SubjectTokenService subjectTokenService) {
    super(realms);
    this.subjectTokenService = subjectTokenService;
  }

  @Override
  public boolean isPermitted(PrincipalCollection principals, String permission) {
    boolean superPerm = super.isPermitted(principals, permission);
    return superPerm && isFromTokenRealm(principals) ? isTokenPermitted(principals, permission) : superPerm;
  }

  private boolean isTokenPermitted(PrincipalCollection principals, String permission) {
    log.trace("Token permission on {}", permission);
    String[] parts = permission.split(":");
    if (parts.length < 3) return true;

    String node = parts[1];
    String action = parts[2];

    if (node.startsWith("/project/") || node.startsWith("/datasource/"))
      return isProjectActionPermitted(principals, node, action);

    return true;
  }

  private boolean isProjectActionPermitted(PrincipalCollection principals, String node, String action) {
    String[] elems = node.split("/");
    if (elems.length < 3) return true;

    String basePath = "/" + elems[1] + "/" + elems[2];
    log.trace(" Token project permission base={} action={}", basePath, action);
    List<String> projectRestrictions = getProjectRestrictions(principals);
    if (projectRestrictions.isEmpty()) return true;

    return projectRestrictions.contains(basePath);
  }

  private boolean isFromTokenRealm(PrincipalCollection principals) {
    return principals.getRealmNames().contains(OpalTokenRealm.TOKEN_REALM);
  }

  private List<String> getProjectRestrictions(PrincipalCollection principals) {
    List<String> restrictions = projectRestrictionsCache.getIfPresent(principals.toString());
    if (restrictions == null) {
      restrictions = Lists.newArrayList();
      try {
        String principal = principals.getPrimaryPrincipal().toString();
        String tk = principals.fromRealm(OpalTokenRealm.TOKEN_REALM).iterator().next().toString();
        SubjectToken token = subjectTokenService.getToken(tk, principal);
        for (String project : token.getProjects()) {
          restrictions.add("/datasource/" + project);
          restrictions.add("/project/" + project);
        }
        projectRestrictionsCache.put(principals.toString(), restrictions);
      } catch (Exception e) {
        log.error("Failed at getting subject token", e);
      }
    }
    return restrictions;
  }

}
