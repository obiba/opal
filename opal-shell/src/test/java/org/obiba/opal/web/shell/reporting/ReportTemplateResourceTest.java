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

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.model.Ws.ClientErrorDto;
import org.obiba.opal.web.reporting.Dtos;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class ReportTemplateResourceTest {

  public static final String BASE_URI = "http://localhost:8888/ws";

  private OpalRuntime opalRuntimeMock;

  private OpalConfigurationService opalConfigurationServiceMock;

  Set<ReportTemplate> reportTemplates;

  @Before
  public void setUp() {
    if(!MagmaEngine.isInstanciated()) new MagmaEngine();
    opalRuntimeMock = createMock(OpalRuntime.class);
    opalConfigurationServiceMock = createMock(OpalConfigurationService.class);
    OpalConfiguration opalConfiguration = new OpalConfiguration();

    reportTemplates = new LinkedHashSet<ReportTemplate>();
    reportTemplates.add(getReportTemplate("template1"));
    reportTemplates.add(getReportTemplate("template2"));
    reportTemplates.add(getReportTemplate("template3"));
    reportTemplates.add(getReportTemplate("template4"));
    opalConfiguration.setReportTemplates(reportTemplates);

    expect(opalConfigurationServiceMock.getOpalConfiguration()).andReturn(opalConfiguration).anyTimes();
  }

  @After
  public void stopYourEngine() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testGetReportTemplate_ReportTemplateFoundAndReturned() {
    Subject mockSubject = createMock(Subject.class);
    ThreadContext.bind(mockSubject);
    expect(mockSubject.getPrincipal()).andReturn(createMock(Principal.class)).anyTimes();
    expect(mockSubject.isPermitted("magma:/report-template/template3:GET")).andReturn(true).anyTimes();

    replay(opalRuntimeMock, opalConfigurationServiceMock, mockSubject);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template3");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);

    Response response = reportTemplateResource.get();
    ThreadContext.unbindSubject();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) response.getEntity();
    assertEquals("template3", reportTemplateDto.getName());

    verify(opalRuntimeMock, opalConfigurationServiceMock, mockSubject);

  }

  @Test
  public void testGetReportTemplate_ReportTemplateNotFound() {

    replay(opalRuntimeMock, opalConfigurationServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template9");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);

    Response response = reportTemplateResource.get();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, opalConfigurationServiceMock);
  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateDeleted() {
    Subject mockSubject = createMock(Subject.class);
    ThreadContext.bind(mockSubject);
    expect(mockSubject.getPrincipal()).andReturn(createMock(Principal.class)).anyTimes();
    expect(mockSubject.isPermitted("magma:/report-template/template2:GET")).andReturn(true).anyTimes();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.deleteCommand("template2", "reports");

    opalConfigurationServiceMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expectLastCall().once();

    replay(opalRuntimeMock, opalConfigurationServiceMock, commandSchedulerServiceMock, mockSubject);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template2");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);
    reportTemplateResource.setCommandSchedulerService(commandSchedulerServiceMock);

    Response response = reportTemplateResource.deleteReportTemplate();
    ThreadContext.unbindSubject();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    // Assert.assertTrue(opalConfiguration.getReportTemplate("template2") == null);

    verify(opalRuntimeMock, opalConfigurationServiceMock, commandSchedulerServiceMock, mockSubject);

  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateNotFound() {

    replay(opalRuntimeMock, opalConfigurationServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template9");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);

    Response response = reportTemplateResource.deleteReportTemplate();
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, opalConfigurationServiceMock);
  }

  @Ignore
  @Test
  public void testUpdateReportTemplate_NewReportTemplateCreated() {
    UriInfo uriInfoMock = createMock(UriInfo.class);
    expect(uriInfoMock.getAbsolutePath()).andReturn(UriBuilder.fromUri(BASE_URI).build("")).atLeastOnce();

    opalConfigurationServiceMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expectLastCall().once();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template9", "reports");
    commandSchedulerServiceMock.scheduleCommand("template9", "reports", "schedule");

    @SuppressWarnings("unchecked")
    Command<Object> commandMock = createMock(Command.class);
    commandSchedulerServiceMock.addCommand("template9", "reports", commandMock);

    replay(opalRuntimeMock, opalConfigurationServiceMock, uriInfoMock, commandSchedulerServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template9");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);
    reportTemplateResource.setCommandSchedulerService(commandSchedulerServiceMock);

    Response response = reportTemplateResource.updateReportTemplate(Dtos.asDto(getReportTemplate("template9")));

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    assertEquals(BASE_URI, response.getMetadata().get("location").get(0).toString());

    verify(opalRuntimeMock, opalConfigurationServiceMock, uriInfoMock, commandSchedulerServiceMock);
  }

  @Test
  public void testUpdateReportTemplate_ExistingReportTemplateUpdated() {
    opalConfigurationServiceMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expectLastCall().once();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template1", "reports");
    commandSchedulerServiceMock.scheduleCommand("template1", "reports", "schedule");

    replay(opalRuntimeMock, opalConfigurationServiceMock, commandSchedulerServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template1");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);
    reportTemplateResource.setCommandSchedulerService(commandSchedulerServiceMock);

    Response response = reportTemplateResource.updateReportTemplate(Dtos.asDto(getReportTemplate("template1")));

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, opalConfigurationServiceMock, commandSchedulerServiceMock);

  }

  @Test
  public void testUpdateReportTemplate_ErrorEncountered() {

    replay(opalRuntimeMock, opalConfigurationServiceMock);

    ReportTemplateResource reportTemplateResource = new ReportTemplateResource();
    reportTemplateResource.setName("template1");
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);

    Response response = reportTemplateResource.updateReportTemplate(Dtos.asDto(getReportTemplate("template2")));

    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    assertEquals("CouldNotUpdateTheReportTemplate", ((ClientErrorDto) response.getEntity()).getStatus());

    verify(opalRuntimeMock, opalConfigurationServiceMock);
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
