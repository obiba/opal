/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.resource;

import com.google.common.collect.Iterables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.security.ProjectResourceReferencePermissionConverter;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.project.permissions.ProjectPermissionsResource;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Component
@Scope("request")
@Path("/project/{name}/permissions/resource/{resource}")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Permissions", description = "Operations on permissions")
public class ProjectResourceReferencePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @PathParam("resource")
  private String resource;

  @Autowired
  private ProjectService projectService;

  /**
   * Get all resource-level permissions in the project.
   *
   * @param principals
   * @param permission
   * @param type
   * @return
   */
  @GET
  @Operation(summary = "Get project resource permissions", description = "Retrieves permissions for a specific resource within a project. Resource permissions control access to individual project resources like files, reports, or other assets.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resource permissions successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or resource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getPermissions(@QueryParam("type") SubjectAcl.SubjectType type,
                                           @QueryParam("principal") List<String> principals,
                                           @QueryParam("permission") ProjectResourceReferencePermissionConverter.Permission permission) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodePermissions(ProjectPermissionsResource.DOMAIN, getNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a resource-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  @Operation(summary = "Set project resource permission", description = "Grants permissions to subjects for a specific resource within a project. Resource permissions control access to individual project resources like files, reports, or other assets.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resource permission successfully set"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or resource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setPermission(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
                                @QueryParam("principal") List<String> principals,
                                @QueryParam("permission") ProjectResourceReferencePermissionConverter.Permission permission) {

    // make sure project exists
    projectService.getProject(name);

    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove a resources-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  @Operation(summary = "Delete project resource permissions", description = "Removes all permissions from subjects for a specific resource within a project. This revokes access to individual project resources like files, reports, or other assets.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Resource permissions successfully deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or resource not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePermission(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
                                   @QueryParam("principal") List<String> principals) {

    // make sure project exists
    projectService.getProject(name);

    deletePermissions(principals, type);
    return Response.ok().build();
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }

  @Override
  protected String getNode() {
    return "/project/" + name + "/resource/" + resource;
  }
}
