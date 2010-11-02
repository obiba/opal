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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.opal.core.cfg.OpalConfiguration;
import org.obiba.opal.core.cfg.ReportTemplate;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.web.model.Opal.ReportTemplateDto;

import com.google.common.collect.Maps;

public class ReportTemplatesResourceTest {

  @Test
  public void testGetReportTemplates_RetrieveSetOfTemplates() {
    OpalRuntime opalRuntimeMock = createMock(OpalRuntime.class);
    OpalConfiguration opalConfiguration = new OpalConfiguration();
    expect(opalRuntimeMock.getOpalConfiguration()).andReturn(opalConfiguration);

    replay(opalRuntimeMock);

    Set<ReportTemplate> reportTemplates = new LinkedHashSet<ReportTemplate>();
    reportTemplates.add(getReportTemplate(reportTemplates, "template1"));
    reportTemplates.add(getReportTemplate(reportTemplates, "template2"));
    reportTemplates.add(getReportTemplate(reportTemplates, "template3"));

    opalConfiguration.setReportTemplates(reportTemplates);

    ReportTemplatesResource reportTemplateResource = new ReportTemplatesResource(opalRuntimeMock);
    Set<ReportTemplateDto> reportTemplatesDtos = reportTemplateResource.getReportTemplates();

    Assert.assertEquals(3, reportTemplates.size());
    Assert.assertEquals(3, reportTemplatesDtos.size());

    ReportTemplateDto reportTemplateDto = (ReportTemplateDto) reportTemplatesDtos.toArray()[0];
    Assert.assertEquals("template1", reportTemplateDto.getName());
    Assert.assertEquals("design", reportTemplateDto.getDesign());
    Assert.assertEquals("format", reportTemplateDto.getFormat());
    Assert.assertEquals("schedule", reportTemplateDto.getCron());
    Assert.assertEquals(2, reportTemplateDto.getParametersList().size());

    verify(opalRuntimeMock);
  }

  private ReportTemplate getReportTemplate(Set<ReportTemplate> reportTemplates, String name) {
    ReportTemplate reportTemplate1 = new ReportTemplate();
    reportTemplate1.setName(name);
    reportTemplate1.setDesign("design");
    reportTemplate1.setFormat("format");
    Map<String, String> params = Maps.newHashMap();
    params.put("param1", "value1");
    params.put("param2", "value2");
    reportTemplate1.setParameters(params);
    reportTemplate1.setSchedule("schedule");
    return reportTemplate1;
  }
}
