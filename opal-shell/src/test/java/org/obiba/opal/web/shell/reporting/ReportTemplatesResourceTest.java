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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

import com.google.common.collect.Maps;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

public class ReportTemplatesResourceTest {

  private OpalConfigurationService opalConfigurationServiceMock;

  private CommandRegistry commandRegistry;

  private CommandSchedulerService commandSchedulerServiceMock;

  private Set<ReportTemplate> reportTemplates;

  @Before
  public void setUp() {
    MagmaEngine.get().shutdown();
    new MagmaEngine();
    opalConfigurationServiceMock = createMock(OpalConfigurationService.class);
    OpalConfiguration opalConfiguration = new OpalConfiguration();
    commandRegistry = createMock(CommandRegistry.class);

    commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template1", "reports");
    commandSchedulerServiceMock.scheduleCommand("template1", "reports", "schedule");

    reportTemplates = new LinkedHashSet<>();
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
  public void testGetReportTemplates_RetrieveSetOfTemplates() {
    Subject mockSubject = createMock(Subject.class);
    ThreadContext.bind(mockSubject);
    configureMock(mockSubject);
    replay(opalConfigurationServiceMock, mockSubject);

    ReportTemplatesResource reportTemplateResource = new ReportTemplatesResource();
    reportTemplateResource.setConfigService(opalConfigurationServiceMock);
    reportTemplateResource.setCommandSchedulerService(commandSchedulerServiceMock);
    reportTemplateResource.setCommandRegistry(commandRegistry);

    Set<ReportTemplateDto> reportTemplatesDtos = reportTemplateResource.getReportTemplates();
    ThreadContext.unbindSubject();

    assertThat(reportTemplates).hasSize(4);
    assertThat(reportTemplatesDtos).hasSize(4);

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) reportTemplatesDtos.toArray()[0];
    assertThat(reportTemplateDto.getName()).isEqualTo("template1");
    assertThat(reportTemplateDto.getDesign()).isEqualTo("design");
    assertThat(reportTemplateDto.getFormat()).isEqualTo("format");
    assertThat(reportTemplateDto.getCron()).isEqualTo("schedule");
    assertThat(reportTemplateDto.getParametersList()).hasSize(2);

    verify(opalConfigurationServiceMock, mockSubject);
  }

  private void configureMock(Subject mockSubject) {
    expect(mockSubject.getPrincipal()).andReturn(createMock(Principal.class)).anyTimes();
    expect(mockSubject.isPermitted("rest:/report-template/template1:GET")).andReturn(true).anyTimes();
    expect(mockSubject.isPermitted("rest:/report-template/template2:GET")).andReturn(true).anyTimes();
    expect(mockSubject.isPermitted("rest:/report-template/template3:GET")).andReturn(true).anyTimes();
    expect(mockSubject.isPermitted("rest:/report-template/template4:GET")).andReturn(true).anyTimes();
  }

  @Test
  public void testUpdateReportTemplate_NewReportTemplateCreated() {

    opalConfigurationServiceMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expectLastCall().once();

    commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template9", "reports");
    commandSchedulerServiceMock.scheduleCommand("template9", "reports", "schedule");

    @SuppressWarnings("unchecked")
    Command<Object> commandMock = createMock(Command.class);
    commandSchedulerServiceMock.addCommand("template9", "reports", commandMock);

    expect(commandRegistry.newCommand("report")).andReturn(commandMock);

    replay(opalConfigurationServiceMock, commandSchedulerServiceMock, commandRegistry);

    ReportTemplatesResource reportTemplatesResource = new ReportTemplatesResource();
    reportTemplatesResource.setConfigService(opalConfigurationServiceMock);
    reportTemplatesResource.setCommandSchedulerService(commandSchedulerServiceMock);
    reportTemplatesResource.setCommandRegistry(commandRegistry);

    Response response = reportTemplatesResource.createReportTemplate(Dtos.asDto(getReportTemplate("template9")));

    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(response.getMetadata().get("location").get(0).toString()).isEqualTo("/report-template/template9");

    verify(opalConfigurationServiceMock, commandSchedulerServiceMock, commandRegistry);
  }

  private ReportTemplate getReportTemplate(String name) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setSchedule("schedule");
    Map<String, String> params = Maps.newHashMap();
    params.put("param1", "value1");
    params.put("param2", "value2");
    reportTemplate.setParameters(params);
    return reportTemplate;
  }

}
