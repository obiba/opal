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
import jakarta.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.security.DatasourcePermissionConverter;
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
@Path("/project/{name}/permissions/datasource")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Permissions", description = "Operations on permissions")
public class ProjectDatasourcePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  //
  // Datasource
  //

  /**
   * Get all datasource-level permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Operation(summary = "Get datasource permissions", description = "Retrieves datasource-level permissions for subjects. Datasource permissions control access to data operations, table management, and variable metadata within the project datasource.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Datasource permissions successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or datasource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getDatasourcePermissions(@QueryParam("type") SubjectType type) {
    // make sure datasource exists
    MagmaEngine.get().getDatasource(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodePermissions(ProjectPermissionsResource.DOMAIN, getNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a datasource-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  @Operation(summary = "Set datasource permission", description = "Grants datasource-level permissions to subjects. Datasource permissions control access to data operations, table management, and variable metadata within the project datasource.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Datasource permission successfully set"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or datasource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setDatasourcePermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals,
      @QueryParam("permission") DatasourcePermissionConverter.Permission permission) {
    // make sure datasource exists
    MagmaEngine.get().getDatasource(name);
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any datasource-level permission of a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  @Operation(summary = "Delete datasource permissions", description = "Removes all datasource-level permissions from subjects. This revokes access to data operations, table management, and variable metadata within the project datasource.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Datasource permissions successfully deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or datasource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteDatasourcePermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals) {
    // make sure datasource exists
    MagmaEngine.get().getDatasource(name);
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  @Override
  protected String getNode() {
    return "/datasource/" + name;
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
