/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.permissions;

import java.util.List;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.security.VariablePermissionConverter;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/project/{name}/permissions/table/{table}/variable/{variable}")
public class ProjectVariablePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @PathParam("table")
  private String table;

  @PathParam("variable")
  private String variable;

  private ValueTable valueTable;

  /**
   * Get variable-level permissions of a variable in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getTableVariablesPermissions(@QueryParam("type") SubjectType type) {

    // make sure datasource, table and variable exists
    MagmaEngine.get().getDatasource(name).getValueTable(table).getVariable(variable);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);

    return Iterables
        .transform(Iterables.filter(permissions, new ProjectPermissionsResource.MagmaPermissionsPredicate()),
            PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a variable-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  public Response setTableVariablePermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals, @QueryParam("permission") VariablePermissionConverter.Permission permission) {

    // make sure datasource, table and variable exists
    getValueTable().getVariable(variable);
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any variable-level permission of a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  public Response deleteTableVariablePermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals) {
    // make sure datasource, table and variable exists
    getValueTable().getVariable(variable);
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  private ValueTable getValueTable() {
    if(valueTable == null) {
      valueTable = MagmaEngine.get().getDatasource(name).getValueTable(table);
    }
    return valueTable;
  }

  @Override
  protected String getNode() {
    return "/datasource/" + name + (getValueTable().isView() ? "/view/" : "/table/") + table + "/variable/" + variable;
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
