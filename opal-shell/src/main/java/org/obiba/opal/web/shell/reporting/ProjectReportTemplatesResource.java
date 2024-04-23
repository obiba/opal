/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell.reporting;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.reporting.Dtos;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
@Scope("request")
@Path("/project/{name}/report-templates")
public class ProjectReportTemplatesResource {

  @PathParam("name")
  private String name;

  private ReportTemplateService reportTemplateService;

  private ReportTemplateScheduler reportTemplateScheduler;

  @Autowired
  public void setReportTemplateService(ReportTemplateService reportTemplateService) {
    this.reportTemplateService = reportTemplateService;
  }

  @Autowired
  public void setReportTemplateScheduler(ReportTemplateScheduler reportTemplateScheduler) {
    this.reportTemplateScheduler = reportTemplateScheduler;
  }

  @GET
  public Set<Opal.ReportTemplateDto> get() {
    ImmutableSet.Builder<ReportTemplate> setBuilder = ImmutableSet.builder();
    setBuilder.addAll(Iterables.filter(reportTemplateService.getReportTemplates(name), new Predicate<ReportTemplate>() {
      @Override
      public boolean apply(ReportTemplate template) {
        return ReportTemplateAuthorizer.authzGet(template);
      }
    }));
    return Dtos.asDto(setBuilder.build());
  }

  @POST
  public Response create(Opal.ReportTemplateDto dto) {
    reportTemplateService.save(Dtos.fromDto(dto));
    reportTemplateScheduler.scheduleCommand(dto);
    reportTemplateScheduler.updateSchedule(dto);
    URI uri = UriBuilder.fromResource(ProjectReportTemplateResource.class).build(dto.getProject(), dto.getName());
    return Response.created(uri) //
        .header("X-Alt-Permissions", new ReportPermissions(uri, Opal.AclAction.REPORT_TEMPLATE_ALL)) //
        .build();
  }

  private static final class ReportPermissions implements SubjectAclService.Permissions {

    private final URI reportUri;

    private final Opal.AclAction action;

    private ReportPermissions(URI reportUri, Opal.AclAction action) {
      this.reportUri = reportUri;
      this.action = action;
    }

    @Override
    public String getDomain() {
      return "opal";
    }

    @Override
    public String getNode() {
      return reportUri.getPath().replaceFirst(OpalWsConfig.WS_ROOT, "");
    }

    @Override
    public SubjectAcl.Subject getSubject() {
      return null;
    }

    @Override
    public Iterable<String> getPermissions() {
      return Lists.newArrayList(action.toString());
    }
  }
}
