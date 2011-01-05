/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.shell.reporting;

import java.io.File;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.commands.options.ReportCommandOptions;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.magma.ClientErrorDtos;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Scope("request")
@Path("/report-template/{name}")
public class ReportTemplateResource {

  private static final Logger log = LoggerFactory.getLogger(ReportTemplateResource.class);

  private static final String REPORT_SCHEDULING_GROUP = "reports";

  @PathParam("name")
  private String name;

  private final OpalRuntime opalRuntime;

  private final ReportService reportService;

  private final CommandSchedulerService commandSchedulerService;

  private final CommandRegistry commandRegistry;

  // Added for unit tests
  public ReportTemplateResource(String name, OpalRuntime opalRuntime, ReportService reportService) {
    this(name, opalRuntime, reportService, null, null);
  }

  public ReportTemplateResource(String name, OpalRuntime opalRuntime, ReportService reportService, CommandSchedulerService commandSchedulerService, CommandRegistry commandRegistry) {
    super();
    this.name = name;
    this.opalRuntime = opalRuntime;
    this.reportService = reportService;
    this.commandSchedulerService = commandSchedulerService;
    this.commandRegistry = commandRegistry;
  }

  @Autowired
  public ReportTemplateResource(OpalRuntime opalRuntime, ReportService reportService, CommandSchedulerService commandSchedulerService, @Qualifier("web") CommandRegistry commandRegistry) {
    super();
    this.opalRuntime = opalRuntime;
    this.reportService = reportService;
    this.commandSchedulerService = commandSchedulerService;
    this.commandRegistry = commandRegistry;
  }

  @GET
  public Response getReportTemplate() {
    ReportTemplate reportTemplate = opalRuntime.getOpalConfiguration().getReportTemplate(name);
    if(reportTemplate == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      return Response.ok(Dtos.asDto(reportTemplate)).build();
    }
  }

  @DELETE
  public Response deleteReportTemplate() {
    ReportTemplate reportTemplateToRemove = opalRuntime.getOpalConfiguration().getReportTemplate(name);
    if(reportTemplateToRemove == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      opalRuntime.getOpalConfiguration().removeReportTemplate(name);
      commandSchedulerService.deleteCommand(name, REPORT_SCHEDULING_GROUP);
      return Response.ok().build();
    }
  }

  @PUT
  public Response updateReportTemplate(@Context UriInfo uriInfo, ReportTemplateDto reportTemplateDto) {
    boolean isNew = !reportTemplateExists();

    try {
      Assert.isTrue(reportTemplateDto.getName().equals(name), "The report template name in the URI does not match the name given in the request body DTO.");

      updateOpalConfiguration(reportTemplateDto);
      updateSchedule(reportTemplateDto, isNew);
    } catch(Exception couldNotUpdateTheReportTemplate) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ClientErrorDtos.getErrorMessage(Status.BAD_REQUEST, "CouldNotUpdateTheReportTemplate").build()).build();
    }

    return isNew ? Response.created(uriInfo.getAbsolutePath()).build() : Response.ok().build();
  }

  private boolean reportTemplateExists() {
    return opalRuntime.getOpalConfiguration().hasReportTemplate(name);
  }

  private void updateOpalConfiguration(ReportTemplateDto reportTemplateDto) {
    OpalConfiguration opalConfig = opalRuntime.getOpalConfiguration();

    ReportTemplate reportTemplate = opalConfig.getReportTemplate(name);
    if(reportTemplate != null) {
      opalConfig.removeReportTemplate(reportTemplateDto.getName());
    }

    reportTemplate = Dtos.fromDto(reportTemplateDto);
    opalConfig.addReportTemplate(reportTemplate);
    opalRuntime.writeOpalConfiguration();
  }

  private void updateSchedule(ReportTemplateDto reportTemplateDto, boolean addToScheduler) {
    if(addToScheduler) {
      addCommand();
    }

    commandSchedulerService.unscheduleCommand(name, REPORT_SCHEDULING_GROUP);
    if(reportTemplateDto.hasCron()) {
      commandSchedulerService.scheduleCommand(name, REPORT_SCHEDULING_GROUP, reportTemplateDto.getCron());
    }
  }

  private void addCommand() {
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

  // TODO: to be REMOVED, only for testing
  @GET
  @Path("/report")
  @NotAuthenticated
  public Response getReport() {

    try {
      File design = opalRuntime.getFileSystem().getLocalFile(resolveFileInFileSystem("/report-templates/" + name + ".rptdesign"));
      if(!design.exists()) {
        return Response.status(Status.NOT_FOUND).build();
      }
      File reports = opalRuntime.getFileSystem().getLocalFile(resolveFileInFileSystem("/reports/" + name));
      if(!reports.exists()) {
        if(!reports.mkdirs()) {
          return Response.serverError().build();
        }
      }
      File report = new File(reports, name + "-" + System.currentTimeMillis() + ".pdf");
      reportService.render("PDF", null, design.getAbsolutePath(), report.getAbsolutePath());
      if(report.exists()) {
        return Response.ok().build();
      } else {
        return Response.serverError().build();
      }
    } catch(Exception e) {
      e.printStackTrace();
      return Response.serverError().build();
    }
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

}
