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
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.domain.ReportTemplate;
import org.obiba.opal.core.service.NoSuchReportTemplateException;
import org.obiba.opal.core.service.ReportTemplateService;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectReportTemplateResourceTest {

  private static final String BASE_URI = "http://localhost:8888/ws";

  private ReportTemplateService reportTemplateService;

  private ReportTemplateScheduler reportTemplateScheduler;

  @Before
  public void before() {
    new MagmaEngine();

    reportTemplateService = mock(ReportTemplateService.class);
    reportTemplateScheduler = mock(ReportTemplateScheduler.class);

    ReportTemplate reportTemplate1 = createReportTemplate("template1", "project1");
    ReportTemplate reportTemplate2 = createReportTemplate("template2", "project1");
    ReportTemplate reportTemplate3 = createReportTemplate("template3", "project1");
    ReportTemplate reportTemplate4 = createReportTemplate("template4", "project1");
    Collection<ReportTemplate> templates = new LinkedHashSet<>();
    templates.add(reportTemplate1);
    templates.add(reportTemplate2);
    templates.add(reportTemplate3);
    templates.add(reportTemplate4);
    when(reportTemplateService.getReportTemplates("project1")).thenReturn(templates);

    when(reportTemplateService.getReportTemplate("template1", "project1")).thenReturn(reportTemplate1);
    when(reportTemplateService.getReportTemplate("template2", "project1")).thenReturn(reportTemplate2);
    when(reportTemplateService.getReportTemplate("template3", "project1")).thenReturn(reportTemplate3);
    when(reportTemplateService.getReportTemplate("template4", "project1")).thenReturn(reportTemplate4);
    when(reportTemplateService.getReportTemplate("template9", "project1"))
        .thenThrow(NoSuchReportTemplateException.class);
  }

  @After
  public void after() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void test_get() {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    configureMock(subject);
    when(subject.isPermitted("rest:/project/project1/report-template/template3:GET")).thenReturn(true);

    ProjectReportTemplateResource resource = createResource("template3", "project1");
    Response response = resource.get();
    ThreadContext.unbindSubject();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) response.getEntity();
    assertThat(reportTemplateDto.getName()).isEqualTo("template3");
  }

  @Test(expected = NoSuchReportTemplateException.class)
  public void test_get_not_found() {
    ProjectReportTemplateResource resource = createResource("template9", "project1");
    assertThat(resource.get().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
  }

  @Test
  public void test_delete() {
    Subject mockSubject = mock(Subject.class);
    ThreadContext.bind(mockSubject);
    configureMock(mockSubject);
    when(mockSubject.isPermitted("rest:/project/project1/report-template/template2:GET")).thenReturn(true);

    CommandSchedulerService commandSchedulerService = mock(CommandSchedulerService.class);
    commandSchedulerService.deleteCommand("template2", "reports");

    ProjectReportTemplateResource resource = createResource("template2", "project1");
    Response response = resource.delete();
    ThreadContext.unbindSubject();

    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  private ProjectReportTemplateResource createResource(String name, String project) {
    ProjectReportTemplateResource resource = new ProjectReportTemplateResource();
    resource.setReportTemplateService(reportTemplateService);
    resource.setReportTemplateScheduler(reportTemplateScheduler);
    resource.setName(name);
    resource.setProject(project);
    return resource;
  }

  @Test(expected = NoSuchReportTemplateException.class)
  public void test_delete_not_found() {
    ProjectReportTemplateResource resource = createResource("template9", "project1");
    resource.delete();
  }

  @Test
  @Ignore
  public void test_update_with_new_report_created() {
    UriInfo uriInfoMock = mock(UriInfo.class);
    when(uriInfoMock.getAbsolutePath()).thenReturn(UriBuilder.fromUri(BASE_URI).build(""));

    CommandSchedulerService commandSchedulerService = mock(CommandSchedulerService.class);
    commandSchedulerService.unscheduleCommand("template9", "reports");
    commandSchedulerService.scheduleCommand("template9", "reports", "schedule");

    Command<?> command = mock(Command.class);
    commandSchedulerService.addCommand("template9", "reports", command);

    ProjectReportTemplateResource resource = createResource("template9", "project1");
    Response response = resource.update(Dtos.asDto(createReportTemplate("template9", "project1")));

    assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    assertThat(response.getMetadata().get("location").get(0).toString()).isEqualTo(BASE_URI);
  }

  @Test
  public void test_update_existing() {

    CommandSchedulerService commandSchedulerService = mock(CommandSchedulerService.class);
    commandSchedulerService.unscheduleCommand("template1", "reports");
    commandSchedulerService.scheduleCommand("template1", "reports", "schedule");

    ProjectReportTemplateResource resource = createResource("template1", "project1");

    Response response = resource.update(Dtos.asDto(createReportTemplate("template1", "project1")));
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
  }

  @Test
  public void test_update_with_error() {

    ProjectReportTemplateResource resource = new ProjectReportTemplateResource();
    resource.setReportTemplateService(reportTemplateService);
    resource.setName("template1");
    resource.setProject("project1");

    try {
      resource.update(Dtos.asDto(createReportTemplate("template2", "project1")));
      fail("Should throw IllegalArgumentException");
    } catch(Exception e) {
      assertThat(e) //
          .isInstanceOf(IllegalArgumentException.class) //
          .hasMessage("The report template name in the URI does not match the name given in the request body DTO.");
    }
  }

  private ReportTemplate createReportTemplate(String name, String project) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setProject(project);
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setSchedule("schedule");
    reportTemplate.setParameters(new HashMap<String, String>());
    return reportTemplate;
  }

  private void configureMock(Subject subject) {
    when(subject.getPrincipal()).thenReturn(mock(Principal.class));
    when(subject.getSession()).thenReturn(new SimpleSession());
  }
}
