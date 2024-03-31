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

import com.google.common.collect.Iterables;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.security.TablePermissionConverter;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import static org.obiba.opal.web.project.permissions.ProjectPermissionsResource.MagmaPermissionsPredicate;

@Component
@Scope("request")
@Path("/project/{name}/permissions/table/{table}")
public class ProjectTablePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @PathParam("table")
  private String table;

  private ValueTable valueTable;

  /**
   * Get all table-level permissions of a table in the project.
   *
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getTablePermissions(@QueryParam("type") SubjectType type) {

    // make sure datasource and table exists
    getValueTable();

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);

    return Iterables
        .transform(Iterables.filter(permissions, new MagmaPermissionsPredicate()), PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a table-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  public Response setTablePermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
                                     @QueryParam("principal") List<String> principals, @QueryParam("permission") TablePermissionConverter.Permission permission) {

    // make sure datasource and table exists
    getValueTable();
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any table-level permission of a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  public Response deleteTablePermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
                                         @QueryParam("principal") List<String> principals) {

    // make sure datasource and table exists
    getValueTable();
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  //
  // Variables
  //

  /**
   * Get all variable-level permissions of a table in the project.
   *
   * @param type
   * @return
   */
  @GET
  @Path("/variables")
  public Iterable<Opal.Acl> getTableVariablesPermissions(@QueryParam("type") SubjectType type) {

    // make sure datasource and table exists
    getValueTable();

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .filter(subjectAclService.getNodeHierarchyPermissions(DOMAIN, getNode() + "/variable", type),
            new MagmaPermissionsPredicate());

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  private ValueTable getValueTable() {
    if (valueTable == null) {
      valueTable = MagmaEngine.get().getDatasource(name).getValueTable(table);
    }
    return valueTable;
  }

  @Override
  protected String getNode() {
    return "/datasource/" + name + (getValueTable().isView() ? "/view/" : "/table/") + table;
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
