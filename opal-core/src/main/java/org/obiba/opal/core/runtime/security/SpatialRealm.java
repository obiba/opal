/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectAclService.Permissions;
import org.obiba.opal.core.service.SubjectAclService.SubjectAclChangeCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import eu.flatwhite.shiro.spatial.SingleSpaceRelationProvider;
import eu.flatwhite.shiro.spatial.SingleSpaceResolver;
import eu.flatwhite.shiro.spatial.Spatial;
import eu.flatwhite.shiro.spatial.SpatialPermissionResolver;
import eu.flatwhite.shiro.spatial.finite.Node;
import eu.flatwhite.shiro.spatial.finite.NodeRelationProvider;
import eu.flatwhite.shiro.spatial.finite.NodeResolver;
import eu.flatwhite.shiro.spatial.finite.NodeSpace;

@Component
public class SpatialRealm extends AuthorizingRealm {

  @Autowired
  private SubjectAclService subjectAclService;

  public SpatialRealm() {
    super();
    super.setPermissionResolver(new SpatialPermissionResolver(new SingleSpaceResolver(new RestSpace()), new NodeResolver(), new SingleSpaceRelationProvider(new NodeRelationProvider())));
  }

  @PostConstruct
  public void registerListener() {
    subjectAclService.addListener(new SubjectAclChangeCallback() {

      @Override
      public void onSubjectAclChanged(String subject) {
        getAuthorizationCache().remove(subject);
      }
    });
  }

  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
    Iterable<String> perms = loadSubjectPermissions(principals);
    if(perms != null) {
      SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
      sai.setStringPermissions(ImmutableSet.copyOf(perms));
      return sai;
    }
    return null;
  }

  @Override
  protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
    return principals.getPrimaryPrincipal().toString();
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return null;
  }

  private Iterable<String> loadSubjectPermissions(PrincipalCollection principals) {
    final List<String> perms = Lists.newArrayList();
    for(Permissions sp : subjectAclService.getSubjectPermissions(principals.getPrimaryPrincipal().toString())) {
      for(String p : sp.getPermissions()) {
        perms.add(sp.getDomain() + ":" + sp.getNode() + ":" + p);
      }
    }
    return perms;
  }

  /**
   * Overriden to make plural form resources part of non-plural form resources.
   * <p>
   * That is, this space considers plural form sub-resources as related to non-plural form sub-resources:
   * 
   * <pre>
   * /parent/kids
   * /parent/kid/1
   * </pre>
   */
  private static class RestSpace extends NodeSpace {
    @Override
    protected double calculateDistance(Spatial s1, Spatial s2) {

      Double d = super.calculateDistance(s1, s2);
      if(Double.isNaN(d)) {
        // Check for plural form relation
        Node n1 = (Node) s1;

        Node n2 = (Node) s2;

        int nodes = Math.min(n1.getPath().size(), n2.getPath().size());
        for(int i = 0; i < nodes; i++) {
          Node lhs = n1.getPath().get(i);
          Node rhs = n2.getPath().get(i);
          if(lhs.getPathElem().equals(rhs.getPathElem()) == false) {
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
