/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.security;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@Component
@Scope("request")
@Path("/project/{name}/permissions")
public class ProjectPermissionsResource {

  private final SubjectAclService subjectAclService;

  private final ProjectService projectService;

  @PathParam("name")
  private String name;

  @Autowired
  public ProjectPermissionsResource(SubjectAclService subjectAclService, ProjectService projectService) {
    this.subjectAclService = subjectAclService;
    this.projectService = projectService;
  }

  /**
   * Get all permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getPermissions(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .concat(subjectAclService.getNodeHierarchyPermissions(domain, "/project/" + name, type), Iterables
            .filter(subjectAclService.getNodeHierarchyPermissions(domain, "/datasource/" + name, type),
                new MagmaPermissionsPredicate()));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
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
  public Iterable<Opal.Acl> getProjectPermissions(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodeHierarchyPermissions(domain, "/project/" + name, type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all datasource-level permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/datasource")
  public Iterable<Opal.Acl> getDatasourcePermissions(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource exists
    MagmaEngine.get().getDatasource(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodePermissions(domain, "/datasource/" + name, type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all table-level permissions of a table in the project.
   *
   * @param table
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/table/{table}")
  public Iterable<Opal.Acl> getTablePermissions(@PathParam("table") String table,
      @QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource and table exists
    MagmaEngine.get().getDatasource(name).getValueTable(table);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.filter(Iterables
        .concat(subjectAclService.getNodePermissions(domain, "/datasource/" + name + "/table/" + table, type),
            subjectAclService.getNodePermissions(domain, "/datasource/" + name + "/view/" + table, type)),
        new MagmaPermissionsPredicate());

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all variable-level permissions of a table in the project.
   *
   * @param table
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/table/{table}/variables")
  public Iterable<Opal.Acl> getTableVariablesPermissions(@PathParam("table") String table,
      @QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure datasource and table exists
    MagmaEngine.get().getDatasource(name).getValueTable(table);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.filter(Iterables.concat(subjectAclService
        .getNodeHierarchyPermissions(domain, "/datasource/" + name + "/table/" + table + "/variable", type),
        subjectAclService
            .getNodeHierarchyPermissions(domain, "/datasource/" + name + "/view/" + table + "/variable", type)),
        new MagmaPermissionsPredicate());

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all permissions of a subject in the project.
   *
   * @param principal
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/subject/{principal}")
  public Iterable<Opal.Acl> getSubjectPermissions(@PathParam("principal") String principal,
      @QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables.concat(
        subjectAclService.getSubjectNodeHierarchyPermissions(domain, "/project/" + name, type.subjectFor(principal)),
        Iterables.filter(subjectAclService
            .getSubjectNodeHierarchyPermissions(domain, "/datasource/" + name, type.subjectFor(principal)),
            new MagmaPermissionsPredicate()));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Delete all permissions of a subject in the project.
   * @param principal
   * @param domain
   * @param type
   * @return
   */
  @DELETE
  @Path("/subject/{principal}")
  public Response deleteSubjectPermissions(@PathParam("principal") String principal,
      @QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    for(SubjectAclService.Permissions permissions : Iterables
        .concat(subjectAclService.getSubjectNodeHierarchyPermissions(domain, "/project/" + name, subject),
            subjectAclService.getSubjectNodeHierarchyPermissions(domain, "/datasource/" + name, subject))) {
      subjectAclService.deleteSubjectPermissions(domain, permissions.getNode(), subject);
    }

    return Response.ok().build();
  }

  /**
   * Filter the accessible Magma objects.
   */
  private static class MagmaPermissionsPredicate implements Predicate<SubjectAclService.Permissions> {
    @Override
    public boolean apply(@Nullable SubjectAclService.Permissions input) {
      try {
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
