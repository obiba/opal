/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.core.service.SubjectAclService.Permissions;
import org.obiba.opal.web.magma.support.InvalidRequestException;
import org.obiba.opal.web.model.Opal.Acl;
import org.obiba.opal.web.model.Opal.Acls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/authz/query")
public class AuthorizationQueryResource {

  private final SubjectAclService subjectAclService;

  @Autowired
  public AuthorizationQueryResource(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  public Iterable<Acls> get(@QueryParam("node") List<String> nodes, @QueryParam("by") String by) {
    if(nodes == null || nodes.size() == 0) throw new InvalidRequestException("At least one 'node' query param expected.");

    return getAclsGroupedBySubject(nodes);
  }

  private Iterable<Acls> getAclsGroupedBySubject(List<String> nodes) {
    Map<String, Acls.Builder> aclMap = new HashMap<String, Acls.Builder>();

    for(String node : nodes) {
      for(Acl acl : Iterables.transform(subjectAclService.getNodePermissions("magma", node), PermissionsToAclFunction.INSTANCE)) {
        Acls.Builder acls;
        if(aclMap.containsKey(acl.getPrincipal())) {
          acls = aclMap.get(acl.getPrincipal());
        } else {
          acls = Acls.newBuilder().setName(acl.getPrincipal());
          aclMap.put(acl.getPrincipal(), acls);
        }
        acls.addAcls(acl);
      }
    }

    List<Acls.Builder> builders = Lists.newLinkedList(aclMap.values());
    Collections.sort(builders, new Comparator<Acls.Builder>() {

      @Override
      public int compare(Acls.Builder b1, Acls.Builder b2) {
        return b1.getName().compareTo(b2.getName());
      }

    });

    return Iterables.transform(builders, AclsBuilderFunction.INSTANCE);
  }

  private static final class AclsBuilderFunction implements Function<Acls.Builder, Acls> {

    private static final AclsBuilderFunction INSTANCE = new AclsBuilderFunction();

    @Override
    public Acls apply(Acls.Builder from) {
      return from.build();
    }
  }

  private static class PermissionsToAclFunction implements Function<Permissions, Acl> {

    private static final PermissionsToAclFunction INSTANCE = new PermissionsToAclFunction();

    @Override
    public Acl apply(Permissions from) {
      return Acl.newBuilder().setPrincipal(from.getSubject()).setResource(from.getNode()).addAllActions(from.getPermissions()).build();
    }

  }

}
