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

import jakarta.annotation.Nullable;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.security.ProjectPermissionConverter;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;

@Component
@Scope("request")
@Path("/project/{name}/permissions")
public class ProjectPermissionsResource extends AbstractPermissionsResource {

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private ProjectService projectService;

  @PathParam("name")
  private String name;

  /**
   * Get all permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/_all")
  public Iterable<Opal.Acl> getPermissions(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .concat(subjectAclService.getNodeHierarchyPermissions(domain, getNode(), type), Iterables
            .filter(subjectAclService.getNodeHierarchyPermissions(domain, "/datasource/" + name, type),
                new MagmaPermissionsPredicate()));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  @GET
  @Path("/subjects")
  public Iterable<Opal.Subject> getSubjects(@QueryParam("type") SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .concat(subjectAclService.getNodeHierarchyPermissions(DOMAIN, getNode(), type), Iterables
            .filter(subjectAclService.getNodeHierarchyPermissions(DOMAIN, "/datasource/" + name, type),
                new MagmaPermissionsPredicate()));

    List<SubjectAcl.Subject> subjects = Lists.newArrayList();
    for(SubjectAclService.Permissions perms : permissions) {
      SubjectAcl.Subject subject = perms.getSubject();
      if(!subjects.contains(subject)) subjects.add(subject);
    }

    return Iterables.transform(subjects, new Function<SubjectAcl.Subject, Opal.Subject>() {
      @Nullable
      @Override
      public Opal.Subject apply(@Nullable SubjectAcl.Subject input) {
        assert input != null;
        return Opal.Subject.newBuilder().setPrincipal(input.getPrincipal())
            .setType(Opal.Subject.SubjectType.valueOf(input.getType().name())).build();
      }
    });
  }

  /**
   * Get all project-level permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/project")
  public Iterable<Opal.Acl> getProjectPermissions(@QueryParam("type") SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a project-level permission for a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @SuppressWarnings("TypeMayBeWeakened")
  @POST
  @Path("/project")
  public Response addProjectPermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals,
      @QueryParam("permission") ProjectPermissionConverter.Permission permission) {
    // make sure project exists
    projectService.getProject(name);
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove project-level permissions from a subject.
   *
   * @param principals
   * @param type
   * @return
   */
  @SuppressWarnings("TypeMayBeWeakened")
  @DELETE
  @Path("/project")
  public Response deleteProjectPermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals) {
    // make sure project exists
    projectService.getProject(name);
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  @Override
  protected String getNode() {
    return "/project/" + name;
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }

  /**
   * Filter the accessible Magma objects.
   */
  static class MagmaPermissionsPredicate implements Predicate<SubjectAclService.Permissions> {
    @Override
    public boolean apply(@Nullable SubjectAclService.Permissions input) {
      try {
        assert input != null;
        String fullName = input.getNode().replace("/datasource/", "").replace("/table/", ".").replace("/view/", ".")
            .replace("/variable/", ":");
        if(input.getNode().contains("/variable/")) MagmaEngineVariableResolver.valueOf(fullName).resolveSource();
        else if(input.getNode().contains("/table/") || input.getNode().contains("/view/"))
          MagmaEngineTableResolver.valueOf(fullName).resolveTable();
        return true;
      } catch(Exception e) {
        return false;
      }
    }
  }
}
