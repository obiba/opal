/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.datashield;

import com.google.common.collect.Iterables;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.security.DataShieldProfilePermissionConverter;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.datashield.cfg.DataShieldProfileService;
import org.obiba.opal.web.model.Opal;
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
@Path("/datashield/profile/{name}/permissions")
@Tag(name = "DataSHIELD", description = "Operations on DataSHIELD")
@Tag(name = "Permissions", description = "Operations on permissions")
public class DataShieldProfilePermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private DataShieldProfileService datashieldProfileService;

  @PathParam("name")
  private String name;

  @GET
  @Operation(
    summary = "Get DataSHIELD profile permissions",
    description = "Retrieves the access control list (ACL) permissions for a DataSHIELD profile, optionally filtered by subject type (USER or GROUP)."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully retrieved permissions"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Iterable<Opal.Acl> getPermissions(@QueryParam("type") SubjectAcl.SubjectType type) {
    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);
    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  @POST
  @Operation(
    summary = "Set DataSHIELD profile permission",
    description = "Sets access permissions for users or groups on a DataSHIELD profile. Automatically enables restricted access mode on the profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully set permission"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "400", description = "Invalid permission data"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response setPermission(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
                                @QueryParam("principal") List<String> principals, @QueryParam("permission") DataShieldProfilePermissionConverter.Permission permission) {

    // make sure profile exists
    checkProfileExists();
    setPermission(principals, type, permission.name());
    DataShieldProfile profile = datashieldProfileService.findProfile(name);
    profile.setRestrictedAccess(true);
    datashieldProfileService.saveProfile(profile);
    return Response.ok().build();
  }

  @DELETE
  @Operation(
    summary = "Delete DataSHIELD profile permissions",
    description = "Removes access permissions for users or groups from a DataSHIELD profile."
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Successfully deleted permissions"),
    @ApiResponse(responseCode = "404", description = "Profile not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePermissions(@QueryParam("type") @DefaultValue("USER") SubjectAcl.SubjectType type,
                                         @QueryParam("principal") List<String> principals) {

    // make sure profile exists
    checkProfileExists();
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  void checkProfileExists() {
    if (!datashieldProfileService.hasProfile(name))
      throw new NotFoundException("DataSHIELD profile does not exist: " + name);
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }

  @Override
  protected String getNode() {
    return "/datashield/profile/" + name;
  }
}
