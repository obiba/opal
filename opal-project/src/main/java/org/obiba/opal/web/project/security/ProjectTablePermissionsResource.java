/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.security;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import static org.obiba.opal.web.project.security.ProjectPermissionsResource.DOMAIN;
import static org.obiba.opal.web.project.security.ProjectPermissionsResource.MagmaPermissionsPredicate;

@Component
@Scope("request")
@Path("/project/{name}/permissions/table/{table}")
public class ProjectTablePermissionsResource {

  // ugly: duplicate of ProjectsPermissionConverter.Permission

  public enum TablePermission {
    TABLE_READ,
    TABLE_VALUES,
    TABLE_EDIT,
    TABLE_VALUES_EDIT,
    TABLE_ALL
  }

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @PathParam("table")
  private String table;

  /**
   * Get all table-level permissions of a table in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getTablePermissions(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource and table exists
    MagmaEngine.get().getDatasource(name).getValueTable(table);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.filter(Iterables
        .concat(subjectAclService.getNodePermissions(DOMAIN, getNode(false), type),
            subjectAclService.getNodePermissions(DOMAIN, getNode(true), type)),
        new MagmaPermissionsPredicate());

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a table-level permission for a subject in the project.
   *
   * @param type
   * @param principal
   * @param permission
   * @return
   */
  @POST
  public Response setTablePermission(@QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal, @QueryParam("permission") TablePermission permission) {

    // make sure datasource and table exists
    ValueTable vt = MagmaEngine.get().getDatasource(name).getValueTable(table);
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(true), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(false), subject);
    subjectAclService.addSubjectPermission(DOMAIN, getNode(vt.isView()), subject, permission.name());

    return Response.ok().build();
  }

  /**
   * Remove any table-level permission of a subject in the project.
   *
   * @param type
   * @param principal
   * @return
   */
  @DELETE
  public Response deleteTablePermissions(@QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal) {

    // make sure datasource and table exists
    MagmaEngine.get().getDatasource(name).getValueTable(table);
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(true), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(false), subject);

    return Response.ok().build();
  }

  //
  // Variables
  //

  /**
   * Get all variable-level permissions of a table in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/variables")
  public Iterable<Opal.Acl> getTableVariablesPermissions(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource and table exists
    MagmaEngine.get().getDatasource(name).getValueTable(table);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.filter(Iterables
        .concat(subjectAclService.getNodeHierarchyPermissions(DOMAIN, getNode(false) + "/variable", type),
            subjectAclService.getNodeHierarchyPermissions(DOMAIN, getNode(true) + "/variable", type)),
        new MagmaPermissionsPredicate());

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  private void validatePrincipal(String principal) {
    if(Strings.isNullOrEmpty(principal)) throw new InvalidRequestException("Principal is required.");
  }

  private String getNode(boolean isView) {
    return "/datasource/" + name + (isView ? "/view/" : "/table/") + table;
  }
}
