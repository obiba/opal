package org.obiba.opal.web.shell.reporting;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.security.SubjectAclService;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.AclAction;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.reporting.Dtos;
import org.obiba.opal.web.ws.cfg.OpalWsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class ReportTemplatesResource extends AbstractReportTemplateResource {

  private OpalConfigurationService configService;

  private CommandSchedulerService commandSchedulerService;

  private CommandRegistry commandRegistry;

  @Autowired
  @Qualifier("web")
  public void setCommandRegistry(CommandRegistry commandRegistry) {
    this.commandRegistry = commandRegistry;
  }

  @Autowired
  public void setCommandSchedulerService(CommandSchedulerService commandSchedulerService) {
    this.commandSchedulerService = commandSchedulerService;
  }

  @Autowired
  public void setConfigService(OpalConfigurationService configService) {
    this.configService = configService;
  }

  @GET
  public Set<ReportTemplateDto> getReportTemplates() {
    Set<ReportTemplate> templates = configService.getOpalConfiguration().getReportTemplates();
    ImmutableSet.Builder<ReportTemplate> builder = ImmutableSet.builder();
    builder.addAll(Iterables.filter(templates, new Predicate<ReportTemplate>() {

      @Override
      public boolean apply(ReportTemplate template) {
        return authzReadReportTemplate(template);
      }
    }));
    return Dtos.asDto(builder.build());
  }

  @POST
  public Response createReportTemplate(ReportTemplateDto reportTemplateDto) {
    if(reportTemplateDto == null || reportTemplateDto.getName().isEmpty() ||
        reportTemplateExists(reportTemplateDto.getName())) {
      return Response.status(Response.Status.BAD_REQUEST).entity(
          ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode())
              .setStatus("ReportTemplateAlreadyExists").build()).build();
    }

    save(reportTemplateDto);
    addCommand(reportTemplateDto.getName());
    updateSchedule(reportTemplateDto);

    URI reportUri = getReportTemplateURI(reportTemplateDto);
    return Response.created(reportUri) //
        .header("X-Alt-Permissions", new ReportPermissions(reportUri, AclAction.REPORT_TEMPLATE_ALL)) //
        .build();
  }

  protected URI getReportTemplateURI(ReportTemplateDto reportTemplateDto) {
    return UriBuilder.fromResource(ReportTemplateResource.class).build(reportTemplateDto.getName());
  }

  private void addCommand(final String name) {
    ReportCommandOptions reportOptions = new ReportCommandOptions() {

      @Override
      public boolean isHelp() {
        return false;
      }

      @Override
      public String getName() {
        return name;
      }
    };
    Command<ReportCommandOptions> reportCommand = commandRegistry.newCommand("report");
    reportCommand.setOptions(reportOptions);
    commandSchedulerService.addCommand(name, REPORT_SCHEDULING_GROUP, reportCommand);
  }

  @Override
  protected OpalConfigurationService getOpalConfigurationService() {
    return configService;
  }

  @Override
  protected CommandSchedulerService getCommandSchedulerService() {
    return commandSchedulerService;
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
