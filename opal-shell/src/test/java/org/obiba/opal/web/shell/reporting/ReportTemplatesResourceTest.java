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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

import com.google.common.collect.Maps;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReportTemplatesResourceTest {

  private ReportTemplateService reportTemplateService;

  private CommandRegistry commandRegistry;

  private CommandSchedulerService commandSchedulerService;

  private ReportTemplateScheduler reportTemplateScheduler;

  @Before
  public void before() {
    MagmaEngine.get().shutdown();
    new MagmaEngine();

    reportTemplateService = mock(ReportTemplateService.class);
    commandRegistry = mock(CommandRegistry.class);
    reportTemplateScheduler = mock(ReportTemplateScheduler.class);

    commandSchedulerService = mock(CommandSchedulerService.class);
    commandSchedulerService.unscheduleCommand("template1", "reports");
    commandSchedulerService.scheduleCommand("template1", "reports", "schedule");

    Collection<ReportTemplate> reportTemplates = new LinkedHashSet<>();
    reportTemplates.add(createReportTemplate("template1"));
    reportTemplates.add(createReportTemplate("template2"));
    reportTemplates.add(createReportTemplate("template3"));
    reportTemplates.add(createReportTemplate("template4"));

    when(reportTemplateService.getReportTemplates()).thenReturn(reportTemplates);

  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testGetReportTemplates_RetrieveSetOfTemplates() {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    configureMock(subject);

    ReportTemplatesResource resource = getReportTemplatesResource();
    Set<ReportTemplateDto> dtos = resource.get();
    ThreadContext.unbindSubject();

    assertThat(dtos).hasSize(4);

    ReportTemplateDto dto = (ReportTemplateDto) dtos.toArray()[0];
    assertThat(dto.getName()).isEqualTo("template1");
    assertThat(dto.getDesign()).isEqualTo("design");
    assertThat(dto.getFormat()).isEqualTo("format");
    assertThat(dto.getCron()).isEqualTo("schedule");
    assertThat(dto.getParametersList()).hasSize(2);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void test_update_with_new_report_created() {

    commandSchedulerService = mock(CommandSchedulerService.class);
    commandSchedulerService.unscheduleCommand("template9", "reports");
    commandSchedulerService.scheduleCommand("template9", "reports", "schedule");

    Command<Object> commandMock = mock(Command.class);
    commandSchedulerService.addCommand("template9", "reports", commandMock);
    when(commandRegistry.newCommand("report")).thenReturn(commandMock);

    ProjectReportTemplatesResource reportTemplatesResource = getProjectReportTemplatesResource();

    Response response = reportTemplatesResource.create(Dtos.asDto(createReportTemplate("template9")));

    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(response.getMetadata().get("location").get(0).toString())
        .isEqualTo("/project/project1/report-template/template9");

  }

  private ReportTemplate createReportTemplate(String name) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setProject("project1");
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setSchedule("schedule");
    Map<String, String> params = Maps.newHashMap();
    params.put("param1", "value1");
    params.put("param2", "value2");
    reportTemplate.setParameters(params);
    return reportTemplate;
  }

  private ReportTemplatesResource getReportTemplatesResource() {
    ReportTemplatesResource resource = new ReportTemplatesResource();
    resource.setReportTemplateService(reportTemplateService);
    return resource;
  }

  private ProjectReportTemplatesResource getProjectReportTemplatesResource() {
    ProjectReportTemplatesResource resource = new ProjectReportTemplatesResource();
    resource.setReportTemplateService(reportTemplateService);
    resource.setReportTemplateScheduler(reportTemplateScheduler);
    return resource;
  }

  private void configureMock(Subject subject) {
    when(subject.getPrincipal()).thenReturn(mock(Principal.class));
    when(subject.isPermitted("rest:/project/project1/report-template/template1:GET")).thenReturn(true);
    when(subject.isPermitted("rest:/project/project1/report-template/template2:GET")).thenReturn(true);
    when(subject.isPermitted("rest:/project/project1/report-template/template3:GET")).thenReturn(true);
    when(subject.isPermitted("rest:/project/project1/report-template/template4:GET")).thenReturn(true);
  }

}
