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

import com.google.common.collect.Iterables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.security.DataShieldPermissionConverter;
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

@Component
@Scope("request")
@Path("/system/permissions/datashield")
@Tag(name = "Permissions", description = "Operations on permissions")
public class DataShieldPermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  /**
   * Get DataShield permissions.
   *
   * @param type
   * @return
   */
  @GET
  @Operation(
    summary = "Get DataShield system permissions",
    description = "Retrieves access control list (ACL) permissions for DataShield system, optionally filtered by subject type (USER or GROUP)."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getDataShieldPermissions(@QueryParam("type") SubjectType type) {
    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);
    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set DataShield permission for a subject.
   *
   * @param type
   * @param principals
   * @param permission
   * @return
   */
  @POST
  @Operation(
    summary = "Set DataShield system permission",
    description = "Sets access permissions for users or groups on the DataShield system."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully set permission"),
    @ApiResponse(responseCode = "400", description = "Invalid permission data"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setDataShieldPermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
                                          @QueryParam("principal") List<String> principals,
                                          @QueryParam("permission") DataShieldPermissionConverter.Permission permission) {
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any DataShield permissions of a subject.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  @Operation(
    summary = "Delete DataShield system permissions",
    description = "Removes access permissions for users or groups from the DataShield system."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully deleted permissions"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteDataShieldPermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
                                              @QueryParam("principal") List<String> principals) {
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  @Override
  protected String getNode() {
    return "/datashield";
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
