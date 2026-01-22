/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.permissions;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.opal.core.security.AdministrationPermissionConverter;
import org.obiba.opal.core.security.RPermissionConverter;
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
@Path("/system/permissions/administration")
@Tag(name = "Permissions", description = "Operations on permissions")
public class AdministrationPermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  /**
   * Get administration permissions.
   *
   * @param type
   * @return
   */
  @GET
  @Operation(summary = "Get administration permissions", description = "Retrieves administration permissions for subjects. Administration permissions control access to system administration functions and Opal server management.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Administration permissions successfully retrieved"),
      @ApiResponse(responseCode = "400", description = "Invalid subject type provided"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getAdministrationPermissions(@QueryParam("type") SubjectType type) {
    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);
    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set administration permission for a subject.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  @Operation(summary = "Set administration permission", description = "Grants administration permissions to subjects. Administration permissions control access to system administration functions and Opal server management.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Administration permission successfully set"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Subject not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setAdministrationPermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals,
      @QueryParam("permission") AdministrationPermissionConverter.Permission permission) {
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any administration permissions of a subject.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  @Operation(summary = "Delete administration permissions", description = "Removes all administration permissions from subjects. This revokes access to system administration functions and Opal server management.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Administration permissions successfully deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
      @ApiResponse(responseCode = "404", description = "Subject not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteAdministrationPermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals) {
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  @Override
  protected String getNode() {
    return "/";
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
