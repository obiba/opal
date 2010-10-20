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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.reporting.service.ReportService;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

public class ReportTemplateResourceTest {

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
  }

  @Test
  public void testGetReportTemplate_ReportTemplateFoundAndReturned() {

    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).atLeastOnce();

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplate = new ReportTemplateResource("template3", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplate.getReportTemplate();
    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) response.getEntity();
    Assert.assertEquals("template3", reportTemplateDto.getName());

    verify(opalRuntimeMock);

  }

  @Test
  public void testGetReportTemplate_ReportTemplateNotFound() {
    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).atLeastOnce();

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplate = new ReportTemplateResource("template9", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplate.getReportTemplate();
    Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateDeleted() {
    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).atLeastOnce();

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplate = new ReportTemplateResource("template2", opalRuntimeMock, reportServiceMock);
    Response response = reportTemplate.deleteReportTemplate();

    Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    Assert.assertTrue(opalConfiguration.getReportTemplate("template2") == null);

    verify(opalRuntimeMock);

  }

  @Test
  public void testDeleteReportTemplate_ReportTemplateNotFound() {
    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration).atLeastOnce();

    replay(opalRuntimeMock);

    ReportTemplateResource reportTemplate = new ReportTemplateResource("template9", opalRuntimeMock, reportServiceMock);

    Response response = reportTemplate.deleteReportTemplate();
    Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  private ReportTemplate getReportTemplate(String name) {
    ReportTemplate reportTemplate = new ReportTemplate();
    reportTemplate.setName(name);
    reportTemplate.setDesign("design");
    reportTemplate.setFormat("format");
    reportTemplate.setParameters(new HashMap<String, String>());
    return reportTemplate;
  }
}
