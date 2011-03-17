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
import org.obiba.opal.web.model.Opal;
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
    if(nodes == null || nodes.size() == 0) return getSubjects();

    return getAclsGroupedBySubject(nodes);
  }

  public Iterable<Acls> getSubjects() {
    List<Acls> acls = Lists.newArrayList();
    for(SubjectAclService.Subject subject : subjectAclService.getSubjects("magma")) {
      acls.add(newAcls(subject).build());
    }
    Collections.sort(acls, AclsComparator.INSTANCE);
    return acls;
  }

  private Iterable<Acls> getAclsGroupedBySubject(List<String> nodes) {
    Map<Opal.Subject, Acls.Builder> aclMap = new HashMap<Opal.Subject, Acls.Builder>();

    for(String node : nodes) {
      for(Acl acl : Iterables.transform(subjectAclService.getNodePermissions("magma", node), PermissionsToAclFunction.INSTANCE)) {
        Acls.Builder acls;
        if(aclMap.containsKey(acl.getSubject())) {
          acls = aclMap.get(acl.getSubject());
        } else {
          acls = newAcls(acl.getSubject());
          aclMap.put(acl.getSubject(), acls);
        }
        acls.addAcls(acl);
      }
    }

    List<Acls> acls = Lists.newLinkedList(Iterables.transform(aclMap.values(), AclsBuilderFunction.INSTANCE));
    Collections.sort(acls, AclsComparator.INSTANCE);
    return acls;
  }

  private Acls.Builder newAcls(Opal.Subject subject) {
    return Acls.newBuilder().setSubject(subject);
  }

  private Acls.Builder newAcls(SubjectAclService.Subject subject) {
    return Acls.newBuilder().setSubject(PermissionsToAclFunction.valueOf(subject));
  }

  private static final class AclsComparator implements Comparator<Acls> {

    static final AclsComparator INSTANCE = new AclsComparator();

    @Override
    public int compare(Acls o1, Acls o2) {
      if(o1.getSubject().getType() == o2.getSubject().getType()) {
        return o1.getSubject().getPrincipal().compareTo(o2.getSubject().getPrincipal());
      }
      return o1.getSubject().getType().compareTo(o2.getSubject().getType());
    }
  }

  private static final class AclsBuilderFunction implements Function<Acls.Builder, Acls> {

    private static final AclsBuilderFunction INSTANCE = new AclsBuilderFunction();

    @Override
    public Acls apply(Acls.Builder from) {
      return from.build();
    }
  }

}
