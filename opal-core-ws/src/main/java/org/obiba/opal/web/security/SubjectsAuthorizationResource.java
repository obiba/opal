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
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.Acl;
import org.obiba.opal.web.model.Opal.SubjectAcls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/authz/subjects")
public class SubjectsAuthorizationResource {

  private final SubjectAclService subjectAclService;

  @Autowired
  public SubjectsAuthorizationResource(SubjectAclService subjectAclService) {
    this.subjectAclService = subjectAclService;
  }

  @GET
  public Iterable<SubjectAcls> get(@QueryParam("node") List<String> nodes) {
    if(nodes == null || nodes.size() == 0) throw new InvalidRequestException("At least one 'node' query param expected.");

    Map<String, SubjectAcls.Builder> aclMap = new HashMap<String, SubjectAcls.Builder>();

    for(String node : nodes) {
      for(Acl acl : Iterables.transform(subjectAclService.getNodePermissions("magma", node), PermissionsToAclFunction.INSTANCE)) {
        SubjectAcls.Builder acls;
        if(aclMap.containsKey(acl.getPrincipal())) {
          acls = aclMap.get(acl.getPrincipal());
        } else {
          acls = SubjectAcls.newBuilder().setPrincipal(acl.getPrincipal());
          aclMap.put(acl.getPrincipal(), acls);
        }
        acls.addAcls(acl);
      }
    }

    List<SubjectAcls.Builder> builders = Lists.newLinkedList(aclMap.values());
    Collections.sort(builders, new Comparator<SubjectAcls.Builder>() {

      @Override
      public int compare(SubjectAcls.Builder b1, SubjectAcls.Builder b2) {
        return b1.getPrincipal().compareTo(b2.getPrincipal());
      }

    });

    return Iterables.transform(builders, SubjectAclsBuilderFunction.INSTANCE);
  }

  private static final class SubjectAclsBuilderFunction implements Function<SubjectAcls.Builder, SubjectAcls> {

    private static final SubjectAclsBuilderFunction INSTANCE = new SubjectAclsBuilderFunction();

    @Override
    public SubjectAcls apply(SubjectAcls.Builder from) {
      return from.build();
    }
  }

  private static class PermissionsToAclFunction implements Function<Permissions, Opal.Acl> {

    private static final PermissionsToAclFunction INSTANCE = new PermissionsToAclFunction();

    @Override
    public Acl apply(Permissions from) {
      return Acl.newBuilder().setPrincipal(from.getSubject()).setResource(from.getNode()).addAllActions(from.getPermissions()).build();
    }

  }

}
