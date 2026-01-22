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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/authz/{resource:.*}")
@Tag(name = "Authorization", description = "Operations related to user authorization")
public class AuthorizationResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("resource")
  private String resource;

@GET
@Operation(summary = "Get ACLs for resource", description = "Retrieve access control list (ACL) entries for a specific resource with optional filtering by domain and subject type")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "ACL entries successfully retrieved"),
  @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Iterable<Opal.Acl> get(@QueryParam("domain") @DefaultValue("opal") String domain,
    @QueryParam("type") SubjectType type) {
    return Iterables
        .transform(subjectAclService.getNodePermissions(domain, getNode(), type), PermissionsToAclFunction.INSTANCE);
  }

@POST
@Operation(summary = "Add permission to ACL", description = "Add a new permission entry to the access control list for a specific subject on a resource")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Permission successfully added to ACL"),
  @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Opal.Acl add(@QueryParam("domain") @DefaultValue("opal") String domain, @QueryParam("subject") String subject,
    @QueryParam("type") SubjectType type, @QueryParam("perm") String permission) {
    subjectAclService.addSubjectPermission(domain, getNode(), type.subjectFor(subject), permission);
    return PermissionsToAclFunction.INSTANCE
        .apply(subjectAclService.getSubjectNodePermissions(domain, getNode(), type.subjectFor(subject)));
  }

@DELETE
@Operation(summary = "Delete permission from ACL", description = "Remove a specific permission or all permissions for a subject from the access control list. If permission parameter is not provided, all permissions for the subject will be removed.")
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Permission successfully deleted from ACL"),
  @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
  @ApiResponse(responseCode = "404", description = "Subject or permission not found"),
  @ApiResponse(responseCode = "500", description = "Internal server error")
})
public Opal.Acl delete(@QueryParam("domain") @DefaultValue("opal") String domain,
    @QueryParam("subject") String subject, @QueryParam("type") SubjectType type, @QueryParam("perm") String permission) {
    if(Strings.isNullOrEmpty(permission)) {
      subjectAclService.deleteSubjectPermissions(domain, getNode(), type.subjectFor(subject));
    } else {
      subjectAclService.deleteSubjectPermissions(domain, getNode(), type.subjectFor(subject), permission);
    }
    return PermissionsToAclFunction.INSTANCE
        .apply(subjectAclService.getSubjectNodePermissions(domain, getNode(), type.subjectFor(subject)));
  }

  private String getNode() {
    return '/' + resource;
  }
}
