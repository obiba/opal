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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.OpalConfigurationService;
import org.obiba.opal.core.cfg.OpalConfigurationService.ConfigModificationTask;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.commands.Command;
import org.obiba.opal.shell.service.CommandSchedulerService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;
import org.obiba.opal.web.reporting.Dtos;

import com.google.common.collect.Maps;

import junit.framework.Assert;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class ReportTemplatesResourceTest {

  public static final String BASE_URI = "http://localhost:8888/ws";

  private OpalConfiguration opalConfiguration;

  private OpalConfigurationService opalConfigurationServiceMock;

  private CommandRegistry commandRegistry;

  private CommandSchedulerService commandSchedulerServiceMock;

  Set<ReportTemplate> reportTemplates;

  @Before
  public void setUp() {
    new MagmaEngine();
    opalConfigurationServiceMock = createMock(OpalConfigurationService.class);
    opalConfiguration = new OpalConfiguration();
    commandRegistry = createMock(CommandRegistry.class);

    commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template1", "reports");
    commandSchedulerServiceMock.scheduleCommand("template1", "reports", "schedule");

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
  public void testGetReportTemplates_RetrieveSetOfTemplates() {
    replay(opalConfigurationServiceMock);

    ReportTemplatesResource reportTemplateResource = new ReportTemplatesResource(opalConfigurationServiceMock,
        commandSchedulerServiceMock, commandRegistry);
    Set<ReportTemplateDto> reportTemplatesDtos = reportTemplateResource.getReportTemplates();

    Assert.assertEquals(4, reportTemplates.size());
    Assert.assertEquals(4, reportTemplatesDtos.size());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) reportTemplatesDtos.toArray()[0];
    Assert.assertEquals("template1", reportTemplateDto.getName());
    Assert.assertEquals("design", reportTemplateDto.getDesign());
    Assert.assertEquals("format", reportTemplateDto.getFormat());
    Assert.assertEquals("schedule", reportTemplateDto.getCron());
    Assert.assertEquals(2, reportTemplateDto.getParametersList().size());

    verify(opalConfigurationServiceMock);
  }

  @Test
  public void testUpdateReportTemplate_NewReportTemplateCreated() {

    opalConfigurationServiceMock.modifyConfiguration((ConfigModificationTask) EasyMock.anyObject());
    expectLastCall().once();

    CommandSchedulerService commandSchedulerServiceMock = createMock(CommandSchedulerService.class);
    commandSchedulerServiceMock.unscheduleCommand("template9", "reports");
    commandSchedulerServiceMock.scheduleCommand("template9", "reports", "schedule");

    @SuppressWarnings("unchecked")
    Command<Object> commandMock = createMock(Command.class);
    commandSchedulerServiceMock.addCommand("template9", "reports", commandMock);

    expect(commandRegistry.newCommand("report")).andReturn(commandMock);

    replay(opalConfigurationServiceMock, commandSchedulerServiceMock, commandRegistry);

    ReportTemplatesResource reportTemplatesResource = new ReportTemplatesResource(opalConfigurationServiceMock,
        commandSchedulerServiceMock, commandRegistry);
    Response response = reportTemplatesResource.createReportTemplate(Dtos.asDto(getReportTemplate("template9")));

    Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    Assert.assertEquals("/report-template/template9", response.getMetadata().get("location").get(0).toString());

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
