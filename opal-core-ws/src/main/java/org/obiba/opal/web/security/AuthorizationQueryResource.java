/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.security;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.Opal.Acl;
import org.obiba.opal.web.model.Opal.Acls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/authz/query")
public class AuthorizationQueryResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @GET
  public Iterable<Acls> get(@QueryParam("domain") String domain, @QueryParam("type") SubjectType type,
      @QueryParam("node") List<String> nodes) {
    if(nodes == null || nodes.isEmpty()) return getSubjects(domain, type);

    return getAclsGroupedBySubject(domain, type, nodes);
  }

  Iterable<Acls> getSubjects(String domain, SubjectType type) {
    List<Acls> acls = Lists.newArrayList();
    for(SubjectAcl.Subject subject : subjectAclService.getSubjects(domain, type)) {
      acls.add(newAcls(subject).build());
    }
    Collections.sort(acls, AclsComparator.INSTANCE);
    return acls;
  }

  private Iterable<Acls> getAclsGroupedBySubject(String domain, SubjectType type, Iterable<String> nodes) {
    Map<Opal.Subject, Acls.Builder> aclMap = new HashMap<>();

    for(String node : nodes) {
      for(Acl acl : Iterables
          .transform(subjectAclService.getNodePermissions(domain, node, type), PermissionsToAclFunction.INSTANCE)) {
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

  private Acls.Builder newAcls(SubjectAcl.Subject subject) {
    return Acls.newBuilder().setSubject(PermissionsToAclFunction.valueOf(subject));
  }

  private static final class AclsComparator implements Comparator<Acls> {

    static final AclsComparator INSTANCE = new AclsComparator();

    @Override
    public int compare(Acls o1, Acls o2) {
      return ComparisonChain.start() //
          .compare(o1.getSubject().getType(), o2.getSubject().getType()) //
          .compare(o1.getSubject().getPrincipal(), o2.getSubject().getPrincipal()) //
          .result();
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
