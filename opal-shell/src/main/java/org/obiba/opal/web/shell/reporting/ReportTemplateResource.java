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
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.reporting.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
@Scope("request")
@Path("/report-template/{name}")
public class ReportTemplateResource extends AbstractReportTemplateResource {

  private static final Logger log = LoggerFactory.getLogger(ReportTemplateResource.class);

  @PathParam("name")
  private String name;

  private final OpalConfigurationService configService;

  private final OpalRuntime opalRuntime;

  private final CommandSchedulerService commandSchedulerService;

  // Added for unit tests
  ReportTemplateResource(String name, OpalRuntime opalRuntime, OpalConfigurationService configService) {
    this(name, opalRuntime, configService, null);
  }

  public ReportTemplateResource(String name, OpalRuntime opalRuntime, OpalConfigurationService configService, CommandSchedulerService commandSchedulerService) {
    super();
    this.name = name;
    this.configService = configService;
    this.opalRuntime = opalRuntime;
    this.commandSchedulerService = commandSchedulerService;

  }

  @Autowired
  public ReportTemplateResource(OpalRuntime opalRuntime, ReportService reportService, OpalConfigurationService configService, CommandSchedulerService commandSchedulerService) {
    super();
    this.opalRuntime = opalRuntime;
    this.configService = configService;
    this.commandSchedulerService = commandSchedulerService;
  }

  @GET
  public Response getReportTemplate() {
    ReportTemplate reportTemplate = getOpalConfigurationService().getOpalConfiguration().getReportTemplate(name);
    if(reportTemplate == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      return Response.ok(Dtos.asDto(reportTemplate)).build();
    }
  }

  @DELETE
  public Response deleteReportTemplate() {
    ReportTemplate reportTemplateToRemove = getOpalConfigurationService().getOpalConfiguration().getReportTemplate(name);
    if(reportTemplateToRemove == null) {
      return Response.status(Status.NOT_FOUND).build();
    } else {
      getOpalConfigurationService().modifyConfiguration(new ConfigModificationTask() {

        @Override
        public void doWithConfig(OpalConfiguration config) {
          config.removeReportTemplate(name);
          commandSchedulerService.deleteCommand(name, REPORT_SCHEDULING_GROUP);
        }
      });
      return Response.ok().build();
    }
  }

  @PUT
  public Response updateReportTemplate(@Context UriInfo uriInfo, ReportTemplateDto reportTemplateDto) {
    if(!reportTemplateExists()) return Response.status(Status.NOT_FOUND).build();

    try {
      Assert.isTrue(reportTemplateDto.getName().equals(name), "The report template name in the URI does not match the name given in the request body DTO.");

      updateOpalConfiguration(reportTemplateDto);
      updateSchedule(reportTemplateDto);
    } catch(Exception e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(ClientErrorDto.newBuilder().setCode(Status.BAD_REQUEST.getStatusCode()).setStatus("CouldNotUpdateTheReportTemplate").build()).build();
    }

    return Response.ok().build();
  }

  private boolean reportTemplateExists() {
    return getOpalConfigurationService().getOpalConfiguration().hasReportTemplate(name);
  }

  protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
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
