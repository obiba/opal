/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.shell.reporting;

import java.io.File;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.reporting.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

@Component
@Transactional
@Scope("request")
@Path("/project/{project}/report-template/{name}")
public class ProjectReportTemplateResource extends AbstractReportTemplateResource {

  @PathParam("project")
  private String project;

  @PathParam("name")
  protected String name;

  @Nullable
  private ReportTemplate reportTemplate;

  private OpalRuntime opalRuntime;

  @Autowired
  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  @GET
  public Response get() {
    return authzReadReportTemplate(getReportTemplate())
        ? Response.ok(Dtos.asDto(getReportTemplate())).build()
        : Response.status(Response.Status.FORBIDDEN).build();
  }

  @NotNull
  private ReportTemplate getReportTemplate() {
    if(reportTemplate == null) {
      reportTemplate = reportTemplateService.getReportTemplate(name, project);
    }
    return reportTemplate;
  }

  @DELETE
  public Response delete() {
    ReportTemplate reportTemplateToRemove = getReportTemplate();
    if(!authzReadReportTemplate(reportTemplateToRemove)) {
      return Response.status(Response.Status.FORBIDDEN).build();
    }
    reportTemplateService.delete(reportTemplateToRemove.getName(), reportTemplateToRemove.getProject());
    commandSchedulerService.deleteCommand(name, REPORT_SCHEDULING_GROUP);
    return Response.ok().build();
  }

  @PUT
  public Response update(Opal.ReportTemplateDto dto) {
    getReportTemplate();

    Preconditions.checkArgument(dto.getName().equals(name),
        "The report template name in the URI does not match the name given in the request body DTO.");

    save(dto);
    updateSchedule(dto);

    return Response.ok().build();
  }

  @GET
  @Path("/reports")
  public List<Opal.ReportDto> getReports() throws FileSystemException {
    getReportTemplate();
    FileObject reportFolder = getReportFolder();
    List<Opal.ReportDto> reports = Lists.newArrayList();
    if(reportFolder.exists()) {
      for(FileObject reportFile : reportFolder.getChildren()) {
        if(reportFile.getType() == FileType.FILE && reportFile.getName().getBaseName().startsWith(name + "-") &&
            reportFile.isReadable()) {
          reports.add(getReportDto(reportFile));
        }
      }
    }
    return reports;
  }

  @GET
  @Path("/reports/latest")
  public Response getReport() throws FileSystemException {
    getReportTemplate();

    FileObject reportFolder = getReportFolder();
    if(!reportFolder.exists()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    FileObject lastReportFile = null;
    File lastReport = null;
    for(FileObject reportFile : reportFolder.getChildren()) {
      if(reportFile.getType() == FileType.FILE && reportFile.getName().getBaseName().startsWith(name + "-") &&
          reportFile.isReadable()) {
        File report = opalRuntime.getFileSystem().getLocalFile(reportFile);
        if(lastReport == null || report.lastModified() > lastReport.lastModified()) {
          lastReport = report;
          lastReportFile = reportFile;
        }
      }
    }

    return lastReportFile == null
        ? Response.status(Response.Status.NOT_FOUND).build()
        : Response.ok(getReportDto(lastReportFile)).build();
  }

  private Opal.ReportDto getReportDto(FileObject reportFile) throws FileSystemException {
    String publicLink = "/report/public/" + opalRuntime.getFileSystem().getObfuscatedPath(reportFile) + "?project=" +
        getReportTemplate().getProject();

    return Opal.ReportDto.newBuilder()//
        .setName(reportFile.getName().getBaseName())//
        .setLink("/files" + reportFile.getName().getPath())//
        .setSize(reportFile.getContent().getSize())//
        .setLastModifiedTime(reportFile.getContent().getLastModifiedTime())//
        .setPublicLink(publicLink).build();
  }

  private FileObject getReportFolder() throws FileSystemException {
    OpalFileSystem fileSystem = opalRuntime.getFileSystem();
    return fileSystem.getRoot().resolveFile("/reports/" + getReportTemplate().getProject() + "/" + name);
  }

  @Override
  protected boolean authzReadReportTemplate(ReportTemplate template) {
    return template.getProject().equals(project) &&
        getAuthorizer().isPermitted("rest:/project/" + project + "/report-template/" + template.getName() + ":GET");
  }

}
