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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ReportTemplateResource.class);

  @PathParam("name")
  protected String name;

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
  public Response getReportTemplate() {
    ReportTemplate reportTemplate = getOpalConfigurationService().getOpalConfiguration().getReportTemplate(name);
    return reportTemplate == null || !authzReadReportTemplate(reportTemplate) ? Response.status(Status.NOT_FOUND)
        .build() : Response.ok(Dtos.asDto(reportTemplate)).build();
  }

  @DELETE
  public Response deleteReportTemplate() {
    ReportTemplate reportTemplateToRemove = getOpalConfigurationService().getOpalConfiguration()
        .getReportTemplate(name);
    if(reportTemplateToRemove == null || !authzReadReportTemplate(reportTemplateToRemove)) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      getOpalConfigurationService().modifyConfiguration(new ConfigModificationTask() {

        @Override
        public void doWithConfig(OpalConfiguration config) {
          config.removeReportTemplate(name);
        }
      });
      commandSchedulerService.deleteCommand(name, REPORT_SCHEDULING_GROUP);
      return Response.ok().build();
    }
  }

  @PUT
  public Response updateReportTemplate(@Context UriInfo uriInfo, ReportTemplateDto reportTemplateDto) {
    if(!reportTemplateExists()) return Response.status(Status.NOT_FOUND).build();

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
    return ReportDto.newBuilder()//
        .setName(reportFile.getName().getBaseName())//
        .setLink("/files" + reportFile.getName().getPath())//
        .setSize(reportFile.getContent().getSize())//
        .setLastModifiedTime(reportFile.getContent().getLastModifiedTime())//
        .setPublicLink("/report/public/" + opalRuntime.getFileSystem().getObfuscatedPath(reportFile)).build();
  }

  private FileObject getReportFolder() throws FileSystemException {
    OpalFileSystem fileSystem = opalRuntime.getFileSystem();
    return fileSystem.getRoot().resolveFile("/reports/" + name);
  }

  private boolean reportTemplateExists() {
    return getOpalConfigurationService().getOpalConfiguration().hasReportTemplate(name);
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
