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
import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportDto;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.reporting.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

@Component
@Transactional
@Scope("request")
@Path("/report-template/{name}")
public class ReportTemplateResource extends AbstractReportTemplateResource {

  @PathParam("name")
  protected String name;

  private ReportTemplate reportTemplate;

  private OpalConfigurationService configService;

  private CommandSchedulerService commandSchedulerService;

  private OpalRuntime opalRuntime;

  @Autowired
  public void setCommandSchedulerService(CommandSchedulerService commandSchedulerService) {
    this.commandSchedulerService = commandSchedulerService;
  }

  @Autowired
  public void setConfigService(OpalConfigurationService configService) {
    this.configService = configService;
  }

  @Autowired
  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  protected void setName(String name) {
    this.name = name;
  }

  @GET
  public Response get() {
    loadReportTemplate();
    return authzReadReportTemplate(reportTemplate)
        ? Response.ok(Dtos.asDto(reportTemplate)).build()
        : Response.status(Status.NOT_FOUND).build();
  }

  private ReportTemplate loadReportTemplate() {
    if (reportTemplate == null) {
      reportTemplate = getOpalConfigurationService().getOpalConfiguration().getReportTemplate(name);
      if (reportTemplate == null) {
        throw new NoSuchElementException();
      }
    }
    return reportTemplate;
  }

  @DELETE
  public Response deleteReportTemplate() {
    ReportTemplate reportTemplateToRemove = loadReportTemplate();
    if(!authzReadReportTemplate(reportTemplateToRemove)) {
      return Response.status(Status.NOT_FOUND).build();
    }
    getOpalConfigurationService().modifyConfiguration(new ConfigModificationTask() {

      @Override
      public void doWithConfig(OpalConfiguration config) {
        config.removeReportTemplate(name);
      }
    });
    commandSchedulerService.deleteCommand(name, REPORT_SCHEDULING_GROUP);
    return Response.ok().build();
  }

  @PUT
  public Response updateReportTemplate(ReportTemplateDto reportTemplateDto) {
    loadReportTemplate();

    try {
      Assert.isTrue(reportTemplateDto.getName().equals(name),
          "The report template name in the URI does not match the name given in the request body DTO.");

      updateOpalConfiguration(reportTemplateDto);
      updateSchedule(reportTemplateDto);
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(
          ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode())
              .setStatus("CouldNotUpdateTheReportTemplate").build()).build();
    }

    return Response.ok().build();
  }

  @GET
  @Path("/reports")
  public List<ReportDto> getReports() throws FileSystemException {
    loadReportTemplate();

    FileObject reportFolder = getReportFolder();
    List<ReportDto> reports = Lists.newArrayList();

    if(reportFolder.exists()) {
      for(FileObject reportFile : reportFolder.getChildren()) {
        if(reportFile.getType() == FileType.FILE && reportFile.getName().getBaseName().startsWith(name + "-")) {
          reports.add(getReportDto(reportFile));
        }
      }
    }

    return reports;
  }

  @GET
  @Path("/reports/latest")
  public Response getReport() throws FileSystemException {
    loadReportTemplate();

    FileObject reportFolder = getReportFolder();
    if(!reportFolder.exists()) {
      return Response.status(Status.NOT_FOUND).build();
    }

    FileObject lastReportFile = null;
    File lastReport = null;
    for(FileObject reportFile : reportFolder.getChildren()) {
      if(reportFile.getType() == FileType.FILE && reportFile.getName().getBaseName().startsWith(name + "-")) {
        File report = opalRuntime.getFileSystem().getLocalFile(reportFile);
        if(lastReport == null || report.lastModified() > lastReport.lastModified()) {
          lastReport = report;
          lastReportFile = reportFile;
        }
      }
    }

    return lastReportFile == null
        ? Response.status(Status.NOT_FOUND).build()
        : Response.ok(getReportDto(lastReportFile)).build();
  }

  private ReportDto getReportDto(FileObject reportFile) throws FileSystemException {
    String publicLink = "/report/public/" + opalRuntime.getFileSystem().getObfuscatedPath(reportFile);
    if (reportTemplate.hasProject()) {
      publicLink += "?project=" + reportTemplate.getProject();
    }
    return ReportDto.newBuilder()//
        .setName(reportFile.getName().getBaseName())//
        .setLink("/files" + reportFile.getName().getPath())//
        .setSize(reportFile.getContent().getSize())//
        .setLastModifiedTime(reportFile.getContent().getLastModifiedTime())//
        .setPublicLink(publicLink).build();
  }

  private FileObject getReportFolder() throws FileSystemException {
    String folder = "/reports/" + name;
    if (reportTemplate.hasProject()) {
      folder = "/projects/" + reportTemplate.getProject() + folder;
    }
    OpalFileSystem fileSystem = opalRuntime.getFileSystem();
    return fileSystem.getRoot().resolveFile(folder);
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
