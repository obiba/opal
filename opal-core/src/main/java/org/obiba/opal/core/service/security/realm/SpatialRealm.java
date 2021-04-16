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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import eu.flatwhite.shiro.spatial.SingleSpaceRelationProvider;
import eu.flatwhite.shiro.spatial.SingleSpaceResolver;
import eu.flatwhite.shiro.spatial.Spatial;
import eu.flatwhite.shiro.spatial.SpatialPermissionResolver;
import eu.flatwhite.shiro.spatial.finite.Node;
import eu.flatwhite.shiro.spatial.finite.NodeRelationProvider;
import eu.flatwhite.shiro.spatial.finite.NodeResolver;
import eu.flatwhite.shiro.spatial.finite.NodeSpace;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.core.service.security.event.SubjectAclChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.obiba.opal.core.domain.security.SubjectAcl.Subject;
import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
public class SpatialRealm extends AuthorizingRealm implements RolePermissionResolver {

  private static final Logger log = LoggerFactory.getLogger(SpatialRealm.class);

  private final SubjectAclService subjectAclService;

  private final RolePermissionResolver rolePermissionResolver;

  private final SubjectPermissionsConverterRegistry subjectPermissionsConverterRegistry;

  private Cache<Subject, Collection<Permission>> rolePermissionCache;

  @Autowired
  public SpatialRealm(SubjectAclService subjectAclService, SubjectPermissionsConverterRegistry subjectPermissionsConverterRegistry) {
    if (subjectAclService == null) throw new IllegalArgumentException("subjectAclService cannot be null");
    this.subjectAclService = subjectAclService;
    this.subjectPermissionsConverterRegistry = subjectPermissionsConverterRegistry;

    setPermissionResolver(new SpatialPermissionResolver(new SingleSpaceResolver(new RestSpace()), new NodeResolver(),
        new SingleSpaceRelationProvider(new NodeRelationProvider())));
    rolePermissionResolver = new GroupPermissionResolver();
  }

  @Override
  public boolean supports(AuthenticationToken token) {
    // This realm is not used for authentication
    return false;
  }

  @Subscribe
  public void onSubjectAclChangedEvent(SubjectAclChangedEvent event) {
    if (isAuthorizationCachingEnabled()) {
      log.info("Clear cache perms for {}", event.getSubject());
      getAuthorizationCache().remove(event.getSubject());
      getRolePermissionCache().remove(event.getSubject());
    }
  }

  @Override
  public Collection<Permission> resolvePermissionsInRole(String roleString) {
    return getRolePermissionResolver().resolvePermissionsInRole(roleString);
  }

  /**
   * Overridden because the OpalSecurityManager sets {@code this} as the {@code RolePermissionResolver} on all configured
   * realms. This results the following object graph:
   * <p/>
   * <pre>
   * AuthorizingReam.rolePermissionResolver -> SpatialRealm (this)
   *      ^
   *      |
   * SpatialRealm.rolePermissionResolver -> GroupPermissionResolver
   *
   * <pre>
   * By overriding this method, we prevent an infinite loop from occurring when
   * {@code getRolePermissionResolver().resolvePermissionsInRole()} is called.
   */
  @Override
  public RolePermissionResolver getRolePermissionResolver() {
    return rolePermissionResolver;
  }

  protected Cache<Subject, Collection<Permission>> getRolePermissionCache() {
    return rolePermissionCache;
  }

  @Override
  protected void afterCacheManagerSet() {
    super.afterCacheManagerSet();
    if (isAuthorizationCachingEnabled()) {
      CacheManager cacheManager = getCacheManager();
      rolePermissionCache = cacheManager.getCache(getAuthorizationCacheName() + "_role");
    }
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Iterable<String> perms = loadSubjectPermissions(principals);
    if (perms == null) return null;
    SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
    sai.setStringPermissions(ImmutableSet.copyOf(perms));
    return sai;
  }

  @Override
  protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
    return getSubject(principals);
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return null;
  }

  private Iterable<String> loadSubjectPermissions(Subject subject) {
    return subjectPermissionsConverterRegistry.convert(subjectAclService.getSubjectPermissions(subject));
  }

  private Iterable<String> loadSubjectPermissions(PrincipalCollection principals) {
    return loadSubjectPermissions(getSubject(principals));
  }

  private Subject getSubject(PrincipalCollection principals) {
    return SubjectType.USER.subjectFor(principals.getPrimaryPrincipal().toString());
  }

  private final class GroupPermissionResolver implements RolePermissionResolver {

    @Override
    public Collection<Permission> resolvePermissionsInRole(String roleString) {
      Subject group = SubjectType.GROUP.subjectFor(roleString);
      appendRoleToSession(roleString);
      if (isAuthorizationCachingEnabled() && getRolePermissionCache() != null) {
        Collection<Permission> cached = getRolePermissionCache().get(group);
        if (cached == null) {
          cached = doGetGroupPermissions(group);
          getRolePermissionCache().put(group, cached);
        }
        return cached;
      }
      return doGetGroupPermissions(group);
    }

    private void appendRoleToSession(String roleString) {
      if (SecurityUtils.getSubject().getPrincipals().getRealmNames().contains(OpalTokenRealm.TOKEN_REALM)) return;
      Session session = SecurityUtils.getSubject().getSession(false);
      if (session != null) {
        Set<String> roles = (Set<String>) session.getAttribute("roles");
        if (roles == null) {
          roles = Sets.newHashSet();
        }
        roles.add(roleString);
        session.setAttribute("roles", roles);
      }
    }

    private Collection<Permission> doGetGroupPermissions(Subject group) {
      if ("admin".equals(group.getPrincipal())) {
        return Lists.newArrayList(new AllPermission());
      }
      return StreamSupport.stream(loadSubjectPermissions(group).spliterator(), false)
          .map(from -> getPermissionResolver().resolvePermission(from))
          .collect(Collectors.toList());
    }

  }

  /**
   * Overriden to make plural form resources part of non-plural form resources.
   * <p/>
   * That is, this space considers plural form sub-resources as related to non-plural form sub-resources:
   * <p/>
   * <pre>
   * /parent/kids
   * /parent/kid/1
   * </pre>
   */
  static class RestSpace extends NodeSpace {

    private static final long serialVersionUID = -9002715059388992984L;

    @Override
    protected double calculateDistance(Spatial s1, Spatial s2) {
      Double d = super.calculateDistance(s1, s2);
      if (Double.isNaN(d)) {
        // Check for plural form relation
        Node n1 = (Node) s1;
        Node n2 = (Node) s2;
        int nodes = Math.min(n1.getPath().size(), n2.getPath().size());
        for (int i = 0; i < nodes; i++) {
          Node lhs = n1.getPath().get(i);
          Node rhs = n2.getPath().get(i);
          if (!lhs.getPathElem().equals(rhs.getPathElem())) {
            // Check for plural form
            return isPluralForm(lhs, rhs) ? 1 : d;
          }
        }
      }
      return d;
    }

    /**
     * Returns true when lhs is the plural form of rhs (or vice-versa), that is their node element text is identical
     * except for an additional 's' in the other node.
     *
     * @param lhs
     * @param rhs
     */
    private boolean isPluralForm(Node lhs, Node rhs) {
      String a = lhs.getPathElem();
      String b = rhs.getPathElem();
      return a.equals(b + 's') || b.equals(a + 's');
    }
  }
}
