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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import junit.framework.Assert;

import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;

public class ReportTemplateResourceTest {

  public static final String BASE_URI = "http://localhost:8888/ws";

  private OpalRuntime opalRuntimeMock;

  private ReportService reportServiceMock;

  private OpalConfiguration opalConfiguration;

  Set<ReportTemplate> reportTemplates;

  @Before
  public void setUp() {
    opalRuntimeMock = createMock(OpalRuntime.class);
    reportServiceMock = createMock(ReportService.class);
    opalConfiguration = new OpalConfiguration();

    reportTemplates = new LinkedHashSet<ReportTemplate>();
    reportTemplates.add(getReportTemplate("template1"));
    reportTemplates.add(getReportTemplate("template2"));
    reportTemplates.add(getReportTemplate("template3"));
    reportTemplates.add(getReportTemplate("template4"));
    opalConfiguration.setReportTemplates(reportTemplates);

    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).anyTimes();
  }

  @Test
  public void testGetReportTemplate_ReportTemplateFoundAndReturned() {

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template3", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplateResource.getReportTemplate();
    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) response.getEntity();
    Assert.assertEquals("template3", reportTemplateDto.getName());

    verify(opalRuntimeMock);

  }

  @Test
  public void testGetReportTemplate_ReportTemplateNotFound() {

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template9", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplateResource.getReportTemplate();
    Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateDeleted() {

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.deleteCommand("template2", "reports");

    replay(opalRuntimeMock, commandSchedulerServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template2", opalRuntimeMock, reportServiceMock, commandSchedulerServiceMock);
    Response response = reportTemplateResource.deleteReportTemplate();

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Assert.assertTrue(opalConfiguration.getReportTemplate("template2") == null);

    verify(opalRuntimeMock, commandSchedulerServiceMock);

  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateNotFound() {

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template9", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplateResource.deleteReportTemplate();
    Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  @Test
  public void testUpdateReportTemplate_NewReportTemplateCreated() {
    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getAbsolutePath()).andReturn(UriBuilderImpl.fromUri(BASE_URI).build("")).atLeastOnce();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.scheduleCommand("template9", "reports", "schedule");

    replay(opalRuntimeMock, uriInfoMock, commandSchedulerServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template9", opalRuntimeMock, reportServiceMock, commandSchedulerServiceMock);
    Response response = reportTemplateResource.updateReportTemplate(uriInfoMock, Dtos.asDto(getReportTemplate("template9")));

    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    Assert.assertEquals(BASE_URI, response.getMetadata().get("location").get(0).toString());

    verify(opalRuntimeMock, uriInfoMock, commandSchedulerServiceMock);
  }

  @Test
  public void testUpdateReportTemplate_ExistingReportTemplateUpdated() {

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.scheduleCommand("template1", "reports", "schedule");

    replay(opalRuntimeMock, commandSchedulerServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template1", opalRuntimeMock, reportServiceMock, commandSchedulerServiceMock);
    Response response = reportTemplateResource.updateReportTemplate(createMock(UriInfo.class), Dtos.asDto(getReportTemplate("template1")));

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, commandSchedulerServiceMock);

  }

  @Test
  public void testUpdateReportTemplate_ErrorEncountered() {
    UriInfo uriInfoMock = createMock(UriInfo.class);

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource("template1", opalRuntimeMock, reportServiceMock);
    Response response = reportTemplateResource.updateReportTemplate(uriInfoMock, Dtos.asDto(getReportTemplate("template2")));

    Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    Assert.assertEquals("CouldNotUpdateTheReportTemplate", ((ClientErrorDto) response.getEntity()).getStatus());

    verify(opalRuntimeMock);
  }

  private ReportTemplate getReportTemplate(String name) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setSchedule("schedule");
    reportTemplate.setParameters(new HashMap<String, String>());
    return reportTemplate;
  }
}
