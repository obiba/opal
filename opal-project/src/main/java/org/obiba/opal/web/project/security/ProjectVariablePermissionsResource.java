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

@Component
@Scope("request")
@Path("/project/{name}/permissions/table/{table}/variable/{variable}")
public class ProjectVariablePermissionsResource {

  // ugly: duplicate of ProjectsPermissionConverter.Permission

  public enum VariablePermission {
    VARIABLE_READ
  }

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @PathParam("table")
  private String table;

  @PathParam("variable")
  private String variable;

  /**
   * Get variable-level permissions of a variable in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getTableVariablesPermissions(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource, table and variable exists
    MagmaEngine.get().getDatasource(name).getValueTable(table).getVariable(variable);

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .concat(subjectAclService.getNodePermissions(DOMAIN, getNode(false), type),
            subjectAclService.getNodeHierarchyPermissions(DOMAIN, getNode(true), type));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a variable-level permission for a subject in the project.
   *
   * @param type
   * @param principal
   * @param permission
   * @return
   */
  @POST
  public Response setTableVariablePermission(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal, @QueryParam("permission") VariablePermission permission) {

    // make sure datasource, table and variable exists
    ValueTable vt = MagmaEngine.get().getDatasource(name).getValueTable(table);
    vt.getVariable(variable);
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(true), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(false), subject);
    subjectAclService.addSubjectPermission(DOMAIN, getNode(vt.isView()), subject, permission.name());

    return Response.ok().build();
  }

  /**
   * Remove any variable-level permission of a subject in the project.
   *
   * @param type
   * @param principal
   * @return
   */
  @DELETE
  public Response setTableVariablePermission(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal) {

    // make sure datasource, table and variable exists
    MagmaEngine.get().getDatasource(name).getValueTable(table).getVariable(variable);
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(true), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(false), subject);

    return Response.ok().build();
  }

  private void validatePrincipal(String principal) {
    if(Strings.isNullOrEmpty(principal)) throw new InvalidRequestException("Principal is required.");
  }

  private String getNode(boolean isView) {
    return "/datasource/" + name + (isView ? "/view/" : "/table/") + table + "/variable/" + variable;
  }

}
