/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.permissions;

import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.AbstractPermissionsResource;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;

import static org.obiba.opal.core.domain.security.SubjectAcl.SubjectType;
import static org.obiba.opal.web.project.permissions.ProjectPermissionsResource.MagmaPermissionsPredicate;

@Component
@Scope("request")
@Path("/project/{name}/permissions/report-template/{template}")
public class ProjectReportTemplatePermissionsResource extends AbstractPermissionsResource {

  // ugly: duplicate of ReportTemplatePermissionConverter.Permission

  public enum ReportTemplatePermission {
    REPORT_TEMPLATE_READ,
    REPORT_TEMPLATE_ALL
  }

  @Autowired
  private SubjectAclService subjectAclService;

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
  public Iterable<Opal.Acl> getTablePermissions(@QueryParam("type") SubjectType type) {

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
   * @param principals
   * @param permission
   * @return
   */
  @POST
  public Response setTablePermission(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals, @QueryParam("permission") ReportTemplatePermission permission) {
    // make sure template exists
    validateTemplate();
    setPermission(principals, type, permission.name());
    return Response.ok().build();
  }

  /**
   * Remove any table-level permission of a subject in the project.
   *
   * @param type
   * @param principals
   * @return
   */
  @DELETE
  public Response deleteTablePermissions(@QueryParam("type") @DefaultValue("USER") SubjectType type,
      @QueryParam("principal") List<String> principals) {

    // make sure template exists
    validateTemplate();
    deletePermissions(principals, type);
    return Response.ok().build();
  }

  private void validateTemplate() {
    ReportTemplate rt = configService.getOpalConfiguration().getReportTemplate(template);
    if(rt == null || !rt.hasProject() || !name.equals(rt.getProject())) throw new NoSuchElementException();
  }

  @Override
  protected String getNode() {
    return "/project/" + name + "/report-template/" + template;
  }

  @Override
  protected SubjectAclService getSubjectAclService() {
    return subjectAclService;
  }
}
