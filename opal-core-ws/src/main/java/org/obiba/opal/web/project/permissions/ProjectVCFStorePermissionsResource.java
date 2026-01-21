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

import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.security.VCFStorePermissionsConverter;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

@Component
@Scope("request")
@Path("/project/{name}/permissions/vcf-store")
@Tag(name = "Projects", description = "Operations on projects")
@Tag(name = "Permissions", description = "Operations on permissions")
public class ProjectVCFStorePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @PathParam("name")
  private String name;

  @Autowired
  private ProjectService projectService;

  /**
   * Get all datasource-level permissions in the project.
   *
   * @param principals
   * @param permission
   * @param type
   * @return
   */
  @GET
  @Operation(summary = "Get VCF store permissions", description = "Retrieves VCF store permissions for subjects. VCF store permissions control access to VCF (Variant Call Format) genomic data storage and operations within the project.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "VCF store permissions successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getVCFStorePermissions(@QueryParam("type") SubjectAcl.SubjectType type,
      @QueryParam("principal") List<String> principals,
      @QueryParam("permission") VCFStorePermissionsConverter.Permission permission) {

    // make sure project exists
    Project project = projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodePermissions(ProjectPermissionsResource.DOMAIN, getNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a VCF-Store-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  @Operation(summary = "Set VCF store permission", description = "Grants VCF store permissions to subjects. VCF store permissions control access to VCF (Variant Call Format) genomic data storage and operations within the project.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "VCF store permission successfully set"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setVCFStorePermission(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
      @QueryParam("principal") List<String> principals,
      @QueryParam("permission") VCFStorePermissionsConverter.Permission permission) {

    // make sure project exists
    projectService.getProject(name);

    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove a VCF-Store-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  @Operation(summary = "Delete VCF store permissions", description = "Removes all VCF store permissions from subjects. This revokes access to VCF (Variant Call Format) genomic data storage and operations within the project.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "VCF store permissions successfully deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Project not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteVCFStorePermission(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
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
    return "/project/" + name + "/vcf-store";
  }
}
