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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectAclService.SubjectPermission;
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
    super(new MemoryConstrainedCacheManager());
    // super.setPermissionResolver(new SpatialPermissionResolver(new MapSpaceResolver(ImmutableMap.<String, Space>
    // of("magma", new NodeSpace(), "ws", new WsSpace())), new NodeResolver(), new SingleSpaceRelationProvider(new
    // NodeRelationProvider())));
    super.setPermissionResolver(new SpatialPermissionResolver(new SingleSpaceResolver(new NodeSpace()), new NodeResolver(), new SingleSpaceRelationProvider(new NodeRelationProvider())));
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
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    return null;
  }

  private Iterable<String> loadSubjectPermissions(PrincipalCollection principals) {
    final List<String> perms = Lists.newArrayList();
    for(SubjectPermission sp : subjectAclService.getSubjectPermissions(principals.getPrimaryPrincipal().toString())) {
      for(String p : sp.getPermissions()) {
        perms.add(sp.getDomain() + ":" + sp.getNode() + ":" + p);
      }
    }
    return perms;
  }

  // Unused for now
  private static class WsSpace extends NodeSpace {

    @Override
    public Spatial project(Spatial spatial) {
      if(spatial.getSpace() instanceof NodeSpace && spatial.getSpace() != this) {
        Node node = (Node) spatial;
        List<Node> magmaPath = node.getPath();
        List<String> wsPath = new ArrayList<String>();
        switch(magmaPath.size()) {
        case 3:
          wsPath.add(magmaPath.get(2).getPathElem());
          wsPath.add("variable");
        case 2:
          wsPath.add(magmaPath.get(1).getPathElem());
          wsPath.add("table");
        case 1:
          wsPath.add(magmaPath.get(0).getPathElem());
          wsPath.add("datasource");
          break;
        default:
          return null;
        }
        Collections.reverse(wsPath);

        Node wsNode = getOrigin();
        for(String s : wsPath) {
          wsNode = new Node(wsNode, s);
        }
        return wsNode;
      }
      return super.project(spatial);
    }
  }
}
