/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.reporting;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.web.ws.security.NotAuthenticated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("request")
@Path("/report-templates/{name}")
public class ReportTemplateResource {

  private static final Logger log = LoggerFactory.getLogger(ReportTemplateResource.class);

  @PathParam("name")
  private String name;

  private final OpalRuntime opalRuntime;

  private final ReportService reportService;

  // Added for testing
  public ReportTemplateResource(String name, OpalRuntime opalRuntime, ReportService reportService) {
    super();
    this.name = name;
    this.opalRuntime = opalRuntime;
    this.reportService = reportService;
  }

  @Autowired
  public ReportTemplateResource(OpalRuntime opalRuntime, ReportService reportService) {
    super();
    this.opalRuntime = opalRuntime;
    this.reportService = reportService;
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
