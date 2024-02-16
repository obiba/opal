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
