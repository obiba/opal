package org.obiba.opal.web.shell.reporting;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.reporting.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/report-templates")
public class ReportTemplatesResource extends AbstractReportTemplateResource {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ReportTemplatesResource.class);

  private final OpalConfigurationService configService;

  private final CommandSchedulerService commandSchedulerService;

  private final CommandRegistry commandRegistry;

  @Autowired
  public ReportTemplatesResource(OpalConfigurationService configService, CommandSchedulerService commandSchedulerService, @Qualifier("web") CommandRegistry commandRegistry) {
    super();
    this.configService = configService;
    this.commandSchedulerService = commandSchedulerService;
    this.commandRegistry = commandRegistry;
  }

  @GET
  public Set<ReportTemplateDto> getReportTemplates() {
    Set<ReportTemplate> templates = configService.getOpalConfiguration().getReportTemplates();
    return Dtos.asDto(templates);
  }

  @POST
  public Response createReportTemplate(ReportTemplateDto reportTemplateDto) {
    if(reportTemplateDto == null || reportTemplateDto.getName().length() == 0 || reportTemplateExists(reportTemplateDto.getName())) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    try {
      updateOpalConfiguration(reportTemplateDto);
      addCommand(reportTemplateDto.getName());
      updateSchedule(reportTemplateDto);
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode()).setStatus("CouldNotCreateReportTemplate").build()).build();
    }

    return Response.created(UriBuilder.fromResource(ReportTemplateResource.class).build(reportTemplateDto.getName())).build();
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
    commandSchedulerService.addCommand(name, "reports", reportCommand);
  }

  @Override
  protected OpalConfigurationService getOpalConfigurationService() {
    return configService;
  }

  @Override
  protected CommandSchedulerService getCommandSchedulerService() {
    return commandSchedulerService;
  }

}
