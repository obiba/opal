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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import static org.obiba.opal.web.project.permissions.ProjectPermissionsResource.DOMAIN;
import static org.obiba.opal.web.project.permissions.ProjectPermissionsResource.MagmaPermissionsPredicate;

@Component
@Scope("request")
@Path("/project/{name}/permissions/subject/{principal}")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Permissions", description = "Operations on permissions")
public class ProjectSubjectPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private ProjectService projectService;

  @PathParam("name")
  private String name;

  @PathParam("principal")
  private String principal;

  //
  // Permissions by Subject
  //

  /**
   * Get all permissions of a subject in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Operation(summary = "Get subject project permissions", description = "Retrieves all permissions for a specific subject within a project. This includes project-level, datasource-level, table-level, and variable-level permissions.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject permissions successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or subject not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getSubjectPermissions(
      @QueryParam("type") @DefaultValue("USER") SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.concat(
        subjectAclService.getSubjectNodeHierarchyPermissions(DOMAIN, getProjectNode(), type.subjectFor(principal)),
        Iterables.filter(subjectAclService
            .getSubjectNodeHierarchyPermissions(DOMAIN, getDatasourceNode(), type.subjectFor(principal)),
            new MagmaPermissionsPredicate()));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Delete all permissions of a subject in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @DELETE
  @Operation(summary = "Delete subject project permissions", description = "Removes all permissions for a specific subject within a project. This includes project-level, datasource-level, table-level, and variable-level permissions.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Subject permissions successfully deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project or subject not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteSubjectPermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    SubjectAcl.Subject subject = type.subjectFor(principal);
    for(SubjectAclService.Permissions permissions : Iterables
        .concat(subjectAclService.getSubjectNodeHierarchyPermissions(DOMAIN, getProjectNode(), subject),
            subjectAclService.getSubjectNodeHierarchyPermissions(DOMAIN, getDatasourceNode(), subject))) {
      subjectAclService.deleteSubjectPermissions(DOMAIN, permissions.getNode(), subject);
    }

    return Response.ok().build();
  }

  private String getProjectNode() {
    return "/project/" + name;
  }

  private String getDatasourceNode() {
    return "/datasource/" + name;
  }

}
