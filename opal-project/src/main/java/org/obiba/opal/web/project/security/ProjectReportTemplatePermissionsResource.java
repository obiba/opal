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

import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import static org.obiba.opal.web.project.security.ProjectPermissionsResource.DOMAIN;
import static org.obiba.opal.web.project.security.ProjectPermissionsResource.MagmaPermissionsPredicate;

@Component
@Scope("request")
@Path("/project/{name}/permissions/report-template/{template}")
public class ProjectReportTemplatePermissionsResource {

  // ugly: duplicate of ReportTemplatePermissionConverter.Permission

  public enum ReportTemplatePermission {
    REPORT_TEMPLATE_READ,
    REPORT_TEMPLATE_ALL
  }

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalConfigurationService configService;

  @PathParam("name")
  private String name;

  @PathParam("template")
  private String template;

  /**
   * Get all table-level permissions of a table in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  public Iterable<Opal.Acl> getTablePermissions(@QueryParam("type") SubjectAclService.SubjectType type) {

    // make sure project exists
    validateTemplate();

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService.getNodePermissions(DOMAIN, getNode(), type);

    return Iterables
        .transform(Iterables.filter(permissions, new MagmaPermissionsPredicate()), PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Set a table-level permission for a subject in the project.
   *
   * @param type
   * @param principal
   * @param permission
   * @return
   */
  @POST
  public Response setTablePermission(@QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal, @QueryParam("permission") ReportTemplatePermission permission) {

    // make sure project exists
    validateTemplate();

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(), subject);
    subjectAclService.addSubjectPermission(DOMAIN, getNode(), subject, permission.name());

    return Response.ok().build();
  }

  /**
   * Remove any table-level permission of a subject in the project.
   *
   * @param type
   * @param principal
   * @return
   */
  @DELETE
  public Response deleteTablePermissions(@QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal) {

    // make sure project exists
    validateTemplate();
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(), subject);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getNode(), subject);

    return Response.ok().build();
  }

  private void validateTemplate() {
    ReportTemplate rt = configService.getOpalConfiguration().getReportTemplate(template);
    if(rt == null || !rt.hasProject() || !name.equals(rt.getProject())) throw new NoSuchElementException();
  }

  private void validatePrincipal(String principal) {
    if(Strings.isNullOrEmpty(principal)) throw new InvalidRequestException("Principal is required.");
  }

  private String getNode() {
    return "/project/" + name + "/report-template/" + template;
  }
}
