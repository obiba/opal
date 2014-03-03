package org.obiba.opal.web.shell.reporting;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
@Transactional
@Scope("request")
@Path("/report-templates")
public class ReportTemplatesResource {

  private ReportTemplateScheduler reportTemplateScheduler;

  private ReportTemplateService reportTemplateService;

  @Autowired
  public void setReportTemplateScheduler(ReportTemplateScheduler reportTemplateScheduler) {
    this.reportTemplateScheduler = reportTemplateScheduler;
  }

  @Autowired
  public void setReportTemplateService(ReportTemplateService reportTemplateService) {
    this.reportTemplateService = reportTemplateService;
  }

  @GET
  public Set<ReportTemplateDto> get() {
    ImmutableSet.Builder<ReportTemplate> setBuilder = ImmutableSet.builder();
    setBuilder.addAll(Iterables.filter(reportTemplateService.getReportTemplates(), new Predicate<ReportTemplate>() {

      @Override
      public boolean apply(ReportTemplate template) {
        return ReportTemplateAuthorizer.authzGet(template);
      }
    }));
    return Dtos.asDto(setBuilder.build());
  }

  @POST
  public Response create(ReportTemplateDto dto) {
    reportTemplateService.save(Dtos.fromDto(dto));
    reportTemplateScheduler.scheduleCommand(dto);
    reportTemplateScheduler.updateSchedule(dto);
    URI uri = UriBuilder.fromResource(ProjectReportTemplateResource.class).build(dto.getProject(), dto.getName());
    return Response.created(uri) //
        .header("X-Alt-Permissions", new ReportPermissions(uri, AclAction.REPORT_TEMPLATE_ALL)) //
        .build();
  }

  private static final class ReportPermissions implements SubjectAclService.Permissions {

    private final URI reportUri;

    private final AclAction action;

    private ReportPermissions(URI reportUri, AclAction action) {
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
